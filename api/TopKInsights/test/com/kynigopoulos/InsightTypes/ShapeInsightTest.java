package com.kynigopoulos.InsightTypes;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShapeInsightTest {

    @Test
    public void simpleTest(){
        double[] set = new double[]{0, 2, 4, 5, 6};
        double[] set2 = new double[]{0, 2, 4, 5, 7};

        InsightType insight = new ShapeInsight();
        double significance = insight.getSignificance(InsightHelper.toMapResultSet(set, true));
        double significance2 = insight.getSignificance(InsightHelper.toMapResultSet(set2, true));

        assertTrue(significance < significance2);
    }

    @Test
    public void simpleTest2(){
        double[] set = new double[]{13, 17, 9, 20, 25, 25};


        InsightType insight = new ShapeInsight();
        double significance = insight.getSignificance(InsightHelper.toMapResultSet(set, true));

        System.out.println(significance);
    }

}
