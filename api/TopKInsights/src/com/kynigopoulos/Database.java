package com.kynigopoulos;

import java.util.*;

public class Database {

    private final ArrayList<ArrayList<DataType<?>>> data;
    private final ArrayList<HashMap<Object, DataType<?>>> domainValues;
    private final int[] domainDimensions;
    private final String[] dimensions;
    private final int measureIndex;

    public Database(String[] dimensions, int[] domainDimensions, int measureIndex){
        data = new ArrayList<>();
        this.dimensions = dimensions;
        this.domainDimensions = domainDimensions;
        this.measureIndex = measureIndex;

        domainValues = new ArrayList<>(dimensions.length);
        for(int i = 0; i < dimensions.length; i++){
            domainValues.add(new HashMap<>());
        }
    }

    public void addRow(ArrayList<DataType<?>> row){
        if(row.size() == dimensions.length) {
            for(int i = 0; i < row.size(); i++){
                if(measureIndex == i){
                    continue;
                }
                if(domainValues.get(i).containsKey(row.get(i).getValue())){
                    row.set(i, domainValues.get(i).get(row.get(i).getValue()));
                } else {
                    domainValues.get(i).put(row.get(i).getValue(), row.get(i));
                }
            }
            data.add(row);
        } else {
            System.out.println("Wrong size");
        }
    }

    public ArrayList<DataType<?>> getRow(int index){
        return data.get(index);
    }

    public Number getMeasureValue(int index){
        return (Number) data.get(index).get(measureIndex).getValue();
    }

    public boolean belongsToSubspace(ArrayList<DataType<?>> subspace, int index){
        for(int domainDimension : domainDimensions){
            if(subspace.get(domainDimension) != null && subspace.get(domainDimension) != data.get(index).get(domainDimension)){
                return false;
            }
        }
        return true;
    }

    public ArrayList<Object> getMeasureList(){
        ArrayList<Object> result = new ArrayList<>();
        for(ArrayList<DataType<?>> tuple : data){
            result.add(tuple.get(measureIndex).getValue());
        }

        return result;
    }

    public Double getMeasureSum(){
        double sum = 0;
        for(ArrayList<DataType<?>> row : data){
            sum += ((Number)row.get(measureIndex).getValue()).doubleValue();
        }
        return sum;
    }

    public String getDimensionName(int dimension){
        return dimensions[dimension];
    }

    public ArrayList<ArrayList<DataType<?>>> getData(){
        return data;
    }

    public ArrayList<DataType<?>> getValues(int dimension){
        return new ArrayList<>(domainValues.get(dimension).values());
    }

    public int getMeasureIndex(){
        return measureIndex;
    }

    public int size(){
        return data.size();
    }

    public void printTable(){
        for(ArrayList<DataType<?>> tuple : data){
            for (DataType<?> type : tuple){
                System.out.print(type.getValue());
                System.out.print(" ");
            }
            System.out.println();
        }
    }

}
