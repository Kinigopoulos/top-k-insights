package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;
import com.kynigopoulos.Database;

import java.util.*;

public class PreviousDifferenceExtractor extends Extractor{

    public PreviousDifferenceExtractor() {
        super();
    }

    @Override
    public boolean satisfiesRequirements(DataType<?> input) {
        return input.isOrdinal();
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        DataType<?> previousValue = ((TreeMap<DataType<?>, Double>) input).lowerKey(value);
        if(previousValue == null){
            return null;
        }

        return input.get(value) - input.get(previousValue);
    }

    @Override
    public String toString() {
        return "PreviousDifferenceExtractor";
    }
}
