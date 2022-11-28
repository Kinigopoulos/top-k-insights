class Database {

    constructor(dimensions, domainDimensions, measureIndex, ordinal, name) {
        this.data = [];
        this.dimensions = dimensions;
        this.domainDimensions = domainDimensions;
        this.measureIndex = measureIndex;
        this.ordinal = ordinal;
        this.name = name;

        this.domainValues = [];
        for (let i = 0; i < dimensions.length; i++) {
            this.domainValues.push(new Map());
        }
        this.superSubspace = this.getSubspace();
    }

    addRow(row) {
        if (row.length === this.dimensions.length) {
            for (let i = 0; i < row.length; i++) {
                if (this.measureIndex === i) {
                    continue;
                }
                if (this.domainValues[i].has(row[i].getValue())) {
                    row[i] = this.domainValues[i].get(row[i].getValue());
                } else {
                    this.domainValues[i].set(row[i].getValue(), row[i]);
                }
            }
            this.data.push(row);
        } else {
            console.log("Wrong size of columns");
            throw new Error("Wrong size");
        }
    }

    getSubspace() {
        const subspace = [];
        for(let i = 0; i < this.domainDimensions.length; i++) {
            subspace.push(null);
        }
        return subspace;
    }

    getName() {
        return this.name;
    }

    getDomainValues() {
        return this.domainValues;
    }

    static getSubspaceCopy(initial) {
        return [...initial];
    }

    getDomainDimensions() {
        return this.domainDimensions;
    }

    getDimensions() {
        return this.dimensions;
    }

    getMeasureValue(index) {
        return this.data[index][this.measureIndex].getValue();
    }

    belongsToSubspace(subspace, index) {
        for (const domainDimension of this.domainDimensions) {
            if (subspace[domainDimension] !== null && subspace[domainDimension] !== this.data[index][domainDimension]) {
                return false;
            }
        }
        return true;
    }

    getSubspaceSum(subspace) {
        let sum = 0;
        for (let i = 0; i < this.data.length; i++) {
            if (this.belongsToSubspace(subspace, i)) {
                sum += this.getMeasureValue(i);
            }
        }
        return sum;
    }

    getDimensionName(dimension) {
        return this.dimensions[dimension];
    }

    getValues(dimension) {
        return [...this.domainValues[dimension].values()];
    }

    isOrdinal(dimension){
        return this.ordinal[dimension];
    }

    getMeasureIndex() {
        return this.measureIndex;
    }

    size() {
        return this.data.length;
    }
}

export {Database};
