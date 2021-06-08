package com.kynigopoulos;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.Aggregators.SumAggregator;
import com.kynigopoulos.Extractors.*;
import com.kynigopoulos.InsightTypes.InsightType;
import com.kynigopoulos.InsightTypes.PointInsight;
import com.kynigopoulos.InsightTypes.ShapeInsight;

public class Config {

    public static Extractor[] extractors = new Extractor[]{
            new AverageDifferenceExtractor(),
            new PercentageExtractor(),
            new PreviousDifferenceExtractor(),
            new RankExtractor()
    };

    public static InsightType[] insightTypes = new InsightType[]{
            new PointInsight(),
            new ShapeInsight()
    };

    public static Aggregator aggregator = new SumAggregator();

    public void setExtractors(Extractor[] extractors){
        Config.extractors = extractors;
    }

    public void setInsightTypes(InsightType[] insightTypes){
        Config.insightTypes = insightTypes;
    }

    public void setAggregator(Aggregator aggregator){
        Config.aggregator = aggregator;
    }

}
