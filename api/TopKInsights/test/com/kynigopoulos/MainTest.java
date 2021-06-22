package com.kynigopoulos;

import com.kynigopoulos.Extractors.Extractor;
import com.kynigopoulos.Extractors.RankExtractor;
import com.kynigopoulos.InsightTypes.InsightType;
import com.kynigopoulos.InsightTypes.ShapeInsight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class MainTest {

    public static void calculateAndPrint(Database database, int k, int t){
        TopKInsights topKInsights = new TopKInsights(database, k, t);
        ArrayList<Insight> insights = topKInsights.getInsights();
        for (Insight insight : insights) {
            System.out.println(insight.toJSONString());
        }
    }

    @Test
    public void simpleTest() {
        Database database = DatabaseTest.simpleDatabase();
        int t = 2;
        int k = 3;
        calculateAndPrint(database, k, t);
    }

    @Test
    public void simpleTest2(){
        Database database = DatabaseTest.simpleDatabase2();
        int t = 2;
        int k = 3;
        calculateAndPrint(database, k, t);
    }

    @Test
    public void depth3Test(){
        Database database = DatabaseTest.simpleDatabase();
        int t = 3;
        int k = 3;
        calculateAndPrint(database, k, t);
    }

    @Test
    public void rankExtractorOnly(){
        Database database = DatabaseTest.simpleDatabase3();
        int t = 2;
        int k = 10;
        Config.setExtractors(new Extractor[]{new RankExtractor()});
        Config.setInsightTypes(new InsightType[]{new ShapeInsight()});
        calculateAndPrint(database, k, t);
    }

}
