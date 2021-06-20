package com.kynigopoulos;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.Aggregators.CountAggregator;
import com.kynigopoulos.Aggregators.MeanAggregator;
import com.kynigopoulos.Aggregators.SumAggregator;
import com.kynigopoulos.Extractors.*;
import com.kynigopoulos.InsightTypes.InsightType;
import com.kynigopoulos.InsightTypes.PointInsight;
import com.kynigopoulos.InsightTypes.ShapeInsight;

import java.util.Arrays;
import java.util.HashSet;

public class Config {

    public static Extractor[] availableExtractors = new Extractor[]{
            new AverageDifferenceExtractor(),
            new PercentageExtractor(),
            new PreviousDifferenceExtractor(),
            new RankExtractor()
    };

    public static InsightType[] availableInsightTypes = new InsightType[]{
            new PointInsight(),
            new ShapeInsight()
    };

    public static Aggregator[] availableAggregators = new Aggregator[]{
            new SumAggregator(),
            new MeanAggregator(),
            new CountAggregator()
    };

    public static Extractor[] extractors = availableExtractors;

    public static InsightType[] insightTypes = availableInsightTypes;

    public static Aggregator aggregator = new SumAggregator();

    public static void setExtractors(Extractor[] extractors){
        Config.extractors = extractors;
    }

    public static void setInsightTypes(InsightType[] insightTypes){
        Config.insightTypes = insightTypes;
    }

    public static void setAggregator(Aggregator aggregator){
        Config.aggregator = aggregator;
    }

    public static void setExtractorsByString(String[] extractors){
        HashSet<String> extractorsSet = new HashSet<>(Arrays.asList(extractors));
        Extractor[] selected = new Extractor[extractors.length];
        int i = 0;
        for(Extractor extractor : Config.availableExtractors){
            if(extractorsSet.contains(extractor.toString())){
                selected[i] = extractor;
                i++;
            }
        }

        setExtractors(selected);
    }

    public static void setInsightTypesByString(String[] insightTypes){
        System.out.println(insightTypes.length + " " + insightTypes[0]);
        HashSet<String> insightTypesSet = new HashSet<>(Arrays.asList(insightTypes));
        InsightType[] selected = new InsightType[insightTypes.length];
        int i = 0;
        for(InsightType insightType : Config.availableInsightTypes){
            if(insightTypesSet.contains(insightType.getType())){
                selected[i] = insightType;
                i++;
            }
        }

        setInsightTypes(selected);
    }

    public static void setAggregatorByString(String aggregator){
        for(Aggregator aggregatorFunction : Config.availableAggregators){
            if(aggregatorFunction.toString().equals(aggregator)){
                setAggregator(aggregatorFunction);
                return;
            }
        }
    }

}
