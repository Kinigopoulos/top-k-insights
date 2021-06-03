package com.kynigopoulos;

import java.util.ArrayList;

public class Insight implements Comparable<Insight> {

    private final ArrayList<DataType<?>> subspace;
    private final int dimension;
    private final CompositeExtractor compositeExtractor;
    private final double value;

    public Insight(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor Ce, double value){
        this.subspace = subspace;
        this.dimension = dimension;
        this.compositeExtractor = Ce;
        this.value = value;
    }

    public ArrayList<DataType<?>> getSubspace(){
        return subspace;
    }

    public int getDimension() {
        return dimension;
    }

    public CompositeExtractor getCompositeExtractor() {
        return compositeExtractor;
    }

    public double getValue(){
        return value;
    }

    @Override
    public String toString() {
        return "Insight{" +
                "subspace=" + TopKInsights.getSubspace(subspace) +
                ", dimension=" + dimension +
                ", compositeExtractor=" + compositeExtractor +
                ", value=" + value +
                '}';
    }

    @Override
    public int compareTo(Insight o) {
        if(o != null){
            if(o.value < value){
                return 1;
            } else if (o.value == value){
                return 0;
            }
            return -1;
        }
        return 0;
    }
}
