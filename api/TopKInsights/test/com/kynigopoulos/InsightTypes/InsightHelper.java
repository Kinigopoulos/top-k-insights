package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class InsightHelper {

    public static Map<DataType<?>, Double> toMapResultSet(double[] values, boolean isTreeMap){
        Map<DataType<?>, Double> set = new HashMap<>();
        if(isTreeMap){
            set = new TreeMap<>();
        }
        int key = 2010;
        for(double value : values){
            set.put(new DataType<>(key), value);
            key++;
        }
        return set;
    }

}
