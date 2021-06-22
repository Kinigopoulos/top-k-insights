package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;

import java.util.Map;

public class RankExtractor extends Extractor {

    public RankExtractor() {
        super();
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        double rank = 0;
        for(Double measure : input.values()){
            if(measure >= input.get(value)){
                rank += 1;
            }
        }

        return rank;
    }

    @Override
    public String toString() {
        return "RankExtractor";
    }
}