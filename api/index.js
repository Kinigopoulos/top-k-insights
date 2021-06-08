const express = require("express");
const app = express();

const axios = require('axios');
let java = require("java");
java.classpath.push("./TopKInsights/out/artifacts/TopKInsights_jar/TopKInsights.jar");

app.use(express.json());
const port = 5000;

app.get('/data-sources', (req, res, next) => {
    axios.get('http://localhost:8081/druid/coordinator/v1/datasources?full')
        .then(resAxios => {
            res.send(resAxios.data);
        })
        .catch(errAxios => {
            next(errAxios.response);
        })
})


app.post('/run', async (req, res, next) => {
    const options = req.body.options;

    let queryColumns = options.columns;
    const index = queryColumns.indexOf(options.measureColumn);
    if (index > -1) queryColumns.splice(index, 1);

    queryColumns.push(options.measureColumn);


    try {
        const axiosResponse = await axios.post('http://localhost:8082/druid/v2', {
            queryType: "groupBy",
            dataSource: options.datasource,
            dimensions: queryColumns,
            granularity: "all",
            intervals: "0/10000"
        });

        const result = axiosResponse.data.map(obj => {
            return obj['event']
        });

        console.log(result);

        const columnTypesResponse = await axios.post('http://localhost:8888/druid/v2/sql', {
            "query": `SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS\n
WHERE TABLE_SCHEMA = 'druid' AND TABLE_NAME = '${options.datasource}'`
        });

        const columns = columnTypesResponse.data.filter(column => {
            return queryColumns.includes(column.COLUMN_NAME);
        });

        console.log(columns);

        java.callStaticMethod("com.kynigopoulos.Main", "getResults",
            JSON.stringify(result), JSON.stringify(columns), options.measureColumn, options.k, options.t,
            function (err, result) {
            if (err) {
                console.log(err);
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
