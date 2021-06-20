package com.kynigopoulos;

import com.kynigopoulos.InsightTypes.InsightType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Insight implements Comparable<Insight> {

    private final ArrayList<DataType<?>> subspace;
    private final int dimension;
    private final CompositeExtractor compositeExtractor;
    private final double value;
    private final InsightType insightType;
    private final Map<DataType<?>, Double> resultSet;

    public Insight(
            ArrayList<DataType<?>> subspace,
            int dimension,
            CompositeExtractor Ce,
            double value,
            InsightType insightType,
            Map<DataType<?>, Double> resultSet
    ){
        this.subspace = subspace;
        this.dimension = dimension;
        this.compositeExtractor = Ce;
        this.value = value;
        this.insightType = insightType;
        this.resultSet = resultSet;
    }

    public double getValue(){
        return value;
    }

    /**
     *
     * @return A JSON string of the variables
     */
    public String toJSONString(){
        StringBuilder result = new StringBuilder();
        result.append("{ ");

        //Append Subspace
        result.append("\"subspace\": [ ");
        for (int i = 0; i < subspace.size(); i++){
            if(subspace.get(i) == null){
                result.append("null");
            } else {
                result.append("\"");
                result.append(subspace.get(i).getValue());
                result.append("\"");
            }

            if(i != subspace.size() - 1) result.append(", ");
        }
        result.append(" ], ");

        //Append dividing Dimension
        result.append("\"dimension\": ").append(dimension);
        result.append(", ");

        //Append Composite Extractor
        result.append("\"extractor\": [ ");
        for(int i = 0; i < compositeExtractor.pairs.length; i++){
            result.append("{ ");
            result.append("\"type\": \"").append(compositeExtractor.pairs[i].getType().toString()).append("\", ");
            result.append("\"dimension\": ").append(compositeExtractor.pairs[i].getDimension()).append(" }");

            if(i != compositeExtractor.pairs.length - 1) result.append(", ");
        }
        result.append(" ], ");

        //Append value
        result.append("\"value\": ").append(value);
        result.append(", ");

        //Append insight type
        result.append("\"insightType\": \"").append(insightType.getType()).append("\"");
        result.append(", ");

        //Append result set
        result.append("\"resultSet\": { ");
        Iterator<Map.Entry<DataType<?>, Double>> iterator = resultSet.entrySet().iterator();
        while (true){
            Map.Entry<DataType<?>, Double> entry = iterator.next();
            result.append("\"").append(entry.getKey().getValue()).append("\": ");
            result.append(entry.getValue());
            if(iterator.hasNext()){
                result.append(", ");
            } else {
                break;
            }
        }
        result.append(" }");

        result.append(" }");
        return result.toString();
    }

    @Override
    public String toString() {
        return "Insight{" +
                "subspace=" + TopKInsights.getSubspaceString(subspace) +
                ", dimension=" + dimension +
                ", compositeExtractor=" + compositeExtractor +
                ", value=" + value +
                '}';
    }

    @Override
    public int compareTo(Insight o) {
        if(o != null){
            if(o.value < value){
                return 1;
            } else if (o.value == value){
                return 0;
            }
            return -1;
        }
        return 0;
    }
}
