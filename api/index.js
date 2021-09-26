const express = require("express");
const app = express();

const axios = require('axios');
const {__timeColumns, __timeColumnsFunctions} = require("../src/constants");
let java = require("java");

const jarPath = "./TopKInsights/out/artifacts/TopKInsights_jar";
java.classpath.push(`${jarPath}/TopKInsights.jar`);

app.use(express.json());
const port = 5000;

function arrayToString(array) {
    let res = "";
    for (let i = 0; i < array.length; i++) {
        res += array[i]
        if (i !== array.length - 1) {
            res += ",";
        }
    }
    return res;
}

app.get('/data-sources', (req, res, next) => {
    const broker = req.query.broker;
    const {username = "", password = ""} = req.headers;
    axios.get(`${broker}/druid/v2/datasources`,
        {auth: {username, password}})
        .then(resAxios => {
            res.send(resAxios.data);
        })
        .catch(errAxios => {
            console.log(errAxios.message);
            next(errAxios);
        });
});

app.get('/dimensions', (req, res, next) => {
    const {broker, datasource} = req.query;
    const {username = "", password = ""} = req.headers;
    axios.post(`${broker}/druid/v2/`, {
            "queryType": "segmentMetadata",
            "dataSource": datasource
        },
        {auth: {username, password}})
        .then(resAxios => {
            const data = resAxios.data;
            const columns = [];
            for (let entry of data) {
                const entryColumns = entry.columns;
                Object.keys(entryColumns).forEach((key) => {
                    if (!columns.includes(key)) {
                        columns.push(key);
                    }
                })
            }
            res.send(columns);
        })
        .catch(errAxios => {
            console.log(errAxios.message);
            next(errAxios);
        });
});

function constructFilterJSON(filters) {
    function getCondition(type, dimension, value) {
        switch (type) {
            case "equals":
                return {type: "selector", dimension: dimension, value: value};
            case "not equals":
                return {type: "not", field: {type: "selector", dimension: dimension, value: value[0]}};
            default:
                return undefined;
        }
    }
    function constructOrJSON(type, dimension, values) {
        const fields = [];
        for (const value of values) {
            const obj = getCondition(type, dimension, value);
            if (obj) {
                fields.push(obj);
            }

        }
        return ({type: "or", fields: fields});
    }

    const fields = [];
    for (const filter of filters) {
        if (filter.dimension && filter.type && filter.value && filter.value.length > 0) {
            if (filter.value.length === 1) {
                const obj = getCondition(filter.type, filter.dimension, filter.value[0]);
                if (obj) {
                    fields.push(obj);
                }
            } else {
                fields.push(constructOrJSON(filter.type, filter.dimension, filter.value));
            }
        }
    }
    //The filter JSON, if there are no filters return undefined
    return fields.length === 0 ? undefined : ({type: "and", fields: fields});
}

app.post('/run', async (req, res, next) => {

    console.time("execution");

    const {options, ports} = req.body;
    const {username = "", password = ""} = req.headers;
    const {datasource, columns, ordinal, measureColumn, k, t, aggregator, extractors, insightTypes, filters} = options;


    const queryColumns = columns;
    const ordinalColumns = ordinal;

    const index = queryColumns.indexOf(measureColumn);
    if (index > -1) {
        queryColumns.splice(index, 1);
    }
    if (queryColumns.some(c => __timeColumns.includes(c))){
        queryColumns.push("__time");
    }
    const ordinalIndex = ordinalColumns.indexOf(measureColumn);
    if (ordinalIndex > -1) {
        ordinalColumns.splice(ordinalIndex, 1);
    }

    queryColumns.sort();
    queryColumns.push(measureColumn);

    const groupByQuery = {
        queryType: "groupBy",
        dataSource: datasource,
        dimensions: queryColumns,
        granularity: "all",
        intervals: "0/10000"
    };

    const isScanQuery = true;
    if (isScanQuery) {
        delete groupByQuery.dimensions;
        groupByQuery.queryType = "scan";
        groupByQuery.columns = queryColumns;
    }

    const filtersJSON = constructFilterJSON(filters);
    console.log(filtersJSON);
    if (filtersJSON) {
        groupByQuery.filter = filtersJSON;
    }
    console.log(groupByQuery);

    try {
        const axiosResponse = await axios.post(`${ports.broker}/druid/v2`, groupByQuery, {auth: {username, password}});

        console.log(axiosResponse.data);
        const result = isScanQuery ?
            axiosResponse.data.map(obj => {
                return obj.events.map(event => {
                    if(!event.__time){
                        return event;
                    }
                    const time = event.__time;
                    delete event.__time;
                    for(const key of Object.keys(event)) {
                        if(__timeColumns.includes(key)){
                            event[key] = __timeColumnsFunctions[key](time);
                        }
                    }
                    return event;
                });
            }).flat()
            :
            axiosResponse.data.map(obj => {
                return obj['event']
            });
        const totalRows = result.length;

        console.log(result);

        const columnTypesResponse = await axios.post(`${ports.router}/druid/v2/sql`, {
            "query": `SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS\n
            WHERE TABLE_SCHEMA = 'druid' AND TABLE_NAME = '${datasource}'`
        }, {auth: {username, password}});
        queryColumns.forEach(column => {
            if(__timeColumns.includes(column)){
                columnTypesResponse.data.splice(columnTypesResponse.data.length - 2, 0, {COLUMN_NAME: column, DATA_TYPE: "BIGINT"});
            }
        });

        const columns = columnTypesResponse.data.filter(column => {
            return queryColumns.includes(column.COLUMN_NAME) && column.COLUMN_NAME && column.COLUMN_NAME !== "__time";
        });

        console.log(columns);
        console.log("=== STARTING JAVA ALGORITHM ===")

        java.callStaticMethod("com.kynigopoulos.Main", "getResults",
            JSON.stringify(result), JSON.stringify(columns), arrayToString(ordinalColumns), measureColumn,
            k, t, aggregator, arrayToString(extractors), arrayToString(insightTypes), datasource,
            function (err, result) {
                if (err || result === "") {
                    console.log(err);
                    res.status(500).send({message: "Error occurred with the algorithm."});
                    return;
                }
                console.log(JSON.parse(result));
                const dimensions = columns.map(column => column.COLUMN_NAME);
                res.send({result: JSON.parse(result), rows: totalRows, dimensions: dimensions});
                console.timeEnd("execution");
            });
    } catch (err) {
        console.log(err);
        next(err.response);
    }

});


app.listen(port, () => {
    console.log(`Listening on port ${port}!`)
});
