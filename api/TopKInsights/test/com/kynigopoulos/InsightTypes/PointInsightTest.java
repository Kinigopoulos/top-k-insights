package com.kynigopoulos.InsightTypes;

import com.kynigopoulos.DataType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PointInsightTest {

    private static Map<DataType<?>, Double> toMapResultSet(double[] values){
        Map<DataType<?>, Double> set = new HashMap<>();
        char key = 'A';
        for(double value : values){
            set.put(new DataType<>(key), value);
            key++;
        }
        return set;
    }

    @Test
    public void SimpleSignificanceTest(){
        double[] set = new double[]{8.0, 3.0, 3.5, 3.2, 2.1, 2.0, 2.0, 1.9, 1.8, 1.7, -1.5};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(toMapResultSet(set));
        System.out.println(significance);
    }

    @Test
    public void otherTest(){
        double[] set = new double[]{28.0, 8.0, 7.0, -14.0};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(toMapResultSet(set));
        System.out.println(significance);
    }

    @Test
    public void otherTest2(){
        double[] set = new double[]{15.0, 7.0, 1.0, -5.0};
        PointInsight pointInsight = new PointInsight();
        double significance = pointInsight.getSignificance(toMapResultSet(set));
        System.out.println(significance);
    }

}
