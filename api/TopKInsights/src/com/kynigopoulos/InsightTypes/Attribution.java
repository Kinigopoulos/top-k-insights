package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Attribution implements InsightType{
    @Override
    public double getSignificance(Map<DataType<?>, Double> F) {
        try {
            ArrayList<Double> sortedValues = new ArrayList<>(F.values());
            sortedValues.sort(Collections.reverseOrder());

            if (sortedValues.get(sortedValues.size() - 1) < 0) {
                return .0;
            }
            double sum = .0;
            for (double value : sortedValues) {
                sum += value;
            }
            if (sum == 0) {
                return .0;
            }

            double max = sortedValues.get(0);
            double percentage = max / sum;
            NormalDistribution normalDistribution = new NormalDistribution(0.5, 0.25);
            return normalDistribution.cumulativeProbability(percentage);
        } catch (Exception ignored) {

        }
        return -.01;
    }

    @Override
    public String getType() {
        return "Attribution";
    }
}
