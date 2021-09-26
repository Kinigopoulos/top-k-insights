import React, {useEffect, useState} from "react";
import axios from "axios";
import './App.css';
import Graph from "./Graph";
import Help from "./Components/Help";
import Settings from "./Components/Settings";
import {__timeColumns} from "./constants";

function App() {

    const [druidRunning, setDruidRunning] = useState(true);
    const [bypassDruid, setBypassDruid] = useState(false);
    const [openDialog, setOpenDialog] = useState(0);
    const [ports, setPorts] = useState(JSON.parse(localStorage.getItem("ports")) || {
        Broker: "http://localhost:8082",
        Router: "http://localhost:8888"
    });
    const [credentials, setCredentials] = useState(JSON.parse(localStorage.getItem("credentials")) || {
        username: "",
        password: ""
    });
    const getHeadersWithCredentials = () => {
        const headers = {};
        if (credentials.username && credentials.password) {
            headers.username = credentials.username;
            headers.password = credentials.password;
        }
        return headers;
    };

    const aggregators = ["Sum", "Count", "Mean"];
    const defaultExtractors = [
        "PreviousDifferenceExtractor",
        "RankExtractor",
        "PercentageExtractor",
        "AverageDifferenceExtractor"
    ];
    const defaultInsightTypes = [
        "Point",
        "Shape"
    ];
    const filterTypes = [
        "equals",
        "not equals"
    ];

    function MainBody() {
        const [dataSources, setDataSources] = useState([]);
        const [columns, setColumns] = useState([]);
        const [options, setOptions] = useState({
            datasource: "",
            columns: [],
            ordinal: [],
            measureColumn: "",
            k: 1,
            t: 1,
            aggregator: aggregators[0],
            extractors: [...defaultExtractors],
            insightTypes: [...defaultInsightTypes],
            filters: []
        });
        const [isExecuting, setIsExecuting] = useState(false);
        const [dimensions, setDimensions] = useState([]);
        const [insights, setInsights] = useState([]);
        const [numberOfRows, setNumberOfRows] = useState(-1);
        const [selectedInsight, setSelectedInsight] = useState(-1);
        const [loadedInsights, setLoadedInsights] = useState(false);

        useEffect(() => {
            axios.get("/data-sources", {params: {broker: ports.Broker}, headers: getHeadersWithCredentials()})
                .then(res => {
                    setDataSources(res.data);
                }).catch(err => {
                setDruidRunning(false);
                console.log(err);
            });
        }, []);

        useEffect(() => {
            if (loadedInsights) return;
            const name = options.datasource;
            axios.get("/dimensions", {
                params: {broker: ports.Broker, datasource: name},
                headers: getHeadersWithCredentials()
            })
                .then(res => {
                    if (Array.isArray(res.data)) {
                        const columns = res.data.map(column => {
                            if (column === "__time") {
                                return __timeColumns;
                            }
                            return column;
                        }).flat();
                        setColumns(columns);
                    } else {
                        setColumns([]);
                    }
                    setOptions(options => {
                        return {...options, columns: [], ordinal: [], measureColumn: ''}
                    });
                })
                .catch(err => {
                    console.log(err);
                });
        }, [options.datasource, dataSources, loadedInsights])

        function setOption(e) {
            e.preventDefault();
            if (e.target.name === 'datasource' && loadedInsights) {
                setLoadedInsights(false);
            }
            setOptions({...options, [e.target.name]: e.target.value});
        }

        function setNumericOption(e) {
            e.preventDefault();
            setOptions({...options, [e.target.name]: parseInt(e.target.value)})
        }

        function toggleColumn(e) {
            let newColumns = options.columns;
            let newOrdinal = options.ordinal;
            if (e.target.checked) {
                newColumns.push(e.target.value);
            } else {
                const index = newColumns.indexOf(e.target.value);
                if (index > -1) {
                    newColumns.splice(index, 1);
                }
                const ordinalIndex = newOrdinal.indexOf(e.target.value);
                if (ordinalIndex > -1) {
                    newOrdinal.splice(ordinalIndex, 1);
                }
            }
            setOptions({...options, columns: newColumns, ordinal: newOrdinal});
        }

        function toggleOrdinal(e) {
            let newOrdinal = options[e.target.name];
            if (e.target.checked) {
                newOrdinal.push(e.target.value);
            } else {
                const ordinalIndex = newOrdinal.indexOf(e.target.value);
                if (ordinalIndex > -1) {
                    newOrdinal.splice(ordinalIndex, 1);
                }
            }
            setOptions({...options, [e.target.name]: newOrdinal});
        }

        function addFilter(e) {
            e.preventDefault();
            setOptions({...options, filters: [...options.filters, {type: "", dimension: "", value: [""]}]})
        }

        function setFilter(e) {
            e.preventDefault();

            const name = e.target.name.split('-')[0];
            const id = Number.parseInt(e.target.name.split('-')[1]);

            const newFilters = [...options.filters];
            newFilters[id][name] = e.target.value;
            setOptions({...options, filters: newFilters});
        }

        function setFilterValue(e) {
            e.preventDefault();

            const name = Number.parseInt(e.target.name.split('-')[0]);
            const id = Number.parseInt(e.target.name.split('-')[1]);

            const newFilters = [...options.filters];
            newFilters[id].value[name] = e.target.value;
            setOptions({...options, filters: newFilters});
        }

        function addFilterValue(e) {
            e.preventDefault();
            const id = Number.parseInt(e.target.name);
            const value = Number.parseInt(e.target.value);
            const newFilters = [...options.filters];

            if (value === 0) {
                newFilters[id].value.push("");
            } else if (value > 0) {
                newFilters[id].value.splice(value, 1);
            }


            setOptions({...options, filters: newFilters});
        }

        function removeFilter(e) {
            e.preventDefault();
            const id = e.target.id;
            const newFilters = [...options.filters];
            newFilters.splice(id, 1);
            setOptions({...options, filters: newFilters});
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
            } else if (options.extractors.length === 0) {
                window.alert("Extractors' size cannot be 0");
                return;
            } else if (options.insightTypes.length === 0) {
                window.alert("Insight types' size cannot be 0");
                return;
            }
            setIsExecuting(true);
            axios.post("/run", {
                options: options,
                ports: {broker: ports.Broker, router: ports.Router}
            }, {headers: getHeadersWithCredentials()})
                .then(res => {
                    console.log(res.data);
                    setIsExecuting(false);
                    setDimensions(res.data.dimensions);
                    setInsights(res.data.result);
                    setNumberOfRows(res.data.rows);
                })
                .catch(err => {
                    console.log(err.response.data.message);
                    window.alert(err.response.data.message);
                    setIsExecuting(false);
                    setInsights([]);
                });
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

        const downloadFile = async () => {
            const myData = {
                columns: columns,
                options: options,
                rows: numberOfRows,
                dimensions: dimensions,
                insights: insights
            };
            const fileName = `${options.datasource}_insights_${Date.now()}`;

            const json = JSON.stringify(myData);
            const blob = new Blob([json], {type: 'application/json'});
            const href = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = href;
            link.download = fileName + ".json";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        };

        const showFile = async (e) => {
            e.preventDefault()
            const reader = new FileReader()
            reader.onload = async (e) => {
                const {columns, options, rows, dimensions, insights} = JSON.parse((e.target.result).toString());
                setLoadedInsights(true);
                setColumns(columns);
                setOptions(options);
                setNumberOfRows(rows);
                setDimensions(dimensions);
                setInsights(insights);
            };
            reader.readAsText(e.target.files[0])
        };

        return (
            <div className="mainBody">
                <div className="inputDataContainer">
                    <span>DataSource Name</span>
                    <select name="datasource" id="datasource" onChange={setOption} value={options.datasource}>
                        <option value="">--- Select Source ---</option>
                        {dataSources.map((dataSource, key) => {
                            return (
                                <option value={dataSource} key={key}>
                                    {dataSource}
                                </option>
                            )
                        })}
                    </select>

                    <div className="domainColumn">
                        <span>Domain Columns</span>
                        <span>Ordinal</span>
                    </div>

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
                                    <div className={`domainColumn ${key % 2 && 'brighten'}`} key={key}>
                                        <div>
                                            <input type="checkbox" onChange={toggleColumn}
                                                   id={`columns${column}`}
                                                   checked={options.columns.includes(column)}
                                                   name="columns" value={column}/>
                                            &nbsp;
                                            <label htmlFor={`columns${column}`}>{column}</label>
                                        </div>
                                        {options.columns.includes(column) &&
                                        <div>
                                            <input type="checkbox" onChange={toggleOrdinal}
                                                   name="ordinal"
                                                   value={column}
                                            />
                                        </div>
                                        }

                                    </div>
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

                    <span className="mt-1">Aggregator Function</span>
                    <select name="aggregator" id="aggregator" onChange={setOption} value={options.aggregator}>
                        {aggregators.map((column, key) => {
                            return (
                                <option value={column} key={key}>
                                    {column}
                                </option>
                            )
                        })}
                    </select>

                    <span>Extractors</span>
                    <form>
                        {
                            defaultExtractors.map((column, key) => {
                                return (
                                    <div className={`${key % 2 && 'brighten'}`} key={key}>
                                        <div>
                                            <input type="checkbox" onChange={toggleOrdinal}
                                                   id={`extractor${column}`}
                                                   checked={options.extractors.includes(column)}
                                                   name="extractors"
                                                   value={column}/>
                                            &nbsp;
                                            <label htmlFor={`extractor${column}`}>{column}</label>
                                        </div>
                                    </div>
                                )
                            })
                        }
                    </form>
                    <br/>

                    <span>Insight Types</span>
                    <form>
                        {
                            defaultInsightTypes.map((column, key) => {
                                return (
                                    <div className={`${key % 2 && 'brighten'}`} key={key}>
                                        <div>
                                            <input type="checkbox" onChange={toggleOrdinal}
                                                   id={`insightType${column}`}
                                                   checked={options.insightTypes.includes(column)}
                                                   name="insightTypes"
                                                   value={column}/>
                                            &nbsp;
                                            <label htmlFor={`insightType${column}`}>{column}</label>
                                        </div>
                                    </div>
                                )
                            })
                        }
                    </form>
                    <br/>

                    <span>Filters</span>
                    <form>
                        {
                            options.filters.map((filter, key) => {
                                return (
                                    <React.Fragment key={key}>
                                        <span>Filter No. {key + 1} <span className="filterRemove" onClick={removeFilter}
                                                                         id={key}>Remove</span></span>
                                        <div className="filterContainer">
                                            <select onChange={setFilter} name={`dimension-${key}`}
                                                    value={filter.dimension}>
                                                <option value={''}>--- Select Dimension ---</option>
                                                {columns.map((column, key) => {
                                                    return (
                                                        <option value={column} key={key}>
                                                            {column}
                                                        </option>
                                                    )
                                                })}
                                            </select>

                                            <select onChange={setFilter} className="filterType" name={`type-${key}`}
                                                    value={filter.type}>
                                                <option value={''}>--- Select Type ---</option>
                                                {filterTypes.map((type, key) => {
                                                    return (
                                                        <option value={type} key={key}>
                                                            {type}
                                                        </option>
                                                    )
                                                })}
                                            </select>
                                        </div>
                                        {
                                            filter.value.map((val, key2) => {
                                                return (
                                                    <div className="filterValueContainer" key={key2}>
                                                        <input className="filterValue" name={`${key2}-${key}`}
                                                               onChange={setFilterValue}
                                                               value={val}/>
                                                        {
                                                            filter.type === "equals" && (key2 === 0 ?
                                                                <button className="addOrFilterButton" value={key2}
                                                                        title="Add OR condition" name={key}
                                                                        onClick={addFilterValue}>
                                                                    <span style={{pointerEvents: "none"}}>+</span>
                                                                </button>
                                                                :
                                                                <button
                                                                    className="addOrFilterButton removeOrFilterButton"
                                                                    value={key2} title="Remove OR condition" name={key}
                                                                    onClick={addFilterValue}>
                                                                    <span style={{pointerEvents: "none"}}>-</span>
                                                                </button>)
                                                        }
                                                    </div>
                                                )
                                            })
                                        }
                                    </React.Fragment>
                                )
                            })
                        }
                        <button onClick={addFilter} title="Add a filter">+</button>
                    </form>
                    <br/>

                    <button onClick={ExecuteQuery}>Execute</button>
                </div>

                <div className="resultsContainer">
                    {isExecuting && <h1>Loading</h1>}
                    <div style={{
                        display: "flex", justifyContent: "space-between",
                        alignItems: "center", marginBottom: "0.4rem"
                    }}>
                        {
                            numberOfRows > 0 ?
                                <>
                                    <h3 style={{margin: 0}}>Number of documents/rows: {numberOfRows}</h3>
                                    <button style={{margin: 0}} onClick={downloadFile}>
                                        <i className="fa fa-download"/> Save insights
                                    </button>
                                </>
                                :
                                <>
                                    <div/>
                                    <button style={{margin: 0}}
                                            onClick={() => document.getElementById('fileOpen').click()}>
                                        <i className="fa fa-upload"/> Load insights
                                    </button>

                                    <input type="file" id="fileOpen" onChange={showFile} style={{display: "none"}}/>
                                </>
                        }
                    </div>
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
                                        <div className="insightRow insightRowObj"
                                             onClick={() => changeSelectedInsight(key)}>
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

    function ErrorMessage({setBypassDruid}) {
        return (
            <div className="errorMessage">
                <span className="errorMessageText">
                    Make sure that your Druid server is running and refresh the page!
                </span>
                <button className="errorMessageButton" onClick={() => setBypassDruid(true)}>Run saved insights</button>
            </div>
        );
    }

    function WindowComponent({children}) {
        return (
            <div className="windowDialog" key="WindowComponent">
                <div className="windowDialogCloseContainer" onClick={() => setOpenDialog(0)}>
                    <i className="fa fa-times windowDialogClose" aria-hidden="true"/>
                </div>
                {children}
            </div>
        )
    }

    return (
        <div className="mainContainer">
            <header className="header">
                <span className="headerTitle">Top-K Insights Extractor</span>
                <div style={{display: "flex", marginRight: "2rem"}}>
                    <div className="headerHelpIcon" onClick={() => setOpenDialog(2)}>
                        <i className="fa fa-cog" aria-hidden="true"/>
                    </div>

                    <div className="headerHelpIcon" onClick={() => setOpenDialog(1)}>
                        <i className="fa fa-question-circle" aria-hidden="true"/>
                    </div>
                </div>
            </header>

            {openDialog === 1 && <WindowComponent children={<Help/>}/>}
            {openDialog === 2 && <WindowComponent
                children={<Settings ports={ports} setPorts={setPorts} credentials={credentials}
                                    setCredentials={setCredentials}/>}/>}

            <div className={`mainBody ${openDialog !== 0 && "disabledMainBody"}`}>
                {(druidRunning || bypassDruid) ? <MainBody/> : <ErrorMessage setBypassDruid={setBypassDruid}/>}
            </div>
        </div>
    );
}

export default App;
