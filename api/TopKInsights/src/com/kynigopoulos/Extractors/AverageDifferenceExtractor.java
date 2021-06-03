package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AverageDifferenceExtractor extends Extractor{

    public AverageDifferenceExtractor() {
        super();
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        double avg = 0;
        for(Double measure : input.values()){
            avg += measure;
        }

        avg /= input.size();
        return input.get(value) - avg;
    }

    @Override
    public String toString() {
        return "AverageDifferenceExtractor";
    }
}
