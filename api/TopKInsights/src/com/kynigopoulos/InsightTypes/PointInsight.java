package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Map;

public class PointInsight implements InsightType {
    public PointInsight(){

    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        return 0;
    }
}
