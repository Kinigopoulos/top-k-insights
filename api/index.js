const express = require("express");
const app = express();

const axios = require('axios');
const toArray = require("stream-to-array");

let java = require("java");

app.use(express.json());
const port = 5000;

const getData = async (datasource, columns) => {
    const druidRequesterFactory = require('plywood-druid-requester').druidRequesterFactory;
    const druidRequester = druidRequesterFactory({
        host: 'localhost:8082'
    });

    return toArray(druidRequester({
        query: {
            query: `SELECT ${columns} FROM ${datasource} GROUPBY `
        }
    })).then(res => {
        console.log(res);
    })

}
//getData('sales', `Sales, Brand`)





app.get('/data-sources', (req, res) => {
    axios.get('http://localhost:8081/druid/coordinator/v1/datasources?full')
        .then(resAxios => {
            res.send(resAxios.data);
        })
        .catch(errAxios => {
            res.send(errAxios.response);
        })
})


app.post('/run', async (req, res) => {
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
            return obj['event'];
        });

        console.log(result);
    } catch (err) {
        console.log(err);
        res.end(err.response);
    }




    res.end('hi');
})

app.get('/test', (req, res) => {
    java.classpath.push("./TopKInsights/out/artifacts/TopKInsights_jar/TopKInsights.jar");
    java.callStaticMethod("com.kynigopoulos.Main", "hello", 22, function (err, result) {
        if(err){
            console.log(err);
            return;
        }
        console.log(result);
        res.end(result);
    });
})



app.listen(port, () => {
    console.log(`Listening on port ${port}!`)
});
