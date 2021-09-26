package com.kynigopoulos.Aggregators;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public class MeanAggregator extends Aggregator {

    public Double getOutput(Database database, ArrayList<DataType<?>> subspace, int dimension){
        try{
            double sum = 0;
            int count = 0;
            for(int i = 0; i < database.size(); i++){
                if(database.belongsToSubspace(subspace, i)){
                    sum += database.getMeasureValue(i).doubleValue();
                    count++;
                }
            }

            return sum / count;
        } catch (Exception ignored) {

        }
        return .0;
    }

    @Override
    public String toString() {
        return "Mean";
    }

}
