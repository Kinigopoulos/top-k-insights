package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;

import java.util.Map;

public interface InsightType {
    double getSignificance(Map<DataType<?>, Double> F);
}
