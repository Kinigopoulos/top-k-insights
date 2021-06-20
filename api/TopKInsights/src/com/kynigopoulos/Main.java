package com.kynigopoulos;


import java.util.ArrayList;

public class Main {

    public static ArrayList<DataType<?>> makeRow(int year, char brand, int sales) {
        ArrayList<DataType<?>> row = new ArrayList<>();

        row.add(new DataType<>(year));
        row.add(new DataType<>(brand));
        row.add(new DataType<>(sales));

        return row;
    }

    public static void main(String[] args) {
        int[] domainDimensions = new int[]{0, 1};
        int measureDimension = 2;
        boolean[] ordinal = new boolean[]{true, false, true};
        Database database = new Database(new String[]{"Year", "Brand", "Sales"}, domainDimensions, measureDimension, ordinal);

        database.addRow(makeRow(2010, 'F', 13));
        database.addRow(makeRow(2011, 'F', 10));
        database.addRow(makeRow(2012, 'F', 14));
        database.addRow(makeRow(2013, 'F', 23));
        database.addRow(makeRow(2014, 'F', 27));
        //--
        database.addRow(makeRow(2010, 'B', 20));
        database.addRow(makeRow(2011, 'B', 18));
        database.addRow(makeRow(2012, 'B', 20));
        database.addRow(makeRow(2013, 'B', 17));
        database.addRow(makeRow(2014, 'B', 19));
        //--
        database.addRow(makeRow(2010, 'H', 40));
        database.addRow(makeRow(2011, 'H', 35));
        database.addRow(makeRow(2012, 'H', 36));
        database.addRow(makeRow(2013, 'H', 43));
        database.addRow(makeRow(2014, 'H', 58));
        //--
        database.addRow(makeRow(2010, 'T', 38));
        database.addRow(makeRow(2011, 'T', 34));
        database.addRow(makeRow(2012, 'T', 34));
        database.addRow(makeRow(2013, 'T', 29));
        database.addRow(makeRow(2014, 'T', 36));

        int t = 2;


        TopKInsights topKInsights = new TopKInsights(database, 3, t);
        ArrayList<Insight> insights = topKInsights.getInsights();

        ArrayList<CompositeExtractor> extractors = CompositeExtractor.findCombinations(database, t);
        System.out.println("Size of possible extractors: " + extractors.size());
        for (CompositeExtractor extractor : extractors) {
            System.out.print("<");
            for (int i = 0; i < t; i++) {
                ExtractorPair<?> extractorPair = extractor.getPair(i);
                System.out.print("(" + extractorPair.getType().toString()
                        + ", " + database.getDimensionName(extractorPair.getDimension()) + ")");
            }
            System.out.println(">");
        }

        for (Insight insight : insights) {
            //System.out.println(insight.toString());
            System.out.println(insight.toJSONString());
        }

    }

    public static String getResults(
            String data,
            String columns,
            String[] ordinalColumns,
            String measureColumnName,
            Integer k,
            Integer t,
            String aggregator,
            String[] extractors,
            String[] insightTypes
    ) {
        try {
            Database database = JSONController.toDatabase(data, columns, ordinalColumns, measureColumnName);

            Config.setAggregatorByString(aggregator);
            Config.setExtractorsByString(extractors);
            Config.setInsightTypesByString(insightTypes);

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
            System.err.println(exception);
        }
        return "";
    }

}
