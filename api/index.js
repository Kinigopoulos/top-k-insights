const express = require("express");
const app = express();

const axios = require('axios');
let java = require("java");

const jarPath = "./TopKInsights/out/artifacts/TopKInsights_jar";
java.classpath.push(`${jarPath}/TopKInsights.jar`);

app.use(express.json());
const port = 5000;

function arrayToString(array) {
    let res = "";
    for(let i = 0; i < array.length; i++){
        res += array[i]
        if(i !== array.length - 1){
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
            for(let entry of data){
                const entryColumns = entry.columns;
                Object.keys(entryColumns).forEach((key) => {
                    if(!columns.includes(key)){
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

function constructFilterJSON(filters){
    const fields = [];

    for(const filter of filters){
        if(filter.dimension && filter.type && filter.value){
            if(filter.type === "equals"){
                fields.push({type: "selector", dimension: filter.dimension, value: filter.value});
            } else if(filter.type === "not equals"){
                fields.push({type: "not", value: {type: "selector", dimension: filter.dimension, value: filter.value}});
            }
        }
    }

    //The filter JSON, if there are to filters return undefined
    return fields.length === 0 ? undefined : ({type: "and", fields: fields});
}

app.post('/run', async (req, res, next) => {

    console.time("execution");

    const {options, ports} = req.body;
    const {username = "", password = ""} = req.headers;
    const {datasource, columns, ordinal, measureColumn, k, t, aggregator, extractors, insightTypes, filters} = options;


    let queryColumns = columns;
    let ordinalColumns = ordinal;
    const index = queryColumns.indexOf(measureColumn);
    if (index > -1) {
        queryColumns.splice(index, 1);
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
    if(isScanQuery){
        delete groupByQuery.dimensions;
        groupByQuery.queryType = "scan";
        groupByQuery.columns = queryColumns;
    }

    const filtersJSON = constructFilterJSON(filters);
    console.log(filtersJSON);
    if(filtersJSON){
        groupByQuery.filter = filtersJSON;
    }
    console.log(groupByQuery);

    try {
        const axiosResponse = await axios.post(`${ports.broker}/druid/v2`, groupByQuery, {auth: {username, password}});

        console.log(axiosResponse.data);
        const result = isScanQuery ?
            axiosResponse.data.map(obj => {
                return obj.events
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

        const columns = columnTypesResponse.data.filter(column => {
            return queryColumns.includes(column.COLUMN_NAME);
        });

        console.log(columns);

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
                res.send({result: JSON.parse(result), rows: totalRows});
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
