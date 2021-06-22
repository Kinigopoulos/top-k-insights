package com.kynigopoulos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompositeExtractorTest {

    @Test
    public void simpleTest(){
        int t = 2;
        Database database = DatabaseTest.simpleDatabase();
        ArrayList<CompositeExtractor> extractors = CompositeExtractor.findCombinations(database, t);
        assertEquals(7, extractors.size());
    }

}
