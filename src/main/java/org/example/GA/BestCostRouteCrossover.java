package org.example.GA;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.*;

public class BestCostRouteCrossover implements Runnable {
    @Override
    public void run() {
        ga.getCrossoverChromosomes().add(Crossover());
    }

    private final GeneticAlgorithm ga;
    private final int r;
    private final Chromosome p1, p2;
    private static Patient[] allPatients;
    private static InstancesClass dataset;
    private static int allCaregivers;
    private static double[][] distances;
    private final Random rand;
    private int counter =0;
//    private final int[] routeEndPoint;
//    private final double[] routesCurrentTime;
//    private final double[] highestAndTotalTardiness;
    private final Set<Integer> track;

    public BestCostRouteCrossover(GeneticAlgorithm ga, int r, Chromosome p1, Chromosome p2, Random rand) {
        this.ga = ga;
        this.r = r;
        this.p1 = p1;
        this.p2 = p2;
        this.rand = rand;
//        this.routeEndPoint = new int[allCaregivers];
//        this.routesCurrentTime = new double[allCaregivers];
//        this.highestAndTotalTardiness = new double[2];
        this.track = new HashSet<>(100);
    }

    public static void initialize(InstancesClass instance){
        allPatients = instance.getPatients();
        distances = instance.getDistances();
        allCaregivers = instance.getCaregivers().length;
        dataset = instance;
    }
    public Chromosome Crossover(){
        Set<Integer> selectedRoute = new HashSet<>();
        List<Integer> removedGenes = new ArrayList<>();
        List<Integer> genes = new ArrayList<>();
        CaregiverPair[] caregivers = new CaregiverPair[allPatients.length];
        List<Integer> parent1Genes = p1.getGenes();
        List<Integer> parent2Genes = p2.getGenes();
        CaregiverPair[] parent1Caregivers = p1.getCaregivers();
        CaregiverPair[] parent2Caregivers = p2.getCaregivers();
        //get patients from selected route;
        for (int i = 0; i < parent2Genes.size(); i++) {
            int gene = parent2Genes.get(i);
            CaregiverPair caregiverPair = parent2Caregivers[gene];
            if (caregiverPair.getFirst() == r || caregiverPair.getSecond() == r) {
                selectedRoute.add(gene);
                removedGenes.add(gene);
            }
        }
        //removing patients of selected route from parent
        for (int i = 0; i < parent1Genes.size(); i++) {
            int gene = parent1Genes.get(i);
            if(!selectedRoute.contains(gene)) {
                CaregiverPair caregiverPair = parent1Caregivers[gene];
                genes.add(gene);
                caregivers[gene] = caregiverPair;
            }
        }
        //inserting removed genes
        Collections.shuffle(removedGenes);

        Chromosome c2Temp = new Chromosome(genes, 0.0, caregivers, allCaregivers, false, true);
        double bestFitness;
        int bestPosition;
        int gene;
        CaregiverPair bestCaregiverPair;
        for (int i = 0; i < removedGenes.size(); i++) {
            gene = removedGenes.get(i);
            Patient patient = allPatients[gene];
            List<CaregiverPair> caregiverPairs = patient.getAllPossibleCaregiverCombinations();

            bestFitness = Double.MAX_VALUE;
            bestPosition = -1;
            bestCaregiverPair = null;
            for (int x = 0; x < caregiverPairs.size(); x++) {
                CaregiverPair caregiverPair = caregiverPairs.get(x);
                for (int j = 0; j <= genes.size(); j++) {
                    if(j>0){
                        CaregiverPair tempCaregiverPair = caregivers[genes.get(j-1)];
                        int first = tempCaregiverPair.getFirst();
                        int cFirst = caregiverPair.getFirst();
                        int second = tempCaregiverPair.getSecond();
                        int cSecond = caregiverPair.getSecond();
                        boolean firstCheck = first!=cFirst && first!=cSecond;
                        boolean secondCheck = second!=cFirst && second!=cSecond;
                        boolean thirdCheck = second==cSecond&&second==-1;
                        if(firstCheck&&secondCheck||firstCheck&&thirdCheck){
                            continue;
                        }
                    }
                    genes.add(j, gene);
                    c2Temp.setGenes(genes);
                    c2Temp.setCaregivers(caregiverPair, gene);
                    //Evaluate fitness
                    EvaluationFunction.CrossEvaluationNew(c2Temp,bestFitness);
//                    System.out.println(c2Temp.getFitness());
//                    System.out.println(c2Temp.getTotalTravelCost());
//                    System.out.println(c2Temp.getTotalTardiness());
//                    System.out.println(c2Temp.getHighestTardiness());
//                    System.out.println("--------------");
//                    EvaluationFunction.CrossEvaluation(c2Temp,bestFitness);
//                    System.out.println(c2Temp.getFitness());
//                    System.out.println(c2Temp.getTotalTravelCost());
//                    System.out.println(c2Temp.getTotalTardiness());
//                    System.out.println(c2Temp.getHighestTardiness());
//                    System.exit(1);
                    if (c2Temp.getFitness() < bestFitness||c2Temp.getFitness() == bestFitness&&rand.nextBoolean()) {
                        bestFitness = c2Temp.getFitness();
                        bestPosition = j;
                        bestCaregiverPair = caregiverPair;
                    }
                    genes.remove(Integer.valueOf(gene));
                }
            }
            if (bestCaregiverPair != null) {
                genes.add(bestPosition, gene);
                c2Temp.setGenes(genes);
                c2Temp.setCaregivers(bestCaregiverPair, gene);
                c2Temp.setFitness(bestFitness);
            }
        }
        EvaluationFunction.EvaluateFitness(c2Temp);
        return c2Temp;
    }
}
