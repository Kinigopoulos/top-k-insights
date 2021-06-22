package com.kynigopoulos;


import java.util.ArrayList;

public class Main {

    public static String[] fromString(String string){
        return string.split(",");
    }

    public static String getResults(
            String data,
            String columns,
            String ordinalColumns,
            String measureColumnName,
            Integer k,
            Integer t,
            String aggregator,
            String extractors,
            String insightTypes
    ) {
        try {
            Database database = JSONController.toDatabase(data, columns, fromString(ordinalColumns), measureColumnName);
            System.out.println("Rows loaded: " + database.size());

            Config.setAggregatorByString(aggregator);
            Config.setExtractorsByString(fromString(extractors));
            Config.setInsightTypesByString(fromString(insightTypes));

            TopKInsights topKInsights = new TopKInsights(database, k, t);
            ArrayList<Insight> insights = topKInsights.getInsights();

            //Wrapping the response in an array and sending it back.
            StringBuilder response = new StringBuilder();
            response.append("[ ");
            for (int i = 0; i < insights.size(); i++) {
                System.out.println(insights.get(i).toString());
                response.append(insights.get(i).toJSONString());
                if (i != insights.size() - 1) response.append(", ");
            }
            response.append(" ]");


            return response.toString();
        } catch (Exception exception){
            exception.printStackTrace();
        }
        return "";
    }

}
