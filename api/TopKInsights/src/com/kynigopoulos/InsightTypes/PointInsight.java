package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.GaussianCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.FastMath;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Collection;
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

        double max = sortedValues.remove(0);


        class function implements ParametricUnivariateFunction{

            @Override
            public double value(double v, double... doubles) {
                return doubles[0] * FastMath.pow(v, doubles[1]);
            }

            @Override
            public double[] gradient(double v, double... doubles) {
                double a = doubles[0];
                double b = doubles[1];

                DerivativeStructure aDev = new DerivativeStructure(3, 1, 0, a);
                DerivativeStructure bDev = new DerivativeStructure(3, 1, 1, b);

                DerivativeStructure y = aDev.multiply(DerivativeStructure.pow(v, bDev));

                return new double[] {
                        y.getPartialDerivative(1, 0, 0),
                        y.getPartialDerivative(0, 1, 0)
                };
            }
        }

        class fitter extends AbstractCurveFitter {
            protected LeastSquaresProblem getProblem(Collection<WeightedObservedPoint> points) {
                final int len = points.size();
                final double[] target = new double[len];
                final double[] weights = new double[len];
                final double[] initialGuess = {1.0, -1.0};

                int i = 0;
                for (WeightedObservedPoint point : points) {
                    target[i] = point.getY();
                    weights[i] = point.getWeight();
                    i += 1;
                }

                final AbstractCurveFitter.TheoreticalValuesFunction model = new
                        AbstractCurveFitter.TheoreticalValuesFunction(new function(), points);

                return new LeastSquaresBuilder().
                        maxEvaluations(Integer.MAX_VALUE).
                        maxIterations(Integer.MAX_VALUE).
                        start(initialGuess).
                        target(target).
                        weight(new DiagonalMatrix(weights)).
                        model(model.getModelFunction(), model.getModelFunctionJacobian()).
                        build();
            }
        }


        fitter fitter = new fitter();
        WeightedObservedPoints points = new WeightedObservedPoints();

        for(int i = 0; i < sortedValues.size(); i++){
            points.add(i + 1, sortedValues.get(i));
        }

        final double[] coeffs = fitter.fit(points.toList());
        System.out.println("++++++++++++++++++++++++++++++++++++");
        System.out.println(coeffs[0] + " " + coeffs[1]);
        System.out.println("++++++++++++++++++++++++++++++++++++");

        double slope = coeffs[1];
        double intercept = coeffs[0];


        System.out.println(intercept + "*x^" + slope);

        ArrayList<Double> residuals = new ArrayList<>();
        for(int i = 0; i < sortedValues.size(); i++){
            double predictedValue = intercept * FastMath.pow(i + 2, slope);
            residuals.add(-sortedValues.get(i) + predictedValue);
        }
        double xMaxErr = max - intercept;
        System.out.println("Max Err: " + xMaxErr);


        double[] doubles = new double[residuals.size()];
        for(int i = 0; i < doubles.length; i++){
            doubles[i] = residuals.get(i);
        }

        Mean meanObj = new Mean();
        double mean = meanObj.evaluate(doubles, 0, residuals.size());
        System.out.println("MEAN: " + mean);

        StandardDeviation standardDeviationObj = new StandardDeviation();
        double standardDeviation = standardDeviationObj.evaluate(doubles, mean);
        System.out.println("SD: " + standardDeviation);

        NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);


        return normalDistribution.cumulativeProbability(xMaxErr);
    }


    @Override
    public String getType() {
        return "Point";
    }
}