package com.kynigopoulos.Aggregators;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public class MeanAggregator extends Aggregator {

    public Double getOutput(Database database, ArrayList<DataType<?>> subspace, int dimension) {
        double sum = 0;
        int count = 0;
        for (int i = 0; i < database.size(); i++) {
            if (database.belongsToSubspace(subspace, i)) {
                sum += database.getMeasureValue(i).doubleValue();
                count++;
            }
        }
        if (count == 0) {
            return .0;
        }
        return sum / count;
    }

    @Override
    public String toString() {
        return "Mean";
    }

}
