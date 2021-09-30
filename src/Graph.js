import React from "react";
import {Bar, Line} from "react-chartjs-2";

function Graph({insight, siblingGroup, extractor, measureLabel, dimensions}) {

    function generateData(data){
        let result = {labels: [], data: []}
        for(const [key, value] of Object.entries(data)){
            result.labels.push(key);
            result.data.push(value);
        }
        return result;
    }
    const set = generateData(insight.resultSet);


    const data = {
        labels: set.labels,
        datasets: [
            {
                label: measureLabel,
                data: set.data,
                backgroundColor: [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(255, 159, 64, 0.2)',
                ],
                borderColor: [
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)',
                ],
                borderWidth: 2,
            },
        ],
    };

    const options = {

    };

    const getChart = () => {
        if(insight.insightType === 'Shape'){
            return (
                <Line className="mainColor" data={data} options={options}/>
            )
        } else {
            return (
                <Bar data={data} options={options}/>
            )
        }
    }

    return (
        <div className="graphContainer">
            <h4>Type: {insight.insightType}</h4>
            <h4>Sibling Group: SG({'<'}{siblingGroup.map((sg, key) => {
                return (<span title={sg.title} key={key}>{sg.value + (key === siblingGroup.length -1 ? "" : ",")}</span>)
            })}{'>'}, {dimensions[insight.dimension]})</h4>
            <h4>Extractor: {extractor}</h4>
            <h4>Score: {insight.value}</h4>
            <div className="chartContainer">
                {getChart()}
            </div>

        </div>
    )
}

export default Graph;