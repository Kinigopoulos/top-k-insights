package com.kynigopoulos;

import java.util.*;

public class Database {

    private final ArrayList<ArrayList<DataType<?>>> data;
    private final ArrayList<HashMap<Object, DataType<?>>> domainValues;
    private final int[] domainDimensions;
    private final String[] dimensions;
    private final boolean[] ordinal;
    private final int measureIndex;

    public Database(String[] dimensions, int[] domainDimensions, int measureIndex, boolean[] ordinal) {
        data = new ArrayList<>();
        this.dimensions = dimensions;
        this.domainDimensions = domainDimensions;
        this.measureIndex = measureIndex;
        this.ordinal = ordinal;

        domainValues = new ArrayList<>(dimensions.length);
        for (int i = 0; i < dimensions.length; i++) {
            domainValues.add(new HashMap<>());
        }
    }

    public void addRow(ArrayList<DataType<?>> row) {
        if (row.size() == dimensions.length) {
            for (int i = 0; i < row.size(); i++) {
                if (measureIndex == i) {
                    continue;
                }
                if (domainValues.get(i).containsKey(row.get(i).getValue())) {
                    row.set(i, domainValues.get(i).get(row.get(i).getValue()));
                } else {
                    domainValues.get(i).put(row.get(i).getValue(), row.get(i));
                }
            }
            data.add(row);
        } else {
            System.out.println("Wrong size of columns, expected " + dimensions.length +
                    " and got " + row.size());
        }
    }

    /**
     * @return an Arraylist of null values with the length of a row
     * null represents * sign.
     */
    public ArrayList<DataType<?>> getSubspace() {
        ArrayList<DataType<?>> subspace = new ArrayList<>(domainDimensions.length);
        for(int i = 0; i < domainDimensions.length; i++){
            subspace.add(null);
        }
        return subspace;
    }

    /**
     * @param initial initial Subspace
     * @return cloned subspace object
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<DataType<?>> getSubspaceCopy(ArrayList<DataType<?>> initial){
        return (ArrayList<DataType<?>>) initial.clone();
    }

    public int[] getDomainDimensions() {
        return domainDimensions;
    }

    public Number getMeasureValue(int index) {
        return (Number) data.get(index).get(measureIndex).getValue();
    }

    public boolean belongsToSubspace(ArrayList<DataType<?>> subspace, int index) {
        for (int domainDimension : domainDimensions) {
            if (subspace.get(domainDimension) != null && subspace.get(domainDimension) != data.get(index).get(domainDimension)) {
                return false;
            }
        }
        return true;
    }

    public double getSubspaceSum(ArrayList<DataType<?>> subspace){
        double sum = 0;
        for (int i = 0; i < data.size(); i++){
            if(belongsToSubspace(subspace, i)){
                sum += getMeasureValue(i).doubleValue();
            }
        }

        return sum;
    }

    public Double getMeasureSum() {
        double sum = 0;
        for (ArrayList<DataType<?>> row : data) {
            sum += ((Number) row.get(measureIndex).getValue()).doubleValue();
        }
        return sum;
    }

    public String getDimensionName(int dimension) {
        return dimensions[dimension];
    }

    public ArrayList<DataType<?>> getValues(int dimension) {
        return new ArrayList<>(domainValues.get(dimension).values());
    }

    public Boolean isOrdinal(int dimension){
        return ordinal[dimension];
    }

    /**
     * @return the index/column of the measure value
     */
    public int getMeasureIndex() {
        return measureIndex;
    }

    /**
     * @return the number of records in the database
     */
    public int size() {
        return data.size();
    }

    /**
     * Prints the database to console
     */
    public void printTable() {
        for (ArrayList<DataType<?>> tuple : data) {
            for (DataType<?> type : tuple) {
                System.out.print(type.getValue());
                System.out.print(" ");
            }
            System.out.println();
        }
    }

}
