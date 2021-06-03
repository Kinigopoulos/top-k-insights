package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PercentageExtractor extends Extractor {

    public PercentageExtractor() {
        super();
    }

    @Override
    public boolean isMeaningful() {
        return false;
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        double sum = 0;
        for(Double measure : input.values()){
            sum += measure;
        }

        return input.get(value) / sum;
    }

    @Override
    public String toString() {
        return "PercentageExtractor";
    }
}
