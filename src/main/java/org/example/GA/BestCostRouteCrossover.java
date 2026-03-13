package org.example;

import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    private final int[] routeEndPoint;
    private final double[] routesCurrentTime;
    private final double[] highestAndTotalTardiness;
    private final Set<Integer> track;

    public BestCostRouteCrossoverSwap(GeneticAlgorithm ga, int r, Chromosome p1, Chromosome p2, Random rand) {
        this.ga = ga;
        this.r = r;
        this.p1 = p1;
        this.p2 = p2;
        this.rand = rand;
        this.routeEndPoint = new int[allCaregivers];
        this.routesCurrentTime = new double[allCaregivers];
        this.highestAndTotalTardiness = new double[2];
        this.track = new HashSet<>(100);
    }

    public static void initialize(InstancesClass instance){
        allPatients = instance.getPatients();
        distances = instance.getDistances();
        allCaregivers = instance.getCaregivers().length;
        dataset = instance;
    }
    public Chromosome Crossover(){
        return null;
    }
}
