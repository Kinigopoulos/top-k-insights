package com.kynigopoulos.Aggregators;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public abstract class Aggregator {

    public Aggregator(){

    }

    public Double getOutput(Database database, ArrayList<DataType<?>> subspace, int dimension){
        return .0;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
