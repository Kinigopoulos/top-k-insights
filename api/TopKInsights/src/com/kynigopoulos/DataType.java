package com.kynigopoulos;

import java.util.Objects;

public class DataType<T extends Comparable<T>> implements Comparable<DataType<T>>{

    private final T value;

    public DataType(T value){
        this.value = value;
    }

    public T getValue(){
        return value;
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
        return Objects.hash(value);
    }

    @Override
    public int compareTo(DataType<T> o) {
        return value.compareTo(o.value);
    }
}
