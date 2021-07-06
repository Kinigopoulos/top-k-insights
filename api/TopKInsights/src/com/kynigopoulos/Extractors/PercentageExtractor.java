package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;

import java.util.Map;

public class PercentageExtractor extends Extractor {

    public PercentageExtractor() {
        super();
    }

    @Override
    public boolean isMeaningful(Extractor extractor) {
        return false;
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        double sum = 0;
        for(Double measure : input.values()){
            sum += measure;
        }
        if (sum == 0){
            return .0;
        }

        return input.get(value) / sum;
    }

    @Override
    public String toString() {
        return "PercentageExtractor";
    }
}
