import React, {useEffect, useState} from "react";
import axios from "axios";
import './App.css';
import Graph from "./Graph";

function App() {

    const [druidRunning, setDruidRunning] = useState(true);

    function MainBody() {
        const [dataSources, setDataSources] = useState([]);
        const [columns, setColumns] = useState([]);
        const [options, setOptions] = useState({
            datasource: "",
            columns: [],
            measureColumn: "",
            k: 1,
            t: 1
        });
        const [isExecuting, setIsExecuting] = useState(false);
        const [dimensions, setDimensions] = useState([]);
        const [insights, setInsights] = useState([]);
        const [selectedInsight, setSelectedInsight] = useState(-1);

        useEffect(() => {
            axios.get("/data-sources").then(res => {
                console.log(res);
                setDataSources(res.data);
            }).catch(err => {
                setDruidRunning(false);
                console.log(err);
            })
        }, []);

        useEffect(() => {
            const name = options.datasource;
            let columns = [];
            if (name !== '') {
                dataSources.forEach(dataSource => {
                    if (dataSource.name !== name) return;
                    dataSource.segments.forEach(segment => {
                        const dimensions = segment['dimensions'].split(',');
                        dimensions.forEach(dimension => {
                            columns.push(dimension);
                        })
                    })
                })
            }
            setColumns(columns);
            setOptions(options => {
                return {...options, columns: [], measureColumn: ''}
            });
        }, [options.datasource, dataSources])

        function setOption(e) {
            e.preventDefault();
            setOptions({...options, [e.target.name]: e.target.value});
        }

        function setNumericOption(e) {
            e.preventDefault();
            setOptions({...options, [e.target.name]: parseInt(e.target.value)})
        }

        function toggleColumn(e) {
            let newColumns = options.columns;
            if (e.target.checked) {
                newColumns.push(e.target.value);
            } else {
                const index = newColumns.indexOf(e.target.value);
                if (index > -1) newColumns.splice(index, 1);
            }
            setOptions({...options, columns: newColumns});
        }

        const ExecuteQuery = () => {
            if (options.datasource === "") {
                window.alert("Please choose a valid datasource");
                return;
            } else if (options.columns.length === 0) {
                window.alert("Domain columns' size cannot be 0");
                return;
            } else if (options.measureColumn === "") {
                window.alert("Please choose a valid measure column");
                return;
            } else if (options.k <= 0) {
                window.alert("K must be a positive number");
                return;
            } else if (options.t <= 0) {
                window.alert("τ must be a positive number");
                return;
            }
            setIsExecuting(true);
            axios.post("/run", {options})
                .then(res => {
                    console.log(res.data);
                    setIsExecuting(false);
                    setDimensions(() => {
                        let columns = options.columns.sort();
                        const index = columns.indexOf(options.measureColumn);
                        if (index > -1) columns.splice(index, 1);
                        columns.push(options.measureColumn);
                        return columns;
                    })
                    setInsights(res.data);
                }).catch(err => {
                console.log(err);
            })
        }

        const extractorToString = data => {
            const extractorSign = ex => {
                switch (ex) {
                    case 'PreviousDifferenceExtractor':
                        return '∆prev'
                    case 'RankExtractor':
                        return 'Rank'
                    case 'PercentageExtractor':
                        return '%'
                    case 'AverageDifferenceExtractor':
                        return '∆avg'
                    default:
                        return ex;
                }
            }
            let string = "<";
            data.forEach((e, key) => {
                let comma = ", ";
                if (key === data.length - 1) {
                    comma = "";
                }

                string += `(${extractorSign(e.type)}, ${dimensions[e.dimension]})${comma}`

            })
            string += ">";
            return string;
        }

        function changeSelectedInsight(key) {
            if (selectedInsight === key) {
                setSelectedInsight(-1);
            } else {
                setSelectedInsight(key);
            }
        }

        return (
            <div className="mainBody">
                <div className="inputDataContainer">
                    <span>DataSource Name</span>
                    <select name="datasource" id="datasource" onChange={setOption} value={options.datasource}>
                        <option value="">--- Select Source ---</option>
                        {dataSources.map((dataSource, key) => {
                            return (
                                <option value={dataSource.name} key={key}>
                                    {dataSource.name}
                                </option>
                            )
                        })}
                    </select>

                    <span>Domain Columns</span>
                    {columns.length === 0 &&
                    <>
                        <br/>
                        <span>Select a datasource first</span>
                    </>
                    }
                    <form>
                        {
                            columns.map((column, key) => {
                                return (
                                    <React.Fragment key={key}>
                                        <input type="checkbox" onChange={toggleColumn}
                                               id={`columns${column}`}
                                               checked={options.columns.includes(column)}
                                               name="columns" value={column}/>
                                        <label htmlFor={`columns${column}`}>{column}</label><br/>
                                    </React.Fragment>
                                )
                            })
                        }
                    </form>
                    <br/>

                    <span className="mt-1">Measure Column</span>
                    <select name="measureColumn" id="measureColumn" onChange={setOption} value={options.measureColumn}>
                        <option value={''}>--- Select Measurement ---</option>
                        {columns.map((column, key) => {
                            return (
                                <option value={column} key={key}>
                                    {column}
                                </option>
                            )
                        })}
                    </select>


                    <span>Top-K Results</span>
                    <input className="" name="k" type="number" onChange={setNumericOption} value={options.k}/>

                    <span>τ-depth</span>
                    <input className="" name="t" type="number" onChange={setNumericOption} value={options.t}/>

                    <button onClick={ExecuteQuery}>Execute</button>
                </div>

                <div className="resultsContainer">
                    {isExecuting && <h1>Loading</h1>}
                    {insights.length > 0 &&
                    <div>
                        <div className="insightRow" style={{marginBottom: "0.2rem"}}>
                            <span className="insightCell">Insight Type</span>
                            <span className="insightCell">Sibling Group</span>
                            <span className="insightCell">Extractor</span>
                            <span className="insightCell">Score</span>
                        </div>
                        {
                            insights.map((insight, key) => {
                                let siblingGroup = "<";
                                insight.subspace.forEach((dim, key) => {
                                    siblingGroup += dim === null ? "*" : dim;
                                    if (key !== insight.subspace.length - 1) {
                                        siblingGroup += ", ";
                                    }
                                });
                                siblingGroup += ">";
                                let extractorString = extractorToString(insight.extractor);

                                return (
                                    <React.Fragment key={key}>
                                        <div className="insightRow" onClick={() => changeSelectedInsight(key)}>
                                            <span className="insightCell">
                                                {insight.insightType}
                                            </span>

                                            <span className="insightCell">
                                                SG({siblingGroup}, {dimensions[insight.dimension]})
                                            </span>

                                            <span className="insightCell">
                                                {extractorString}
                                            </span>

                                            <span className="insightCell">
                                                {(Math.round(insight.value * 100) / 100).toFixed(3)}
                                            </span>

                                        </div>
                                        {
                                            selectedInsight === key &&
                                            <Graph insight={insight}
                                                   siblingGroup={siblingGroup}
                                                   extractor={extractorString}
                                                   measureLabel={options.measureColumn}/>
                                        }
                                    </React.Fragment>
                                )
                            })
                        }
                    </div>
                    }
                </div>
            </div>
        );
    }

    function ErrorMessage() {
        return (
            <div className="errorMessage">
                <span className="errorMessageText">
                    Make sure that your Druid server is running and refresh the page!
                </span>
            </div>
        );
    }

    return (
        <div className="mainContainer">
            <header className="header">
                <span className="headerTitle">Top-K Insights Extractor</span>
                <div className="headerHelpIcon">
                    <i className="fa fa-question-circle" aria-hidden="true"/>
                </div>
            </header>

            <div className="mainBody">
                {druidRunning ? <MainBody/> : <ErrorMessage message="Hi"/>}
            </div>
        </div>
    );
}

export default App;
