package com.kynigopoulos;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;


//Creating JSONArrayToArrayList class
public class JSONArrayToArrayList {
    public static void main(String[] args){
        //Creating string of JSON data
        String jsonData = "{\"languages\" : [{\"name\": \"Java\", \"description\":"
                + " \" Java is a class-based high-level programming language that"
                + " follows the OOPs concepts.\"},{\"name\": \"Javascript\","
                + "\"description\": \"JavaScript is also a high-level, often "
                + "just-in-time compiled, and multi-paradigm programming language."
                + "\"},{\"name\": \"Python\",\"description\": \"Python is another "
                + "high-level, interpreted and general-purpose programming language."
                + "\"}]}";

        //Converting jsonData string into JSON object
        JSONObject jsnobject = new JSONObject(jsonData);
        //Printing JSON object
        System.out.println("JSON Object");
        System.out.println(jsnobject);
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
            for (int i=0;i<jsonArray.length();i++){

                //Adding each element of JSON array into ArrayList
                listdata.add(jsonArray.get(i));
            }
        }
        //Iterating ArrayList to print each element

        System.out.println("Each element of ArrayList");
        for(int i=0; i<listdata.size(); i++) {
            //Printing each element of ArrayList
            System.out.println(listdata.get(i));
        }
    }
}