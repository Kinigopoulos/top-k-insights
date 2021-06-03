package com.kynigopoulos;

import java.util.Comparator;
import java.util.Objects;

public class DataType<T> implements Comparable<DataType<T>>{

    private T value;
    private final boolean isOrdinal;

    public DataType(T value){
        this.value = value;
        isOrdinal = value instanceof Number;
    }

    public void setValue(T value){
        this.value = value;
    }

    public T getValue(){
        return value;
    }

    public boolean isOrdinal(){
        return isOrdinal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataType<?> dataType = (DataType<?>) o;
        return Objects.equals(value, dataType.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isOrdinal);
    }

    @Override
    public int compareTo(DataType<T> o) {
        Number a = (Number) o.value;
        Number b = (Number) value;
        if(a.doubleValue() < b.doubleValue()){
            return 1;
        } else if (a.doubleValue() == b.doubleValue()){
            return 0;
        }
        return -1;
    }
}
