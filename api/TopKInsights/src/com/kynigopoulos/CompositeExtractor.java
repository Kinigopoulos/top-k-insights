package com.kynigopoulos;

import com.kynigopoulos.Extractors.*;

import java.util.ArrayList;

public class CompositeExtractor {

    public static int t;

    ExtractorPair<?>[] pairs = new ExtractorPair[t];

    public CompositeExtractor(CompositeExtractor compositeExtractor){
        this.pairs = compositeExtractor.pairs.clone();
    }

    public CompositeExtractor(ExtractorPair<?> pair){
        pairs[0] = pair;
    }

    public ExtractorPair<?> getPair(int index){
        return pairs[index];
    }

    private static ArrayList<CompositeExtractor> makeCompositeExtractor(
            ArrayList<CompositeExtractor> extractors,
            ArrayList<DataType<?>> row,
            int level,
            int[] domainDimensions
    )
    {
        if(level == t){
            return extractors;
        }

        ArrayList<CompositeExtractor> newExtractors = new ArrayList<>();
        for(CompositeExtractor compositeExtractor : extractors){
            for(Extractor extractor : Config.extractors){
                if(level > 1 && !extractor.isMeaningful()){
                    continue;
                }
                for(int dimension : domainDimensions){
                    if(extractor.satisfiesRequirements(row.get(dimension))) {
                        CompositeExtractor Ce = new CompositeExtractor(compositeExtractor);
                        Ce.pairs[level] = new ExtractorPair<>(dimension, extractor);
                        newExtractors.add(Ce);
                    }
                }
            }
        }

        return makeCompositeExtractor(newExtractors, row,level + 1, domainDimensions);
    }

    public static ArrayList<CompositeExtractor> findCombinations(
            Database database,
            int t
    ) {
        CompositeExtractor.t = t;

        ArrayList<DataType<?>> row = database.getRow(0);
        int[] domainDimensions = database.getDomainDimensions();
        int measureDimension = database.getMeasureIndex();

        ArrayList<CompositeExtractor> baseExtractor = new ArrayList<>();
        baseExtractor.add(new CompositeExtractor(new ExtractorPair<>(measureDimension, Config.aggregator)));

        return makeCompositeExtractor(baseExtractor, row,1, domainDimensions);
    }

    @Override
    public String toString() {
        String s = "<";
        for(int i = 0; i < t; i++){
            s += "(" + getPair(i).getType().toString() + ", " + getPair(i).getDimension() + ")";
        }
        s += ">";
        return s;
    }
}


class ExtractorPair<T>{
    private final int dimension;
    private final T type;

    public ExtractorPair(int dimension, T type){
        this.dimension = dimension;
        this.type = type;
    }

    public int getDimension() {
        return dimension;
    }

    public T getType() {
        return type;
    }
}
