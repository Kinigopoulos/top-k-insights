package com.kynigopoulos.Extractors;

import com.kynigopoulos.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RankExtractor extends Extractor {

    public RankExtractor() {
        super();
    }

    public static ArrayList<?> getOutput(ArrayList<?> input) {

        ArrayList<Tuple> rankList = new ArrayList<>(input.size());
        ArrayList<Integer> result = new ArrayList<>(input.size());

        for(int i = 0; i < input.size(); i++){
            double value = ((Number) input.get(i)).doubleValue();

            rankList.add(new Tuple(value, i));
            result.add(0);
        }
        Collections.sort(rankList);

        for(int i = 0; i < rankList.size(); i++){
            result.set(rankList.get(i).position, i + 1);
            System.out.println(rankList.get(i).value + " -> " + rankList.get(i).position);
        }

        return result;
    }

    public Double getOutput(Map<DataType<?>, Double> input, DataType<?> value) {
        double rank = 1;
        for(Double measure : input.values()){
            if(measure > input.get(value)){
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

class Tuple implements Comparable<Tuple>{

    double value;
    int position;

    Tuple(double value, int position){
        this.value = value;
        this.position = position;
    }

    @Override
    public int compareTo(Tuple o) {
        if(o != null){
            if(o.value > value){
                return 1;
            } else if (o.value == value){
                return 0;
            }
            return -1;
        }
        return 0;
    }
}