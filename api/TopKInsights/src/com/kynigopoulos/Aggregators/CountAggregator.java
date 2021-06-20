package com.kynigopoulos.Aggregators;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public class CountAggregator extends Aggregator {

    public Double getOutput(Database database, ArrayList<DataType<?>> subspace, int dimension){
        double count = 0;
        for(int i = 0; i < database.size(); i++){
            if(database.belongsToSubspace(subspace, i)){
                count++;
            }
        }

        return count;
    }

    @Override
    public String toString() {
        return "Count";
    }

}
