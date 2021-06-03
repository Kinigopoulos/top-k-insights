package com.kynigopoulos;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.Extractors.Extractor;
import com.kynigopoulos.InsightTypes.InsightType;
import com.kynigopoulos.InsightTypes.PointInsight;
import com.kynigopoulos.InsightTypes.ShapeInsight;

import java.util.*;

public class TopKInsights {

    private final Database database;
    private final int k;
    private final int t;

    private PriorityQueue<Insight> priorityQueue;

    private static final InsightType[] insightTypes = new InsightType[]{
            new PointInsight(),
            new ShapeInsight()
    };

    public TopKInsights(Database database, int k, int t){
        this.database = database;
        this.k = k;
        this.t = t;
    }

    private static ArrayList<Insight> heapToArrayList(PriorityQueue<Insight> priorityQueue){
        ArrayList<Insight> result = new ArrayList<>();
        while (!priorityQueue.isEmpty()){
            result.add(0, priorityQueue.poll());
        }
        return result;
    }


    public ArrayList<Insight> getInsights(int[] domainDimensions, int measureDimension){

        if(database.size() == 0){
            return new ArrayList<>();
        }

        priorityQueue = new PriorityQueue<>(k);
        //TODO peek to get value
        //TODO poll to remove the smallest value

        //Enumerate all possible Extractors
        ArrayList<CompositeExtractor> compositeExtractors =
                CompositeExtractor.findCombinations(database.getRow(0), domainDimensions, measureDimension, t);

        //Initialize subspace. Null value represents *.
        ArrayList<DataType<?>> subspace = (ArrayList<DataType<?>>) database.getRow(0).clone();
        subspace.remove(measureDimension);
        for (int domainDimension : domainDimensions) {
            subspace.set(domainDimension, null);
        }

        //Starting procedure of enumerating insights
        for(CompositeExtractor compositeExtractor : compositeExtractors){
            for(int domainDimension : domainDimensions){
                EnumerateInsight(subspace, domainDimension, compositeExtractor);

                ArrayList<DataType<?>> values = database.getValues(domainDimension);
                for(DataType<?> value : values){
                    ArrayList<DataType<?>> newSubspace = (ArrayList<DataType<?>>) subspace.clone();
                    newSubspace.set(domainDimension, value);

                    for(int dividingDimension : domainDimensions){
                        if(domainDimension == dividingDimension){
                            continue;
                        }
                        EnumerateInsight(newSubspace, dividingDimension, compositeExtractor);
                    }
                }
                System.out.println();
            }
        }

        //return heap after converting it to a list
        return heapToArrayList(priorityQueue);
    }

    private boolean isValid(ArrayList<DataType<?>> subspace, int dividingDimension, CompositeExtractor extractor){
        for(int i = 1; i < t; i++){
            int Dx = extractor.getPair(i).getDimension();
            if(dividingDimension != Dx && subspace.get(Dx) == null){
                return false;
            }
        }

        return true;
    }

    public static String getSubspace(ArrayList<DataType<?>> subspace){
        String sub = "< ";
        for(DataType<?> dataType : subspace){
            if(dataType == null){
                sub += "* ";
            } else {
                sub += dataType.getValue() + " ";
            }
        }
        sub += ">";
        return sub;
    }

    private void EnumerateInsight(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor extractor){

        String sub = getSubspace(subspace);
        System.out.println("SG(" + sub + ", " + database.getDimensionName(dimension) + dimension + ") \t\t" + extractor.toString() +
                 " " + isValid(subspace, dimension, extractor));

        if(isValid(subspace, dimension, extractor)){
            Map<DataType<?>, Double> F = ExtractF(subspace, dimension, extractor);
            if(F.size() == 0){
                System.out.println("All values were null");
                return;
            }

            Aggregator aggregator = (Aggregator) extractor.getPair(0).getType();
            double impact = aggregator.getOutput(database, subspace, dimension) / database.getMeasureSum();
            for(InsightType insightType : insightTypes){
                if(insightType instanceof ShapeInsight && !database.getRow(0).get(dimension).isOrdinal()){
                    continue;
                }
                double significance = insightType.getSignificance(F);

                double S = significance * impact;
                if(priorityQueue.size() == k){
                    assert priorityQueue.peek() != null;
                    if(S > priorityQueue.peek().getValue()){
                        priorityQueue.poll();
                        priorityQueue.add(new Insight(subspace, dimension, extractor, S));
                    }
                }else{
                    priorityQueue.add(new Insight(subspace, dimension, extractor, S));
                }
            }

            for (Map.Entry<DataType<?>, Double> d : F.entrySet()) {
                System.out.print(d.getKey().getValue() + ": " + d.getValue() + ", ");
            }

            System.out.println("Importance value: " + impact);
            System.out.println();
        }

    }

    private Map<DataType<?>, Double> ExtractF(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor extractor){

        Map<DataType<?>, Double> F;
        if(database.getRow(0).get(dimension).isOrdinal()){
            F = new TreeMap<>();
        }else{
            F = new HashMap<>();
        }

        for(DataType<?> value : database.getValues(dimension)){
            ArrayList<DataType<?>> newSubspace = (ArrayList<DataType<?>>) subspace.clone();
            newSubspace.set(dimension, value);
            Double M = RecursiveExtract(newSubspace, dimension, t, extractor);
            if(M != null) {
                F.put(value, M);
            }
        }
        return F;
    }

    private Double RecursiveExtract(
            ArrayList<DataType<?>> subspace,
            int dimension,
            int level,
            CompositeExtractor extractor
    ) {
        if (level > 1){
            Map<DataType<?>, Double> FLevel;
            int extractorDimension = extractor.getPair(level - 1).getDimension();

            if(database.getRow(0).get(extractorDimension).isOrdinal()){
                FLevel = new TreeMap<>();
            }else{
                FLevel = new HashMap<>();
            }
            for(DataType<?> value : database.getValues(extractorDimension)){
                ArrayList<DataType<?>> childSubspace = (ArrayList<DataType<?>>) subspace.clone();
                childSubspace.set(extractorDimension, value);

                Double M = RecursiveExtract(childSubspace, dimension, level - 1, extractor);
                if(M != null) FLevel.put(value, M);
            }

            Extractor e = (Extractor) extractor.getPair(level - 1).getType();

            return e.getOutput(FLevel, subspace.get(extractorDimension));
        }
        return ((Aggregator)extractor.getPair(0).getType()).getOutput(database, subspace, dimension);
    }
}
