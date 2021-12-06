package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class PointInsight extends SinglePointInsight {


    public PointInsight() {

    }

    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        try {
            ArrayList<Double> sortedValues = new ArrayList<>(F.values());
            sortedValues.sort(Collections.reverseOrder());

            final double max = sortedValues.remove(0);
            if (max == sortedValues.get(sortedValues.size() - 1)) {
                return .0;
            }

            fitter fitter = new fitter();
            WeightedObservedPoints points = new WeightedObservedPoints();

            for (int i = 0; i < sortedValues.size(); i++) {
                points.add(i + 1, sortedValues.get(i));
            }

            final double[] coefficients = fitter.fit(points.toList());
            final double slope = coefficients[1];
            final double intercept = coefficients[0];

            ArrayList<Double> residuals = new ArrayList<>();
            for (int i = 0; i < sortedValues.size(); i++) {
                double predictedValue = intercept * FastMath.pow(i + 2, slope);
                residuals.add(predictedValue - sortedValues.get(i));
            }
            double xMaxErr = max - intercept;

            double[] doubles = new double[residuals.size()];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = residuals.get(i);
            }

            Mean meanObj = new Mean();
            double mean = meanObj.evaluate(doubles, 0, residuals.size());

            StandardDeviation standardDeviationObj = new StandardDeviation();
            double standardDeviation = standardDeviationObj.evaluate(doubles, mean);
            if (standardDeviation == 0) {
                standardDeviation = Double.MIN_VALUE;
            }

            NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation * 5);

            return normalDistribution.cumulativeProbability(xMaxErr);
        } catch (Exception ignored) {

        }
        return -.01;
    }


    @Override
    public String getType() {
        return "Point";
    }
}