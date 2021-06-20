const express = require("express");
const app = express();

const axios = require('axios');
let java = require("java");
java.classpath.push("./TopKInsights/out/artifacts/TopKInsights_jar/TopKInsights.jar");

app.use(express.json());
const port = 5000;

app.get('/data-sources', (req, res, next) => {
    const coordinator = req.query.coordinator;
    axios.get(`${coordinator}/druid/coordinator/v1/datasources?full`)
        .then(resAxios => {
            res.send(resAxios.data);
        })
        .catch(errAxios => {
            next(errAxios.response);
        })
})


app.post('/run', async (req, res, next) => {
    const {options, ports} = req.body;
    const { datasource, columns, ordinal, measureColumn, k, t, aggregator, extractors, insightTypes } = options;

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

    queryColumns.push(measureColumn);


    try {
        const axiosResponse = await axios.post(`${ports.broker}/druid/v2`, {
            queryType: "groupBy",
            dataSource: datasource,
            dimensions: queryColumns,
            granularity: "all",
            intervals: "0/10000"
        });

        const result = axiosResponse.data.map(obj => {
            return obj['event']
        });

        console.log(result);

        const columnTypesResponse = await axios.post(`${ports.router}/druid/v2/sql`, {
            "query": `SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS\n
WHERE TABLE_SCHEMA = 'druid' AND TABLE_NAME = '${datasource}'`
        });

        const columns = columnTypesResponse.data.filter(column => {
            return queryColumns.includes(column.COLUMN_NAME);
        });

        console.log(columns);

        java.callStaticMethod("com.kynigopoulos.Main", "getResults",
            JSON.stringify(result), JSON.stringify(columns), ordinalColumns, measureColumn,
            k, t, aggregator, extractors, insightTypes,
            function (err, result) {
            if (err || result === "") {
                console.log(err);
                res.status(500).send({message: "Error occurred with the algorithm."});
                return;
            }
            console.log(JSON.parse(result));
            res.end(result);
        });
    } catch (err) {
        console.log(err);
        next(err.response);
    }

})


app.listen(port, () => {
    console.log(`Listening on port ${port}!`)
});
