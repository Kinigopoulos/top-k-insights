package com.kynigopoulos.Strategy;

import com.kynigopoulos.CompositeExtractor;
import com.kynigopoulos.DataType;


import java.util.ArrayList;

public interface Strategy {

    double getAggregationResult(CompositeExtractor extractor, ArrayList<DataType<?>> subspace, int dimension);


}
