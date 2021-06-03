package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Extractor {

    public Extractor(){

    }

    public boolean satisfiesRequirements(DataType<?> input){
        return true;
    }

    public boolean isMeaningful(){
        return true;
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value){
        return null;
    };

    @Override
    public String toString() {
        return getClass().toString();
    }
}
