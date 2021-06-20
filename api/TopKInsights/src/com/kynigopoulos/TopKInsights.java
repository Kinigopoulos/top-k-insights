package com.kynigopoulos;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.Extractors.Extractor;
import com.kynigopoulos.InsightTypes.InsightType;

import java.util.*;

public class TopKInsights {

    private final Database database;
    private final int k;
    private final int t;
    private double totalSum;

    private PriorityQueue<Insight> priorityQueue;

    public TopKInsights(Database database, int k, int t){
        this.database = database;
        this.k = k;
        this.t = t;
    }

    /**
     * @param priorityQueue is the max-heap that stores the insights
     * @return the insights in an ArrayList
     */
    private static ArrayList<Insight> heapToArrayList(PriorityQueue<Insight> priorityQueue){
        ArrayList<Insight> result = new ArrayList<>();
        while (!priorityQueue.isEmpty()){
            result.add(0, priorityQueue.poll());
        }
        return result;
    }

    /**
     * @return an ArrayList with Top-K insights with τ-depth
     */
    public ArrayList<Insight> getInsights(){

        if(database.size() == 0){
            return new ArrayList<>();
        }

        int[] domainDimensions = database.getDomainDimensions();

        //Initializing heap with k capacity
        priorityQueue = new PriorityQueue<>(k);
        //Storing the total sum of measures because it is needed for every insight evaluation
        totalSum = database.getMeasureSum();

        //Enumerate all possible Extractors
        ArrayList<CompositeExtractor> compositeExtractors =
                CompositeExtractor.findCombinations(database, t);

        //Initialize subspace. Null value represents *
        ArrayList<DataType<?>> subspace = database.getSubspace();

        //Starting procedure of enumerating insights
        for(CompositeExtractor compositeExtractor : compositeExtractors){
            for(int domainDimension : domainDimensions){
                EnumerateInsight(subspace, domainDimension, compositeExtractor);

                ArrayList<DataType<?>> values = database.getValues(domainDimension);
                for(DataType<?> value : values){
                    ArrayList<DataType<?>> newSubspace = Database.getSubspaceCopy(subspace);
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

    /**
     * @param subspace Subspace of the sibling group
     * @param dividingDimension Dimension of the sibling group
     * @param extractor The extractor
     * @return if it is valid to generate insights with this pair
     */
    private boolean isValid(ArrayList<DataType<?>> subspace, int dividingDimension, CompositeExtractor extractor){
        for(int i = 1; i < t; i++){
            int Dx = extractor.getPair(i).getDimension();
            if(dividingDimension != Dx && subspace.get(Dx) == null){
                return false;
            }
        }

        return true;
    }

    /**
     * @param subspace the subspace
     * @return a String with the subspace's info
     */
    public static String getSubspaceString(ArrayList<DataType<?>> subspace){
        StringBuilder sub = new StringBuilder("< ");
        for(DataType<?> dataType : subspace){
            if(dataType == null){
                sub.append("* ");
            } else {
                sub.append(dataType.getValue()).append(" ");
            }
        }
        sub.append(">");
        return sub.toString();
    }

    /**
     * @param subspace The subspace of the sibling group
     * @param dimension The dimension of the sibling group
     * @param extractor The composite extractor
     */
    private void EnumerateInsight(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor extractor){

        String sub = getSubspaceString(subspace);
        System.out.println("SG(" + sub + ", " + database.getDimensionName(dimension) + dimension + ") \t\t" + extractor.toString() +
                 " " + isValid(subspace, dimension, extractor));

        if(isValid(subspace, dimension, extractor)){
            Map<DataType<?>, Double> F = ExtractF(subspace, dimension, extractor);
            if(F.size() == 0){
                System.out.println("All values were null");
                return;
            }

            double impact = database.getSubspaceSum(subspace) / totalSum;
            for(InsightType insightType : Config.insightTypes){
                double significance = insightType.getSignificance(F);

                double S = significance * impact;
                if(priorityQueue.size() == k){
                    assert priorityQueue.peek() != null;
                    if(S > priorityQueue.peek().getValue()){
                        priorityQueue.poll();
                        priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                    }
                }else{
                    priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                }
            }

            for (Map.Entry<DataType<?>, Double> d : F.entrySet()) {
                System.out.print(d.getKey().getValue() + ": " + d.getValue() + ", ");
            }

            System.out.println("Importance value: " + impact);
            System.out.println();
        }

    }

    /**
     * @param subspace The subspace of the sibling group
     * @param dimension The dimension of the sibling group
     * @param extractor The composite extractor
     * @return the extracted set values
     */
    private Map<DataType<?>, Double> ExtractF(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor extractor){

        Map<DataType<?>, Double> F;
        if(database.isOrdinal(dimension)){
            F = new TreeMap<>();
        }else{
            F = new HashMap<>();
        }

        ArrayList<DataType<?>> newSubspace = Database.getSubspaceCopy(subspace);
        for(DataType<?> value : database.getValues(dimension)){
            newSubspace.set(dimension, value);
            Double M = RecursiveExtract(newSubspace, dimension, t, extractor);
            if(M != null) {
                F.put(value, M);
            }
        }
        return F;
    }

    /**
     *
     * @param subspace The subspace of the sibling group
     * @param dimension The dimension of the sibling group
     * @param level The current level of extractor
     * @param extractor The composite extractor
     * @return a value of the result set
     */
    private Double RecursiveExtract(
            ArrayList<DataType<?>> subspace,
            int dimension,
            int level,
            CompositeExtractor extractor
    ) {
        if (level > 1){
            Map<DataType<?>, Double> FLevel;
            int extractorDimension = extractor.getPair(level - 1).getDimension();

            if(database.isOrdinal(extractorDimension)){
                FLevel = new TreeMap<>();
            }else{
                FLevel = new HashMap<>();
            }

            ArrayList<DataType<?>> childSubspace = Database.getSubspaceCopy(subspace);
            for(DataType<?> value : database.getValues(extractorDimension)){
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
