package com.kynigopoulos;

import com.kynigopoulos.Aggregators.Aggregator;
import com.kynigopoulos.Extractors.Extractor;
import com.kynigopoulos.InsightTypes.InsightType;

import java.util.*;

public class TopKInsights {

    private final Database database;
    private final int k;
    private final int t;

    private PriorityQueue<Insight> priorityQueue;
    private HashMap<ArrayList<DataType<?>>, Double> impactCube;
    private HashMap<ArrayList<DataType<?>>, Double> dataCube;

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

        dataCube = new HashMap<>();
        impactCube = new HashMap<>();

        //Enumerate all possible Extractors
        ArrayList<CompositeExtractor> compositeExtractors =
                CompositeExtractor.findCombinations(database, t);

        //Initialize subspace. Null value represents *
        ArrayList<DataType<?>> subspace = Database.getSubspaceCopy(database.superSubspace);

        //Starting procedure of enumerating insights
        for(CompositeExtractor compositeExtractor : compositeExtractors){
            for(int domainDimension : domainDimensions){
                EnumerateInsight(subspace, domainDimension, compositeExtractor);

                ArrayList<DataType<?>> values = database.getValues(domainDimension);
                for(DataType<?> value : values){
                    ArrayList<DataType<?>> newSubspace = Database.getSubspaceCopy(subspace);
                    newSubspace.set(domainDimension, value);

                    for(int dividingDimension : domainDimensions){
                        if(newSubspace.get(dividingDimension) != null){
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

    private double getImpact(ArrayList<DataType<?>> subspace){
        if(impactCube.containsKey(subspace)){
            return impactCube.get(subspace);
        }
        double impact = database.getSubspaceSum(subspace);
        impactCube.put(subspace, impact);
        return impact;
    }

    /**
     * @param subspace The subspace of the sibling group
     * @param dimension The dimension of the sibling group
     * @param extractor The composite extractor
     */
    private void EnumerateInsight(ArrayList<DataType<?>> subspace, int dimension, CompositeExtractor extractor){

        String sub = getSubspaceString(subspace);
        System.out.println("SG(" + sub + ", " + database.getDimensionName(dimension) + ") \t\t" + extractor.toString(database) +
                 ", is valid: " + isValid(subspace, dimension, extractor));

        if(isValid(subspace, dimension, extractor)){
            double impact = getImpact(subspace) / getImpact(Database.getSubspaceCopy(database.superSubspace));
            if(priorityQueue.size() == k) {
                assert priorityQueue.peek() != null;
                if (impact < priorityQueue.peek().getValue()) {
                    return;
                }
            }

            Map<DataType<?>, Double> F = ExtractF(subspace, dimension, extractor);
            if(F.size() == 0){
                System.out.println("All values were null. Skipping this set...");
                return;
            }

            for(InsightType insightType : Config.insightTypes){
                double significance = insightType.getSignificance(F);
                if (significance < 0){
                    continue;
                }

                double S = significance * impact;
                if(priorityQueue.size() == k){
                    assert priorityQueue.peek() != null;
                    if(S > priorityQueue.peek().getValue()){
                        priorityQueue.poll();
                        System.out.println("Found new insight with score: " + S);
                        priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                    }
                }else{
                    System.out.println("Found new insight with score: " + S);
                    priorityQueue.add(new Insight(subspace, dimension, extractor, S, insightType, F));
                }
            }
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

            if(FLevel.get(subspace.get(extractorDimension)) == null){
                return null;
            }

            Extractor e = (Extractor) extractor.getPair(level - 1).getType();
            return e.getOutput(FLevel, subspace.get(extractorDimension));
        }
        if(dataCube.containsKey(subspace)){
            return dataCube.get(subspace);
        }
        double M = ((Aggregator)extractor.getPair(0).getType()).getOutput(database, subspace, dimension);
        dataCube.put(subspace, M);
        return M;
    }
}
