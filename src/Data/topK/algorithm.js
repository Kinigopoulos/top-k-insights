const {Database} = require("./database");
const {DataType, Config, CompositeExtractor} = require("./utils");

function extractTypes(columns) {
    const dimensionTypes = new Map();
    for (const column of columns) {
        let value = undefined;
        switch (column.DATA_TYPE) {
            case "CHAR":
            case "VARCHAR":
                value = "";
                break;
            case "DECIMAL":
            case "REAL":
            case "FLOAT":
            case "DOUBLE":
                value = 0.0;
                break;
            case "BOOLEAN":
                value = false;
                break;
            default:
                value = 0;
                break;
        }
        dimensionTypes.set(column.COLUMN_NAME, value);
    }
    return dimensionTypes;
}

class Insight {
    constructor(subspace, dimension, Ce, value, insightType, resultSet) {
        this.subspace = subspace;
        this.dimension = dimension;
        this.compositeExtractor = Ce;
        this.value = value;
        this.insightType = insightType;
        this.resultSet = resultSet;
    }

    getValue() {
        return this.value;
    }

    toString() {
        return "Insight{" +
            "subspace=" + TopKInsights.getSubspaceString(this.subspace) +
            ", dimension=" + this.dimension +
            ", compositeExtractor=" + this.compositeExtractor +
            ", value=" + this.value +
            '}';
    }

    toJSON() {
        const subspaceObj = this.subspace.map(dim => {
            if (dim) {
                return dim.getValue();
            }
            return dim;
        });
        const extractorObj = this.compositeExtractor.pairs.map(pair => {
            return {type: pair.getType().toString(), dimension: pair.getDimension()};
        });
        const resultSetObj = {};
        [...this.resultSet.keys()].forEach(key => {
            resultSetObj[key.getValue()] = this.resultSet.get(key);
        });
        return {
            subspace: subspaceObj,
            dimension: this.dimension,
            extractor: extractorObj,
            value: this.value,
            insightType: this.insightType.getType(),
            resultSet: resultSetObj
        }
    }
}

class Queue {
    constructor(k) {
        this.k = k;
        this.list = [];
    }
    peek() {
        return this.list[0];
    }
    poll() {
        this.list.splice(0, 1);
    }
    add(insight) {
        for (let i = 0; i < this.list.length; i++) {
            if (this.list[i].getValue() >= insight.getValue()) {
                this.list.splice(i, 0, insight);
                return;
            }
        }
        this.list.splice(this.list.length, 0, insight);
    }
}

class TopKInsights {
    constructor(database, k, t) {
        this.database = database;
        this.k = k;
        this.t = t;
    }

    getInsights() {
        this.priorityQueue = new Queue(this.k);
        this.dataCube = new Map();
        this.impactCube = new Map();

        const domainDimensions = this.database.getDomainDimensions();
        this.dimensionNames = this.database.getDimensions();

        const compositeExtractors = CompositeExtractor.findCombinations(this.database, this.t);
        const subspace = Database.getSubspaceCopy(this.database.superSubspace);

        for (const compositeExtractor of compositeExtractors) {
            for (const domainDimension of domainDimensions) {
                this.EnumerateInsight(subspace, domainDimension, compositeExtractor);
                const values = this.database.getValues(domainDimension);
                for (const value of values) {
                    const newSubspace = Database.getSubspaceCopy(subspace);
                    newSubspace[domainDimension] = value;
                    for (const dividingDimension of domainDimensions) {
                        if (newSubspace[dividingDimension] !== null) {
                            continue;
                        }
                        this.EnumerateInsight(newSubspace, dividingDimension, compositeExtractor);
                    }
                }
            }
        }

        return this.priorityQueue.list.reverse();
    }

    isValid(subspace, dividingDimension, extractor) {
        for (let i = 1; i < this.t; i++) {
            const Dx = extractor.getPair(i).getDimension();
            if (dividingDimension !== Dx && subspace[Dx] === null) {
                return false;
            }
        }
        return true;
    }

    static getSubspaceString(subspace) {
        let sub = "< ";
        for (const dataType of subspace) {
            if (dataType === null) {
                sub += "* ";
            } else {
                sub += `${dataType.getValue()} `;
            }
        }
        sub += ">";
        return sub;
    }

    getImpact(subspace, subspaceString) {
        if (this.impactCube.has(subspaceString)) {
            return this.impactCube.get(subspaceString);
        }
        const impact = this.database.getSubspaceSum(subspace);
        this.impactCube.set(subspaceString, impact);
        return impact;
    }

    EnumerateInsight(subspace, dimension, extractor) {
        const sub = TopKInsights.getSubspaceString(subspace);
        console.log("SG(" + sub + ", " + this.database.getDimensionName(dimension) + ") \t\t" + extractor.toString(this.database) + ", is valid: " + this.isValid(subspace, dimension, extractor));

        if (this.isValid(subspace, dimension, extractor)) {
            const impact = this.getImpact(subspace, sub) / this.getImpact(Database.getSubspaceCopy(this.database.superSubspace));
            if (this.priorityQueue.list.length === this.k) {
                if (this.priorityQueue.peek() && impact < this.priorityQueue.peek().getValue()) {
                    return;
                }
            }
            const F = this.ExtractF(subspace, dimension, extractor);
            const isOrdinal = this.database.isOrdinal(dimension);
            if (F.size === 0) {
                return;
            }

            for (const insightType of Config.insightTypes) {
                if (!isOrdinal && insightType.getType() === "Shape") {
                    continue;
                }
                const significance = insightType.getSignificance(F);
                if (significance < 0) {
                    continue;
                }
                const S = significance * impact;
                if (this.priorityQueue.list.length === this.k) {
                    if (this.priorityQueue.peek() && S > this.priorityQueue.peek().getValue()) {
                        this.priorityQueue.poll();
                        this.priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                    }
                } else {
                    this.priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                }
            }
        }
    }

    ExtractF(subspace, dimension, extractor) {
        const F = new Map();

        const newSubspace = Database.getSubspaceCopy(subspace);
        for (const value of this.database.getValues(dimension)) {
            newSubspace[dimension] = value;
            const M = this.RecursiveExtract(newSubspace, dimension, this.t, extractor);
            if (M !== null) {
                F.set(value, M);
            }
        }
        return F;
    }

    RecursiveExtract(subspace, dimension, level, extractor) {
        if (level > 1) {
            const FLevel = new Map();
            const extractorDimension = extractor.getPair(level - 1).getDimension();

            const childSubspace = Database.getSubspaceCopy(subspace);
            for (const value of this.database.getValues(extractorDimension)) {
                childSubspace[extractorDimension] = value;
                const M = this.RecursiveExtract(childSubspace, dimension, level - 1, extractor);
                if (M !== null) {
                    FLevel.set(value, M);
                }
            }

            if (FLevel.get(subspace[extractorDimension]) === null || FLevel.get(subspace[extractorDimension]) === undefined) {
                return null;
            }

            const e = extractor.getPair(level - 1).getType();
            return e.getOutput(FLevel, subspace[extractorDimension]);
        }
        const subspaceString = TopKInsights.getSubspaceString(subspace);
        if (this.dataCube.has(subspaceString)) {
            return this.dataCube.get(subspaceString);
        }

        const M = CompositeExtractor.getAggregator(extractor).getOutput(this.database, subspace, dimension);

        this.dataCube.set(subspaceString, M);
        return M;
    }
}

class TopKAlgorithm {

    initialize(data, columns, ordinalColumns, measureColumnName, k, t, aggregator, extractors, insightTypes, datasource) {
        const domainDimensions = new Array(columns.length - 1);
        const dimensionNames = new Array(columns.length);
        const dimensionTypes = extractTypes(columns);

        const measureDimension = dimensionNames.length - 1;
        for (let i = 0; i < domainDimensions.length; i++) {
            domainDimensions[i] = i;
        }
        let index = 0;
        for (const column of columns) {
            const columnName = column.COLUMN_NAME;
            if (columnName === "__time") {
                continue;
            } else if (columnName === measureColumnName) {
                dimensionNames[measureDimension] = columnName;
                continue;
            } else {
                dimensionNames[index] = columnName;
            }
            index++;
        }

        const ordinalSet = new Set(ordinalColumns);
        const ordinalDimensions = new Array(dimensionNames.length - 1);
        for (let i = 0; i < ordinalDimensions.length; i++) {
            if (ordinalSet.has(dimensionNames[i])) {
                ordinalDimensions[i] = true;
            }
        }

        const database = new Database(dimensionNames, domainDimensions, measureDimension, ordinalDimensions, datasource);

        for (const object of data) {
            const row = [];
            for (const dimensionName of dimensionNames) {
                const value = dimensionTypes.get(dimensionName);
                row.push(new DataType(object[dimensionName] || value));
            }
            database.addRow(row);
        }

        Config.setAggregatorByString(aggregator);
        Config.setExtractorByString(extractors);
        Config.setInsightTypesByString(insightTypes);

        const topKInsights = new TopKInsights(database, k, t);
        const insights = topKInsights.getInsights().map(insight => insight.toJSON());

        console.log(insights);
        return insights;
    }
}

export {TopKAlgorithm};
