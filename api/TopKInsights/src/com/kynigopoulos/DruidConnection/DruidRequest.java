package com.kynigopoulos.DruidConnection;

import com.kynigopoulos.DataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

public class DruidRequest {

    //Default broker URL
    private String brokerURL = "http://localhost:8082/druid/v2";
    private JSONObject jsonObject;

    public DruidRequest(String brokerURL){
        if(!brokerURL.isEmpty()){
            this.brokerURL = brokerURL;
            this.brokerURL += brokerURL.charAt(brokerURL.length() - 1) != '/' ? "/druid/v2" : "druid/v2";
        }
    }

    public void setBoilerplateJSON(String dataSource, String measure){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("queryType", "groupBy");
        jsonObject.put("dataSource", dataSource);
        jsonObject.put("granularity", "all");
        jsonObject.put("intervals", "0/10000");

        JSONArray aggregations = new JSONArray();
        JSONObject aggregationSUM = new JSONObject();
        aggregationSUM.put("type", "doubleSum");
        aggregationSUM.put("name", "total");
        aggregationSUM.put("fieldName", measure);
        aggregations.put(aggregationSUM);
        jsonObject.put("aggregations", aggregations);
        this.jsonObject = jsonObject;
    }

    public JSONObject setWithFilters(String[] dimensions, ArrayList<DataType<?>> values){
        JSONObject jsonObject = new JSONObject(this.jsonObject.toString());

        JSONObject filter = new JSONObject();
        filter.put("type", "and");
        JSONArray array = new JSONArray();
        for(int i = 0; i < values.size(); i++){
            DataType<?> obj = values.get(i);
            if(obj == null){
                continue;
            }
            JSONObject filterObject = new JSONObject();
            filterObject.put("type", "selector");
            filterObject.put("dimension", dimensions[i]);
            filterObject.put("value", obj.getValue());

            array.put(filterObject);
        }
        filter.put("fields", array);

        jsonObject.put("filter", filter);

        return jsonObject;
    }

    public JSONObject noFilters(){
        JSONObject jsonObject = new JSONObject(this.jsonObject.toString());
        jsonObject.remove("filter");
        return jsonObject;
    }

    public double aggregationRequest(String jsonInputString){
        double total = 0;
        try{
            URL url = new URL(brokerURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine = br.readLine();

                String event = responseLine.substring(1, responseLine.length() - 1);
                if(event.equals("")){
                    return .0;
                }

                JSONObject jsonObject = new JSONObject(event);
                total = jsonObject.getJSONObject("event").getDouble("total");
            }
        } catch (Exception e) {
            System.out.println(jsonInputString);
            System.err.println(e);
        }

        return total;
    }
}
