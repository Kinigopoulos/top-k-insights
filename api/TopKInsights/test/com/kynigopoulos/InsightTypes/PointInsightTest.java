package com.kynigopoulos.InsightTypes;

import org.junit.jupiter.api.Test;

public class PointInsightTest {

    @Test
    public void SimpleSignificanceTest(){
        double[] set = new double[]{8.0, 3.0, 3.5, 3.2, 2.1, 2.0, 2.0, 1.9, 1.8, 1.7, -1.5};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(InsightHelper.toMapResultSet(set, false));
        System.out.println(significance);
    }

    @Test
    public void otherTest(){
        double[] set = new double[]{28.0, 8.0, 7.0, -14.0};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(InsightHelper.toMapResultSet(set, false));
        System.out.println(significance);
    }

    @Test
    public void otherTest2(){
        double[] set = new double[]{15.0, 7.0, 1.0, -5.0};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(InsightHelper.toMapResultSet(set, false));
        System.out.println(significance);
    }

    @Test
    public void otherTest3(){
        double[] set = new double[]{28, 25.0, 14.0, 8, 5, 3};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(InsightHelper.toMapResultSet(set, false));
        System.out.println(significance);
    }

}
