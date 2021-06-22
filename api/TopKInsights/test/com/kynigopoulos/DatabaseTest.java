package com.kynigopoulos;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTest {

    public static ArrayList<DataType<?>> makeRow(int year, char brand, int sales) {
        ArrayList<DataType<?>> row = new ArrayList<>();

        row.add(new DataType<>(year));
        row.add(new DataType<>(brand));
        row.add(new DataType<>(sales));

        return row;
    }

    public static Database simpleDatabase(){
        int[] domainDimensions = new int[]{0, 1};
        int measureDimension = 2;
        boolean[] ordinal = new boolean[]{true, false, true};
        Database database = new Database(new String[]{"Year", "Brand", "Sales"}, domainDimensions, measureDimension, ordinal);

        database.addRow(makeRow(2010, 'F', 13));
        database.addRow(makeRow(2011, 'F', 10));
        database.addRow(makeRow(2012, 'F', 14));
        database.addRow(makeRow(2013, 'F', 23));
        database.addRow(makeRow(2014, 'F', 27));
        //--
        database.addRow(makeRow(2010, 'B', 20));
        database.addRow(makeRow(2011, 'B', 18));
        database.addRow(makeRow(2012, 'B', 20));
        database.addRow(makeRow(2013, 'B', 17));
        database.addRow(makeRow(2014, 'B', 19));
        //--
        database.addRow(makeRow(2010, 'H', 40));
        database.addRow(makeRow(2011, 'H', 35));
        database.addRow(makeRow(2012, 'H', 36));
        database.addRow(makeRow(2013, 'H', 43));
        database.addRow(makeRow(2014, 'H', 58));
        //--
        database.addRow(makeRow(2010, 'T', 38));
        database.addRow(makeRow(2011, 'T', 34));
        database.addRow(makeRow(2012, 'T', 34));
        database.addRow(makeRow(2013, 'T', 29));
        database.addRow(makeRow(2014, 'T', 36));
        return database;
    }

    public static ArrayList<DataType<?>> makeRow2(int year, char brand, int sales) {
        ArrayList<DataType<?>> row = new ArrayList<>();

        row.add(new DataType<>(brand));
        row.add(new DataType<>(year));
        row.add(new DataType<>(sales));

        return row;
    }

    public static Database simpleDatabase2(){
        int[] domainDimensions = new int[]{0, 1};
        int measureDimension = 2;
        boolean[] ordinal = new boolean[]{false, true, true};
        Database database = new Database(new String[]{"Brand", "Year", "Sales"}, domainDimensions, measureDimension, ordinal);

        database.addRow(makeRow2(2010, 'F', 13));
        database.addRow(makeRow2(2011, 'F', 10));
        database.addRow(makeRow2(2012, 'F', 14));
        database.addRow(makeRow2(2013, 'F', 23));
        database.addRow(makeRow2(2014, 'F', 27));
        //--
        database.addRow(makeRow2(2010, 'B', 20));
        database.addRow(makeRow2(2011, 'B', 18));
        database.addRow(makeRow2(2012, 'B', 20));
        database.addRow(makeRow2(2013, 'B', 17));
        database.addRow(makeRow2(2014, 'B', 19));
        //--
        database.addRow(makeRow2(2010, 'H', 40));
        database.addRow(makeRow2(2011, 'H', 35));
        database.addRow(makeRow2(2012, 'H', 36));
        database.addRow(makeRow2(2013, 'H', 43));
        database.addRow(makeRow2(2014, 'H', 58));
        //--
        database.addRow(makeRow2(2010, 'T', 38));
        database.addRow(makeRow2(2011, 'T', 34));
        database.addRow(makeRow2(2012, 'T', 34));
        database.addRow(makeRow2(2013, 'T', 29));
        database.addRow(makeRow2(2014, 'T', 36));
        return database;
    }

    public static Database simpleDatabase3(){
        int[] domainDimensions = new int[]{0, 1};
        int measureDimension = 2;
        boolean[] ordinal = new boolean[]{false, true, true};
        Database database = new Database(new String[]{"Brand", "Year", "Sales"}, domainDimensions, measureDimension, ordinal);

        database.addRow(makeRow2(2010, 'B', 20));
        database.addRow(makeRow2(2011, 'B', 18));
        database.addRow(makeRow2(2012, 'B', 20));
        database.addRow(makeRow2(2013, 'B', 17));
        database.addRow(makeRow2(2014, 'B', 19));

        database.addRow(makeRow2(2010, 'F', 13));
        database.addRow(makeRow2(2011, 'F', 10));
        database.addRow(makeRow2(2012, 'F', 14));
        database.addRow(makeRow2(2013, 'F', 23));
        database.addRow(makeRow2(2014, 'F', 27));

        database.addRow(makeRow2(2010, 'H', 40));
        database.addRow(makeRow2(2011, 'H', 35));
        database.addRow(makeRow2(2012, 'H', 36));
        database.addRow(makeRow2(2013, 'H', 43));
        database.addRow(makeRow2(2014, 'H', 58));

        database.addRow(makeRow2(2010, 'T', 38));
        database.addRow(makeRow2(2011, 'T', 34));
        database.addRow(makeRow2(2012, 'T', 34));
        database.addRow(makeRow2(2013, 'T', 29));
        database.addRow(makeRow2(2014, 'T', 36));
        return database;
    }

    @Test
    public void checkOrdinalColumns(){
        Database database = simpleDatabase();
        assertEquals(true, database.isOrdinal(0));
        assertEquals(false, database.isOrdinal(1));
    }


}
