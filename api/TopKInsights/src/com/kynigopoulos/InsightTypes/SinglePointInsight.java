package com.kynigopoulos.InsightTypes;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.util.FastMath;

import java.util.Collection;

public abstract class SinglePointInsight implements InsightType {

    static class function implements ParametricUnivariateFunction {

        @Override
        public double value(double v, double... doubles) {
            return doubles[0] * FastMath.pow(v, doubles[1]);
        }

        @Override
        public double[] gradient(double v, double... doubles) {
            double a = doubles[0];
            double b = doubles[1];

            DerivativeStructure aDev = new DerivativeStructure(2, 1, 0, a);
            DerivativeStructure bDev = new DerivativeStructure(2, 1, 1, b);

            DerivativeStructure y = aDev.multiply(DerivativeStructure.pow(v, bDev));

            return new double[]{
                    y.getPartialDerivative(1, 0),
                    y.getPartialDerivative(0, 1)
            };
        }
    }

    static class fitter extends AbstractCurveFitter {
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


}
