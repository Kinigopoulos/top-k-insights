package com.kynigopoulos.Strategy;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.CompositeExtractor;
import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;

public class LocalStrategy implements Strategy{

    private final Database database;

    public LocalStrategy(Database database){
        this.database = database;
    }

    @Override
    public double getAggregationResult(CompositeExtractor extractor, ArrayList<DataType<?>> subspace, int dimension) {
        return ((Aggregator)CompositeExtractor.getAggregator(extractor)).getOutput(database, subspace, dimension);
    }
}
