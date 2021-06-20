package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;

import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.FastMath;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class PointInsight implements InsightType {


    public PointInsight() {

    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        ArrayList<Double> sortedValues = new ArrayList<>(F.values());
        sortedValues.sort(Collections.reverseOrder());

        for (Double d : sortedValues) {
            System.out.print(d + " ");
        }
        System.out.println();

        double lastElement = sortedValues.get(sortedValues.size() - 1);
        double max = sortedValues.remove(0);
        if (max == lastElement) {
            return 0;
        }

        if(lastElement < 1){
            double inc = -lastElement + 1;
            max += inc;
            for(int i = 0; i < sortedValues.size(); i++){
                sortedValues.set(i, sortedValues.get(i) + inc);
            }
        }

        ArrayList<Double> logX = new ArrayList<>();
        ArrayList<Double> logY = new ArrayList<>();

        for(int i = 0; i < sortedValues.size(); i++){
            logX.add(FastMath.log(2, i + 2));
            logY.add(FastMath.log(2, sortedValues.get(i)));
        }

        System.out.println("Ranking");
        for (int i = 0; i < sortedValues.size(); i++) {
            System.out.println((i + 2) + " " + sortedValues.get(i));

        }

        SimpleRegression simpleRegression = new SimpleRegression();
        for(int i = 0; i < sortedValues.size(); i++){
            simpleRegression.addData(logX.get(i), logY.get(i));
        }
        double slope = simpleRegression.getSlope();
        double intercept = FastMath.pow(2, simpleRegression.getIntercept());

        System.out.println(intercept + "*x^" + slope);

        ArrayList<Double> residuals = new ArrayList<>();
        for(int i = 0; i < sortedValues.size(); i++){
            double predictedValue = intercept * FastMath.pow(i + 2, slope);
            residuals.add(sortedValues.get(i) - predictedValue);
            System.out.println("residual " + (i + 2) + ": " + residuals.get(i));
        }
        double xMaxErr = max - intercept;
        System.out.println("Max Err: " + xMaxErr);


        WeightedObservedPoints obs = new WeightedObservedPoints();
        for(int i = 0; i < residuals.size(); i++){
            obs.add(i + 2, residuals.get(i));
        }

        double[] parameters = GaussianCurveFitter.create().fit(obs.toList());

        System.out.println("Got params: " + parameters[1] + " " + parameters[2]);

        NormalDistribution normalDistribution = new NormalDistribution(parameters[1], parameters[2]);


        return normalDistribution.cumulativeProbability(xMaxErr);
    }

    @Override
    public String getType() {
        return "Point";
    }
}