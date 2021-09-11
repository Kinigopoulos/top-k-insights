package com.kynigopoulos.Strategy;

import com.kynigopoulos.CompositeExtractor;
import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;
import com.kynigopoulos.DruidConnection.DruidRequest;

import java.util.ArrayList;

public class DruidStrategy implements Strategy{

    private final DruidRequest druidRequest;
    private final String[] dimensionNames;

    public DruidStrategy(Database database, String[] dimensionNames, String measureName){
        druidRequest = new DruidRequest(""); //TODO ADD BROKER LINK
        druidRequest.setBoilerplateJSON(database.getName(), measureName);
        this.dimensionNames = dimensionNames;
    }


    @Override
    public double getAggregationResult(CompositeExtractor extractor, ArrayList<DataType<?>> subspace, int dimension) {
        druidRequest.aggregationRequest(druidRequest.setWithFilters(dimensionNames, subspace).toString());
        return 0;
    }
}
