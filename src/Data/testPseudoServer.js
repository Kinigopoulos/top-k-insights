const {TopKAlgorithm} = require("./topK/algorithm");
const dataJSON = require("./SensorsAirPressure.json");
const {__timeColumns, __timeColumnsFunctions} = require("../constants");

function test(testDataObj) {
    const {result, columns, ordinalColumns, measureColumn, k, t, aggregator, extractors, insightTypes, datasource} = testDataObj;
    const queryColumns = columns.map(column => column.COLUMN_NAME);
    const newResult = result.map(row => {
        const newRow = {};
        for (const column of queryColumns) {
            if (__timeColumns.includes(column)) {
                newRow[column] = __timeColumnsFunctions[column](row.__time);
            } else {
                newRow[column] = row[column];
            }
        }
        return newRow;
    });

    const topKAlgorithm = new TopKAlgorithm();
    topKAlgorithm.initialize(newResult, columns, ordinalColumns, measureColumn, k, t, aggregator, extractors, insightTypes, datasource);

}


const testDataObj = {
    "result": dataJSON,
    "columns": [
        {
            "COLUMN_NAME": "dimos",
            "DATA_TYPE": "VARCHAR"
        },
        {
            "COLUMN_NAME": "nomos",
            "DATA_TYPE": "VARCHAR"
        },
        {
            "COLUMN_NAME": "__time_Day",
            "DATA_TYPE": "BIGINT"
        },
        {
            "COLUMN_NAME": "__time_Hour",
            "DATA_TYPE": "BIGINT"
        },
        {
            "COLUMN_NAME": "perifereia",
            "DATA_TYPE": "VARCHAR"
        },
        {
            "COLUMN_NAME": "value",
            "DATA_TYPE": "DOUBLE"
        }
    ],
    "ordinalColumns": [
        "__time_Day",
        "__time_Hour"
    ],
    "measureColumn": "value",
    "k": 10,
    "t": 2,
    "aggregator": "Sum",
    "extractors": [
        "PreviousDifferenceExtractor",
        "RankExtractor",
        "PercentageExtractor",
        "AverageDifferenceExtractor"
    ],
    "insightTypes": [
        "Point",
        "Shape",
        "Attribution",
        "TwoPoints",
        "LastPoint"
    ],
    "datasource": "Sensors"
}
const simpleTest = {
    "result": [
        {year: 2010, brand: "F", sales: 13},
        {year: 2011, brand: "F", sales: 10},
        {year: 2012, brand: "F", sales: 14},
        {year: 2013, brand: "F", sales: 23},
        {year: 2014, brand: "F", sales: 27},

        {year: 2010, brand: "B", sales: 20},
        {year: 2011, brand: "B", sales: 18},
        {year: 2012, brand: "B", sales: 20},
        {year: 2013, brand: "B", sales: 17},
        {year: 2014, brand: "B", sales: 19},

        {year: 2010, brand: "H", sales: 40},
        {year: 2011, brand: "H", sales: 35},
        {year: 2012, brand: "H", sales: 36},
        {year: 2013, brand: "H", sales: 43},
        {year: 2014, brand: "H", sales: 58},

        {year: 2010, brand: "T", sales: 38},
        {year: 2011, brand: "T", sales: 34},
        {year: 2012, brand: "T", sales: 34},
        {year: 2013, brand: "T", sales: 29},
        {year: 2014, brand: "T", sales: 36},
    ],
    "columns": [
        {
            "COLUMN_NAME": "year",
            "DATA_TYPE": "BIGINT"
        },
        {
            "COLUMN_NAME": "brand",
            "DATA_TYPE": "VARCHAR"
        },
        {
            "COLUMN_NAME": "sales",
            "DATA_TYPE": "DOUBLE"
        }
    ],
    "ordinalColumns": [
        "year"
    ],
    "measureColumn": "sales",
    "k": 10,
    "t": 2,
    "aggregator": "Sum",
    "extractors": [
        "PreviousDifferenceExtractor",
        "RankExtractor",
        "PercentageExtractor",
        "AverageDifferenceExtractor"
    ],
    "insightTypes": [
        "Point",
        "Shape"
    ],
    "datasource": "Sales"
}
test(testDataObj);
// test(simpleTest);
