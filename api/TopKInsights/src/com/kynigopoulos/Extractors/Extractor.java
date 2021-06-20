package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.Map;

public abstract class Extractor {

    public Extractor(){

    }

    public boolean satisfiesRequirements(Database database, int dimension){
        return true;
    }

    public boolean isMeaningful(){
        return true;
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value){
        return null;
    }

    @Override
    public String toString() {
        return getClass().toString();
    }
}
