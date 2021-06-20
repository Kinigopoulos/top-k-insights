import React from "react";

function Help(){
    return(
        <div className="openDialogContainer">
            <h2>Extracting Top-K Insights from Multi-Dimensional Data</h2>
            <h3>About</h3>
            <p>
                This project was created for my thesis.
                The repository can be found <a href="https://github.com/Kinigopoulos/top-k-insights" target="_blank" rel="noreferrer">here!</a>
            </p>
            <h3>Executing the algorithm</h3>
            <p>
                To start off, make sure Apache Druid is running.<br/>
                Also make sure that you have set up the correct ports and IPs for the server to connect with Druid.
                <br/>
                The default values are: <br/>
                Broker: http://localhost:8082 <br/>
                Coordinator: http://localhost:8081 <br/>
                Router: http://localhost:8888 <br/>
                Click the <i className="fa fa-cog" aria-hidden="true"/> Settings icon to change them if needed.
            </p>
            <h3>Parameters</h3>
            <ul>
                <li>
                    <h4>DataSource Name</h4>
                    Choose the input data which is stored in your Druid server. You need to add data there if you don't
                    see any option.
                </li>
                <li>
                    <h4>Domain Columns</h4>
                    Choose the columns that you want to include for the extraction. You don't need to choose the
                    measurement column here. <br/> Right of each included column you can select whether the column is ordinal.
                    Ordinal columns are used to generate shape insights.
                </li>
                <li>
                    <h4>Measure Column</h4>
                    Choose the measure/value column of your data.
                </li>
                <li>
                    <h4>Top-K Results</h4>
                    The number of top insights to be extracted. Minimum: 1.
                </li>
                <li>
                    <h4>τ-depth</h4>
                    The depth of the composite extractors. Minimum: 1 uses only the aggregate function.
                </li>
                <li>
                    <h4>Aggregator Function</h4>
                    The aggregate function to be used for the measure column calculations.
                </li>
                <li>
                    <h4>Extractors</h4>
                    The type of extractors to extract data.
                </li>
                <li>
                    <h4>Insight Types</h4>
                    The types of insights to look up.
                </li>
            </ul>
            <p>When you are ready press the Execute button. Your results will get executed and presented.</p>
        </div>
    )
}

export default Help;