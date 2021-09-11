package com.kynigopoulos;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;


public class JSONController {

    enum dataTypes {
        String, Long, Boolean, Double, Float
    }

    static class TypeValue {
        final private dataTypes type;

        TypeValue(dataTypes type) {
            this.type = type;
        }

        public dataTypes getType() {
            return type;
        }
    }

    private static HashMap<String, TypeValue> extractTypes(JSONArray columnsArray) {
        HashMap<String, TypeValue> dimensionTypes = new HashMap<>();

        for (Object column : columnsArray) {
            JSONObject object = (JSONObject) column;
            TypeValue type;
            switch (object.getString("DATA_TYPE")) {
                case "CHAR":
                case "VARCHAR": {
                    type = new TypeValue(dataTypes.String);
                    break;
                }
                case "DECIMAL":
                case "REAL":
                case "DOUBLE": {
                    type = new TypeValue(dataTypes.Double);
                    break;
                }
                case "BOOLEAN": {
                    type = new TypeValue(dataTypes.Boolean);
                    break;
                }
                case "FLOAT": {
                    type = new TypeValue(dataTypes.Float);
                    break;
                }
                default: {
                    type = new TypeValue(dataTypes.Long);
                    break;
                }
            }

            dimensionTypes.put(object.getString("COLUMN_NAME"), type);
        }

        return dimensionTypes;
    }


    public static Database toDatabase(String data, String columns, String[] ordinal, String measureColumnName, String datasource) {
        JSONArray jsonArray = new JSONArray(data);
        JSONArray columnsArray = new JSONArray(columns);

        System.out.println("LENGTH OF COLUMNS: " + columnsArray.length());

        //Excluding measure column so that's why we subtract 1.
        int[] domainDimensions = new int[columnsArray.length() - 1];
        String[] dimensionNames = new String[columnsArray.length()];
        HashMap<String, TypeValue> dimensionTypes = extractTypes(columnsArray);
        int measureDimension = dimensionNames.length - 1;
        for (int i = 0; i < domainDimensions.length; i++) {
            domainDimensions[i] = i;
        }

        int index = 0;
        for (Object column : columnsArray) {
            JSONObject object = (JSONObject) column;
            String columnName = object.getString("COLUMN_NAME");
            if (columnName.equals("__time")) {
                continue;
            } else if (columnName.equals(measureColumnName)) {
                dimensionNames[measureDimension] = columnName;
                continue;
            } else {
                dimensionNames[index] = columnName;
            }
            index++;
        }

        for (Map.Entry<String, TypeValue> d : dimensionTypes.entrySet()) {
            System.out.print(d.getKey() + ": " + d.getValue().getType().name());
        }
        System.out.println("\nDimensions Length" + dimensionNames.length);
        for(String d : dimensionNames){
            System.out.println(d);
        }

        HashSet<String> ordinalSet = new HashSet<>(Arrays.asList(ordinal));
        boolean[] ordinalDimensions = new boolean[dimensionNames.length - 1];
        for(int i = 0; i < ordinalDimensions.length; i++){
            if(ordinalSet.contains(dimensionNames[i])){
                ordinalDimensions[i] = true;
            }
        }

        Database database = new Database(dimensionNames, domainDimensions, measureDimension, ordinalDimensions, datasource);

        for (Object object : jsonArray) {
            JSONObject jsonRow = (JSONObject) object;
            ArrayList<DataType<?>> row = new ArrayList<>();

            for (String dimensionName : dimensionNames) {
                dataTypes type = dimensionTypes.get(dimensionName).getType();

                if(type == dataTypes.String){
                    if(jsonRow.get(dimensionName) == null){
                        row.add(new DataType<>(""));
                    }
                    row.add(new DataType<>(jsonRow.getString(dimensionName)));
                } else if(type == dataTypes.Long){
                    if(jsonRow.get(dimensionName) == null){
                        row.add(new DataType<>(0));
                    }
                    row.add(new DataType<>(jsonRow.getLong(dimensionName)));
                } else if(type == dataTypes.Boolean){
                    if(jsonRow.get(dimensionName) == null){
                        row.add(new DataType<>(false));
                    }
                    row.add(new DataType<>(jsonRow.getBoolean(dimensionName)));
                } else if(type == dataTypes.Double){
                    if(jsonRow.get(dimensionName) == null){
                        row.add(new DataType<>(.0));
                    }
                    row.add(new DataType<>(jsonRow.getDouble(dimensionName)));
                } else if(type == dataTypes.Float){
                    if(jsonRow.get(dimensionName) == null){
                        row.add(new DataType<>(.0));
                    }
                    row.add(new DataType<>(jsonRow.getFloat(dimensionName)));
                } else {
                    System.out.println("Unsupported type was not added...");
                }

            }

            database.addRow(row);
        }


        return database;
    }

}