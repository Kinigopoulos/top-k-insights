import React, {useEffect, useState} from "react";
import axios from "axios";
import './App.css';

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
            setOptions(options => {return {...options, columns: [], measureColumn: ''}});
        }, [options.datasource, dataSources])

        function setOption(e) {
            e.preventDefault();
            setOptions({...options, [e.target.name]: e.target.value});
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

            axios.post("/run", {options})
                .then(res => {
                    console.log(res.data);
                }).catch(err => {
                    console.log(err)
                })
        }

        console.log(options);

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
                    <input className="" name="k" type="number" onChange={setOption} value={options.k}/>

                    <span>τ-depth</span>
                    <input className="" name="t" type="number" onChange={setOption} value={options.t}/>

                    <button onClick={ExecuteQuery}>Execute</button>
                </div>

                <div className="resultsContainer">
                    hello
                </div>
            </div>
        );
    }

    function ErrorMessage() {
        return (
            <div className="errorMessage">
                <span className="errorMessageText">Make sure that your Druid server is running!</span>
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
