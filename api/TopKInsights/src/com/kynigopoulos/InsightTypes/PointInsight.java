package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.util.FastMath;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PointInsight implements InsightType {

    NormalDistribution distribution;
    public PointInsight(){
        distribution = new NormalDistribution(0, 10);
    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        ArrayList<Double> sortedValues = new ArrayList<>(F.values());
        sortedValues.sort(Collections.reverseOrder());

        for (Double d : sortedValues){
            System.out.print(d + " ");
        }
        System.out.println();

        double lastElement = sortedValues.get(sortedValues.size() - 1);
        if(sortedValues.get(0).equals(lastElement)){
            return 0;
        }
        if(lastElement <= 0){
            for(int i = 0; i < sortedValues.size(); i++){
                sortedValues.set(i, sortedValues.get(i) - lastElement + 1);
            }
        }
        for (Double d : sortedValues){
            System.out.print(d + " ");
        }
        System.out.println();

        double max = sortedValues.remove(0);
        ArrayList<Double> errors = new ArrayList<>();

        SimpleRegression regression = new SimpleRegression();

        for (int i = 0; i < sortedValues.size(); i++){
            double logX = FastMath.log(2, sortedValues.get(i));
            errors.add(logX);
            regression.addData(logX, FastMath.log(2, i + 1));
        }


        double slope = regression.getSlope();
        double intercept = regression.getIntercept();
        System.out.println(slope + " " + intercept);

        for (int i = 0; i < sortedValues.size(); i++){
            errors.set(i, Math.pow(2, intercept + slope * errors.get(i)) - sortedValues.get(i));
        }

        double maxError = Math.pow(2, intercept) * max;
        System.out.println("maxError: " + maxError);

        double p = distribution.cumulativeProbability(max - maxError);
        System.out.println("P value: " + p);


        return p;
    }

    @Override
    public String getType() {
        return "Point";
    }
}
