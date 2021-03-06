package com.kynigopoulos.Aggregators;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public class SumAggregator extends Aggregator {

    public Double getOutput(Database database, ArrayList<DataType<?>> subspace, int dimension){
        double sum = 0;
        for(int i = 0; i < database.size(); i++){
            if(database.belongsToSubspace(subspace, i)){
                sum += database.getMeasureValue(i).doubleValue();
            }
        }

        return sum;
    }

    @Override
    public String toString() {
        return "Sum";
    }

}
