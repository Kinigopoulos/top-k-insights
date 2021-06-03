package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.distribution.LogisticDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Map;

public class ShapeInsight implements InsightType {

    LogisticDistribution distribution;
    public ShapeInsight(){
        distribution = new LogisticDistribution(5, 2);
    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        SimpleRegression regression = new SimpleRegression();
        for (Map.Entry<DataType<?>, Double> entry : F.entrySet()) {
            regression.addData(((Number) entry.getKey().getValue()).doubleValue(), entry.getValue());
        }
        double slope = regression.getSlope();
        double prob = distribution.cumulativeProbability(slope);

        return prob * regression.getRSquare();
    }
}
