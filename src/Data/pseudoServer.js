
import schema from "./schemaData.json";
const {TopKAlgorithm} = require("./topK/algorithm");

const {__timeColumns, __timeColumnsFunctions} = require("../constants");

export async function getDimensions() {
    return new Promise((resolve, reject) => {
        const result = {
            // data: Object.keys(SensorsAirPressure[0])
            data: ["__time", ...schema.map(s => s.COLUMN_NAME)].filter(t => t !== "type")
        }

        resolve(result);
    });
}

export async function runProgram(body) {

    return new Promise(async (resolve, reject) => {
        console.time("execution");

        const {options} = body;
        const {
            datasource,
            columns,
            ordinal,
            measureColumn,
            k,
            t,
            aggregator,
            extractors,
            insightTypes,
            filters
        } = options;


        const queryColumns = columns;
        const ordinalColumns = ordinal;

        const index = queryColumns.indexOf(measureColumn);
        if (index > -1) {
            queryColumns.splice(index, 1);
        }
        // if (queryColumns.some(c => __timeColumns.includes(c))) {
        //     queryColumns.push("__time");
        // }
        const ordinalIndex = ordinalColumns.indexOf(measureColumn);
        if (ordinalIndex > -1) {
            ordinalColumns.splice(ordinalIndex, 1);
        }

        queryColumns.sort();
        queryColumns.push(measureColumn);


        try {





            let dataJSON = [];
            switch (datasource) {
                case "Sensors - Air Pressure":
                    const SensorsAirPressure = await require("./SensorsAirPressure.json");
                    dataJSON = SensorsAirPressure;
                    break;
                case "Sensors - Air Temperature":
                    const SensorsAirTemperature = await require("./SensorsAirTemperature.json");
                    dataJSON = SensorsAirTemperature;
                    break;
                // case "Sensors - Soil Temperature":
                //     const SensorsSoilTemperature = await require("./SensorsSoilTemperature.json");
                //     dataJSON = SensorsSoilTemperature;
                //     break;
                default:
                    break;
            }

            const result = dataJSON.map(row => {
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
            const totalRows = result.length;

            const columnTypesResponse = {
                data: schema.filter(entry => queryColumns.includes(entry.COLUMN_NAME))
            };

            queryColumns.forEach(column => {
                if (__timeColumns.includes(column)) {
                    columnTypesResponse.data.splice(columnTypesResponse.data.length - 2, 0, {
                        COLUMN_NAME: column,
                        DATA_TYPE: "BIGINT"
                    });
                }
            });

            const columns = columnTypesResponse.data
                .filter(column => {
                    return queryColumns.includes(column.COLUMN_NAME) && column.COLUMN_NAME && column.COLUMN_NAME !== "__time";
                });

            console.log("=== STARTING JS ALGORITHM ===")

            const topKAlgorithm = new TopKAlgorithm();
            const insights = topKAlgorithm.initialize(result, columns, ordinalColumns, measureColumn, k, t, aggregator, extractors, insightTypes, datasource);

            resolve({data: {result: insights, rows: totalRows, dimensions: columns.map(column => column.COLUMN_NAME)}});

        } catch (err) {
            console.log(err);
            reject(err);
        }
    });
}
