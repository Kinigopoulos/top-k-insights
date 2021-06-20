package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.distribution.LogisticDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.Map;
import java.util.TreeMap;

public class ShapeInsight implements InsightType {

    LogisticDistribution distribution;
    public ShapeInsight(){
        distribution = new LogisticDistribution(0.2, 2);
    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        if(F instanceof TreeMap){
            if(((TreeMap<DataType<?>, Double>) F).firstEntry().getValue().equals(((TreeMap<DataType<?>, Double>) F).lastEntry().getValue())){
                return .0;
            }

            SimpleRegression regression = new SimpleRegression();
            for (Map.Entry<DataType<?>, Double> entry : F.entrySet()) {
                regression.addData(((Number) entry.getKey().getValue()).doubleValue(), entry.getValue());
            }
            double slope = regression.getSlope();
            double prob = distribution.cumulativeProbability(slope);

            System.out.println("Shape p-val: " + (prob * regression.getRSquare()));
            return prob * regression.getRSquare();
        }
        return -.01;
    }

    @Override
    public String getType() {
        return "Shape";
    }
}
