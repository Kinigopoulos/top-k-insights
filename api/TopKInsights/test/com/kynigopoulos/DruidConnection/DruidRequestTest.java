package com.kynigopoulos.DruidConnection;

import com.kynigopoulos.DataType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class DruidRequestTest {

    @Test
    void testSimpleRequest(){
        DruidRequest druidRequest = new DruidRequest("");
        druidRequest.setBoilerplateJSON("sales", "Sales");

        String[] dimensions = new String[]{"Brand", "Year", "Sales"};
        ArrayList<DataType<?>> values = new ArrayList<>();
        values.add(new DataType<>("B"));
        values.add(null);

        String s = druidRequest.setWithFilters(dimensions, values).toString();
        double total = druidRequest.aggregationRequest(s);
        System.out.println(total);
    }



}
