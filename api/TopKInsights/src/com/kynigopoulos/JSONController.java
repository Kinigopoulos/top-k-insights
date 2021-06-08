package com.kynigopoulos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;


public class JSONController {
    public static void main(String[] args) {
        //Creating string of JSON data
        String jsonData = "{\"languages\" : [{\"name\": \"Java\", \"description\":"
                + " \" Java is a class-based high-level programming language that"
                + " follows the OOPs concepts.\"},{\"name\": \"Javascript\","
                + "\"description\": \"JavaScript is also a high-level, often "
                + "just-in-time compiled, and multi-paradigm programming language."
                + "\"},{\"name\": \"Python\",\"description\": \"Python is another "
                + "high-level, interpreted and general-purpose programming language."
                + "\"}], \"yes\": 3}";

        //Converting jsonData string into JSON object
        JSONObject jsnobject = new JSONObject(jsonData);
        //Printing JSON object
        System.out.println("JSON Object");
        System.out.println(jsnobject);
        System.out.println(jsnobject.get("yes"));
        //Getting languages JSON array from the JSON object
        JSONArray jsonArray = jsnobject.getJSONArray("languages");
        //Printing JSON array
        System.out.println("JSON Array");
        System.out.println(jsonArray);
        //Creating an empty ArrayList of type Object
        ArrayList<Object> listdata = new ArrayList<>();

        //Checking whether the JSON array has some value or not
        if (jsonArray != null) {

            //Iterating JSON array
            for (int i = 0; i < jsonArray.length(); i++) {

                //Adding each element of JSON array into ArrayList
                listdata.add(jsonArray.get(i));
            }
        }
        //Iterating ArrayList to print each element

        System.out.println("Each element of ArrayList");
        for (int i = 0; i < listdata.size(); i++) {
            //Printing each element of ArrayList
            System.out.println(listdata.get(i));
        }
    }

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


    public static Database toDatabase(String data, String columns, String measureColumnName) {
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


        Database database = new Database(dimensionNames, domainDimensions, measureDimension);

        for (Object object : jsonArray) {
            JSONObject jsonRow = (JSONObject) object;
            ArrayList<DataType<?>> row = new ArrayList<>();

            for (String dimensionName : dimensionNames) {
                dataTypes type = dimensionTypes.get(dimensionName).getType();

                if(type == dataTypes.String){
                    row.add(new DataType<>(jsonRow.getString(dimensionName)));
                } else if(type == dataTypes.Long){
                    row.add(new DataType<>(jsonRow.getLong(dimensionName)));
                } else if(type == dataTypes.Boolean){
                    row.add(new DataType<>(jsonRow.getBoolean(dimensionName)));
                } else if(type == dataTypes.Double){
                    row.add(new DataType<>(jsonRow.getDouble(dimensionName)));
                } else if(type == dataTypes.Float){
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