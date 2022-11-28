const regression = require("regression");
const Logistic = require('@stdlib/stats-base-dists-logistic').Logistic;
const Normal = require('@stdlib/stats-base-dists-normal').Normal;
const mean = require('@stdlib/stats-base-mean');

class DataType {
    constructor(value) {
        this.value = value;
    }

    getValue() {
        return this.value;
    }
}

class Aggregator {
    getOutput(database, subspace, dimension) {
        return .0;
    }

    toString() {
        return "Aggregator";
    }
}

class SumAggregator extends Aggregator {
    getOutput(database, subspace, dimension) {
        let sum = 0;
        for (let i = 0; i < database.size(); i++) {
            if (database.belongsToSubspace(subspace, i)) {
                sum += database.getMeasureValue(i);
            }
        }
        return sum;
    }

    toString() {
        return "Sum";
    }
}

class CountAggregator extends Aggregator {
    getOutput(database, subspace, dimension) {
        let count = 0;
        for (let i = 0; i < database.size(); i++) {
            if (database.belongsToSubspace(subspace, i)) {
                count++;
            }
        }
        return count;
    }

    toString() {
        return "Count";
    }
}

class MeanAggregator extends Aggregator {
    getOutput(database, subspace, dimension) {
        let sum = 0;
        let count = 0;
        for (let i = 0; i < database.size(); i++) {
            if (database.belongsToSubspace(subspace, i)) {
                sum += database.getMeasureValue(i);
                count++;
            }
        }
        if (count === 0) {
            return .0;
        }
        return sum / count;
    }

    toString() {
        return "Mean";
    }
}

class Extractor {
    satisfiesRequirements(database, dimension) {
        return true;
    }

    isMeaningful(extractor) {
        return true;
    }

    getOutput(input, value) {
        return null;
    }

    toString() {
        return "Extractor";
    }
}

class PercentageExtractor extends Extractor {
    isMeaningful(extractor) {
        return false;
    }

    getOutput(input, value) {
        let sum = 0;
        for (const measure of input.values()) {
            sum += measure;
        }
        if (sum === 0) {
            return .0;
        }

        return input.get(value) / sum;
    }

    toString() {
        return "PercentageExtractor";
    }
}

class AverageDifferenceExtractor extends Extractor {
    getOutput(input, value) {
        let avg = 0;
        for (const measure of input.values()) {
            avg += measure;
        }

        avg /= input.size;
        return input.get(value) - avg;
    }

    toString() {
        return "AverageDifferenceExtractor";
    }
}

class PreviousDifferenceExtractor extends Extractor {
    satisfiesRequirements(database, dimension) {
        return database.isOrdinal(dimension);
    }

    getOutput(input, value) {
        const keys = [...input.keys()];
        keys.sort((a, b) => a.getValue() - b.getValue());
        const key = keys.findIndex(key => key === value) - 1;
        const previousValue = keys[key];
        if (previousValue === undefined) {
            return null;
        }
        return input.get(value) - input.get(previousValue);
    }

    toString() {
        return "PreviousDifferenceExtractor";
    }
}

class RankExtractor extends Extractor {
    getOutput(input, value) {
        let rank = 0;
        for (const measure of input.values()) {
            if (measure < input.get(value)) {
                rank++;
            }
        }
        return rank;
    }

    toString() {
        return "RankExtractor";
    }
}

class InsightType {
    getSignificance(F) {
    }

    getType() {
    }
}

function calculateStandardDeviation(numbers, mean) {
    let deviation = 0;
    for (const number of numbers) {
        deviation += Math.pow(number - mean, 2);
    }
    return Math.sqrt(deviation / numbers.length);
}

class PointInsight extends InsightType {
    getSignificance(F) {
        let sortedValues = [...F.values()];
        if (sortedValues.length < 2) {
            return .0;
        }
        sortedValues.sort((a, b) => b - a);
        const min = sortedValues[sortedValues.length - 1];
        if (min <= 0) {
            sortedValues = sortedValues.map(s => s - min + 0.1);
        }

        const [max] = sortedValues.splice(0, 1);
        if (max === sortedValues[sortedValues.length - 1]) {
            return .0;
        }
        const data = sortedValues.map((value, key) => [key + 2, value]);
        const regressionResult = regression.power(data);

        const intercept = regressionResult.equation[0];
        const slope = regressionResult.equation[1];

        if (isNaN(intercept) || isNaN(slope)) {
            return .0;
        }

        const residuals = [];
        for (let i = 0; i < sortedValues.length; i++) {
            const predictedValue = intercept * Math.pow(i + 2, slope);
            residuals.push(predictedValue);
        }
        const xMaxErr = max - intercept;
        const meanResult = mean(residuals.length, residuals, 1);

        let standardDeviation = calculateStandardDeviation(residuals, meanResult);
        if (standardDeviation === 0) {
            standardDeviation = Number.MIN_VALUE;
        }

        const normalDistribution = new Normal(meanResult, standardDeviation * 5);

        return normalDistribution.cdf(xMaxErr);
    }

    getType() {
        return "Point";
    }
}

class ShapeInsight extends InsightType {
    constructor() {
        super();
        this.distribution = new Logistic(0.2, 2);
    }
    getSignificance(F) {
        try {
            const keys = [...F.keys()];
            if (keys.length < 2) {
                return .0;
            }
            keys.sort((a, b) => a.getValue() - b.getValue());
            if (keys[0].getValue() === keys[keys.length - 1].getValue()) {
                return .0;
            }
            const data = keys.map(key => [key.getValue(), F.get(key)]);
            const regressionResult = regression.linear(data);
            const slope = regressionResult.equation[0];
            if (slope === 0) {
                return 0;
            }
            const r2 = regressionResult.r2;

            const prob = this.distribution.cdf(Math.abs(slope));
            return prob * r2;
        } catch (e) {
            console.log(e);
        }
        return -0.1;
    }

    getType() {
        return "Shape";
    }
}

class Attribution extends InsightType {
    getSignificance(F) {
        let sortedValues = [...F.values()];
        if (sortedValues.length < 2) {
            return .0;
        }
        sortedValues.sort((a, b) => b - a);
        if (sortedValues[sortedValues.length - 1] < 0) {
            return .0;
        }
        let sum = 0;
        for (const value of sortedValues) {
            sum += value;
        }
        if (sum === 0) {
            return 0;
        }

        const max = sortedValues[0];
        const percentage = max / sum;

        const normalDistribution = new Normal(0.5, 0.25);
        return normalDistribution.cdf(percentage);
    }

    getType() {
        return "Attribution";
    }
}

class LastPointInsight extends InsightType {
    getSignificance(F) {
        let sortedValues = [...F.values()];
        if (sortedValues.length < 2) {
            return .0;
        }
        sortedValues.sort((a, b) => b - a);
        const minVal = sortedValues[sortedValues.length - 1];
        if (minVal <= 0) {
            sortedValues = sortedValues.map(s => s - minVal + 0.1);
        }

        const [min] = sortedValues.splice(sortedValues.length - 1, 1);
        if (min === sortedValues[0]) {
            return .0;
        }
        const data = sortedValues.map((value, key) => [key + 2, value]);
        const regressionResult = regression.power(data);

        const intercept = regressionResult.equation[0];
        const slope = regressionResult.equation[1];

        if (isNaN(intercept) || isNaN(slope)) {
            return .0;
        }

        const residuals = [];
        for (let i = 0; i < sortedValues.length; i++) {
            const predictedValue = intercept * Math.pow(i + 2, slope);
            residuals.push(predictedValue);
        }
        const xMaxErr = min - intercept;
        const meanResult = mean(residuals.length, residuals, 1);

        let standardDeviation = calculateStandardDeviation(residuals, meanResult);
        if (standardDeviation === 0) {
            standardDeviation = Number.MIN_VALUE;
        }

        const normalDistribution = new Normal(meanResult, standardDeviation * 5);

        return normalDistribution.cdf(xMaxErr);
    }

    getType() {
        return "LastPoint";
    }
}

class TwoPointsInsight extends InsightType {
    getSignificance(F) {
        let sortedValues = [...F.values()];
        if (sortedValues.length < 2) {
            return .0;
        }
        sortedValues.sort((a, b) => b - a);

        let sum = 0;
        for (const value of sortedValues) {
            sum += value;
        }
        if (sum === 0) {
            return 0;
        }
        const max = sortedValues[0];
        const max2 = sortedValues[1];
        const percentage = (max + max2) / sum;
        const normalDistribution = new Normal(0.5, 0.25);

        return normalDistribution.cdf(percentage);
    }

    getType() {
        return "TwoPoints";
    }
}



class Config {

    static setExtractors(extractors) {
        Config.extractors = extractors;
    }

    static setInsightTypes(insightsTypes) {
        Config.insightTypes = insightsTypes;
    }

    static setAggregator(aggregator) {
        Config.aggregator = aggregator;
    }

    static setExtractorByString(extractors) {
        const extractorsSet = new Set(extractors);
        const selected = [];
        for (const extractor of Config.availableExtractors) {
            if (extractorsSet.has(extractor.toString())) {
                selected.push(extractor);
            }
        }
        Config.setExtractors(selected);
    }

    static setInsightTypesByString(insightTypes) {
        const insightTypesSet = new Set(insightTypes);
        const selected = [];
        for (const insightType of Config.availableInsightTypes) {
            if (insightTypesSet.has(insightType.getType())) {
                selected.push(insightType);
            }
        }
        Config.setInsightTypes(selected);
    }

    static setAggregatorByString(aggregator) {
        for (const aggregatorFunction of Config.availableAggregators) {
            if (aggregatorFunction.toString() === aggregator) {
                Config.setAggregator(aggregatorFunction);
                return;
            }
        }
    }
}
Config.availableExtractors = [
    new AverageDifferenceExtractor(),
    new PercentageExtractor(),
    new PreviousDifferenceExtractor(),
    new RankExtractor()
];
Config.availableInsightTypes = [
    new PointInsight(),
    new LastPointInsight(),
    new Attribution(),
    new TwoPointsInsight(),
    new ShapeInsight()
];
Config.availableAggregators = [
    new SumAggregator(),
    new MeanAggregator(),
    new CountAggregator()
];
Config.extractors = Config.availableExtractors;
Config.insightTypes = Config.availableInsightTypes;
Config.aggregator = Config.availableAggregators[0];

class ExtractorPair {
    constructor(dimension, type) {
        this.dimension = dimension;
        this.type = type;
    }

    getDimension() {
        return this.dimension;
    }

    getType() {
        return this.type;
    }
}


class CompositeExtractor {

    constructor({compositeExtractor, pair}) {
        this.pairs = new Array(CompositeExtractor.t);
        if (compositeExtractor) {
            this.pairs = compositeExtractor.pairs.map(pair => {
                if (pair) {
                    return new ExtractorPair(pair.getDimension(), pair.getType());
                }
                return pair;
            });
        }
        if (pair) {
            this.pairs[0] = pair;
        }
    }

    getPair(index) {
        return this.pairs[index];
    }


    static makeCompositeExtractor(extractors, database, level, domainDimensions) {
        if (level === this.t) {
            return extractors;
        }

        const newExtractors = [];
        for (const compositeExtractor of extractors) {
            for (const extractor of Config.extractors) {
                if (level > 1 && !extractor.isMeaningful(compositeExtractor.getPair(level - 1).getType())) {
                    continue;
                }
                for (const dimension of domainDimensions) {
                    if (extractor.satisfiesRequirements(database, dimension)) {
                        const Ce = new CompositeExtractor({compositeExtractor});
                        Ce.pairs[level] = new ExtractorPair(dimension, extractor);
                        newExtractors.push(Ce);
                    }
                }
            }
        }

        return CompositeExtractor.makeCompositeExtractor(newExtractors, database, level + 1, domainDimensions);
    }

    getAggregator(extractor) {
        return extractor.getPair(0).getType();
    }

    static findCombinations(database, t) {
        CompositeExtractor.t = t;
        const domainDimensions = database.getDomainDimensions();
        const measureDimension = database.getMeasureIndex();
        const baseExtractor = [];
        baseExtractor.push(new CompositeExtractor({pair: new ExtractorPair(measureDimension, Config.aggregator)}));

        return CompositeExtractor.makeCompositeExtractor(baseExtractor, database, 1, domainDimensions);
    }

    static getAggregator(extractor) {
        return extractor.getPair(0).getType();
    }

    toString(database) {
        let s = "<";
        for(let i = 0; i < CompositeExtractor.t; i++){
            s += "(" + this.getPair(i).getType().toString() + ", " + database.getDimensionName(this.getPair(i).getDimension()) + ")";
        }
        s += ">";
        return s;
    }
}
CompositeExtractor.t = 1;

export {
    DataType,
    Aggregator,
    SumAggregator,
    CountAggregator,
    MeanAggregator,
    Extractor,
    PercentageExtractor,
    AverageDifferenceExtractor,
    PreviousDifferenceExtractor,
    RankExtractor,
    InsightType,
    PointInsight,
    ShapeInsight,
    Attribution,
    LastPointInsight,
    TwoPointsInsight,
    Config,
    ExtractorPair,
    CompositeExtractor
};
