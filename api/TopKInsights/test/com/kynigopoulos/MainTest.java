package com.kynigopoulos;

import com.kynigopoulos.Extractors.Extractor;
import com.kynigopoulos.Extractors.RankExtractor;
import com.kynigopoulos.InsightTypes.InsightType;
import com.kynigopoulos.InsightTypes.ShapeInsight;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {

    public static String calculateAndPrint(Database database, int k, int t){
        TopKInsights topKInsights = new TopKInsights(database, k, t);
        ArrayList<Insight> insights = topKInsights.getInsights();
        StringBuilder actual = new StringBuilder();
        for (Insight insight : insights) {
            actual.append(insight.toJSONString()).append("\n");
            System.out.println(insight.toJSONString());
        }
        return actual.toString();
    }

    @Test
    public void simpleTest() {
        Database database = DatabaseTest.simpleDatabase();
        int t = 2;
        int k = 3;
        String actual = calculateAndPrint(database, k, t);
        String expected = "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"AverageDifferenceExtractor\", \"dimension\": 0 } ], \"value\": 0.9552938885510289, \"insightType\": \"Point\", \"resultSet\": { \"2010\": -1.7999999999999972, \"2011\": -15.799999999999997, \"2012\": -8.799999999999997, \"2013\": -0.7999999999999972, \"2014\": 27.200000000000003 } }\n" +
                "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"PercentageExtractor\", \"dimension\": 0 } ], \"value\": 0.9418236562446712, \"insightType\": \"Point\", \"resultSet\": { \"2010\": 0.19680851063829788, \"2011\": 0.17198581560283688, \"2012\": 0.18439716312056736, \"2013\": 0.19858156028368795, \"2014\": 0.24822695035460993 } }\n" +
                "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"PreviousDifferenceExtractor\", \"dimension\": 0 } ], \"value\": 0.9118053637278499, \"insightType\": \"Shape\", \"resultSet\": { \"2011\": -14.0, \"2012\": 7.0, \"2013\": 8.0, \"2014\": 28.0 } }\n";
        assertEquals(expected, actual);
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
        String actual = calculateAndPrint(database, k, t);

        String expected = "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"PercentageExtractor\", \"dimension\": 0 }, { \"type\": \"AverageDifferenceExtractor\", \"dimension\": 0 } ], \"value\": 0.9552939334782926, \"insightType\": \"Point\", \"resultSet\": { \"2010\": -0.0031914893617021323, \"2011\": -0.028014184397163133, \"2012\": -0.015602836879432647, \"2013\": -0.0014184397163120588, \"2014\": 0.048226950354609915 } }\n" +
                "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"AverageDifferenceExtractor\", \"dimension\": 0 }, { \"type\": \"AverageDifferenceExtractor\", \"dimension\": 0 } ], \"value\": 0.9552938885510289, \"insightType\": \"Point\", \"resultSet\": { \"2010\": -1.8, \"2011\": -15.8, \"2012\": -8.8, \"2013\": -0.8, \"2014\": 27.2 } }\n" +
                "{ \"subspace\": [ null, null ], \"dimension\": 0, \"extractor\": [ { \"type\": \"Sum\", \"dimension\": 2 }, { \"type\": \"PreviousDifferenceExtractor\", \"dimension\": 0 }, { \"type\": \"AverageDifferenceExtractor\", \"dimension\": 0 } ], \"value\": 0.9118053637278501, \"insightType\": \"Shape\", \"resultSet\": { \"2011\": -21.25, \"2012\": -0.25, \"2013\": 0.75, \"2014\": 20.75 } }\n";

        assertEquals(expected, actual);
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
