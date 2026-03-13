package org.example.GA;

import com.sun.management.OperatingSystemMXBean;
import org.example.Configuration;
import org.example.Data.InstancesClass;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneticAlgorithm {
    private final int popSize;
    private final int gen;
    private final int TSRate;
    private final String selectTechnique;
    private final String crossType;
    private final String mutType;
    private final float mutRate;
    private final int numOfEliteSearch;
    private final double elitismRate;
    private final int elitismSize;
    private final List<Integer> eliteRandomList;
    private final double crossRate;
    private final int crossSize;
    private final int mutSize;
    private final InstancesClass data;
    private Chromosome bestChromosome;
    private final List<Chromosome> nextPopulation;
    private final List<Chromosome> tempPopulation;
    private final List<Chromosome> tempMutPopulation;
    private List<Chromosome> newPopulation;
    private final List<Chromosome> crossoverChromosomes;
    private final List<Chromosome> mutationChromosomes;
    private final double[] popProbabilities;
    private Map<Integer, Chromosome> LSChromosomes;
    private int terminator = 0;
    private final int patientLength;
    private final int caregiversNum;
    private final Random rand;
    private long startCpuTime;
    private long startTime;
    private final OperatingSystemMXBean osBean;
    private final int limit;
    private final CrossoverStrategy crossoverStrategy;
    private final MutationStrategy mutationStrategy;
    private final SelectionStrategy selectionStrategy;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public GeneticAlgorithm(Configuration config, long seed, int gen, InstancesClass data) {
        this.osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        this.data = data;
        rand = new Random(seed);
        this.numOfEliteSearch = config.getNumberOfElites();
        this.TSRate = config.getTSRate();
        this.popSize =config.getPopulationSize();
        this.gen = gen;
        selectTechnique = config.getSelectionMethod();
        this.mutType = config.getMutationMethod();
        this.crossType = config.getCrossoverMethod();
        this.crossoverStrategy = getCrossoverStrategy();
        this.mutationStrategy = getMutationStrategy();
        this.selectionStrategy = getSelectionStrategy();
        this.elitismRate = config.getElitismRate();
        this.elitismSize = (int) (elitismRate * popSize);
        this.eliteRandomList = new ArrayList<>(elitismSize);
        for (int i = 0; i < elitismSize; i++) {
            eliteRandomList.add(i);
        }
        this.crossRate = config.getCrossRate();
        this.crossSize = (int) (popSize* crossRate);
        this.mutRate = config.getMutRate();
        mutSize = (int) (mutRate *popSize);
        popProbabilities = new double[popSize];
        patientLength = data.getPatients().length;
        caregiversNum = data.getCaregivers().length;
        limit = Math.min(numOfEliteSearch, eliteRandomList.size());
        nextPopulation = new ArrayList<>(popSize);
        tempPopulation = new ArrayList<>(popSize);
        tempMutPopulation = new ArrayList<>(popSize);
        mutationChromosomes = Collections.synchronizedList(new ArrayList<>(popSize));
        crossoverChromosomes = Collections.synchronizedList(new ArrayList<>(popSize));
    }
    public Chromosome start() {
        System.out.printf("Population Size: %d, Generation: %d, SelectionType: %s TSRate: %d, Crossover type: %s\n CrossRate: %f, EliteRate: %f, MutType: %s Mutation Rate: %f\n", popSize, gen, selectTechnique, TSRate, crossType, crossRate, elitismRate, mutType, mutRate);
        bestChromosome = null;
        startTimer();
        System.out.println("Initializing Population ...");
        newPopulation = Population.initialize(popSize, patientLength, caregiversNum);
        System.out.printf("Population initialized: CPU Timer(s): %s Timer(s): %s\n", getTotalCPUTimeSeconds(), getTotalTimeSeconds());
        EvaluationFunction.EvaluateFitness(newPopulation);
        sortPopulation(newPopulation);
        performanceUpdate(0);
        for (int g = 1; g <= gen; g++) {
            elitism();
            crossover();
            mutation();
            update();
            performanceUpdate(g);
            if(patientLength<=100){
                if(terminator == patientLength/2) break;
            }else {
                if (terminator == 50) break;
            }
        }
        return newPopulation.get(0);
    }

    private void elitism() {
        nextPopulation.clear();
        sortPopulation(newPopulation);
        for (int i = 0; i < elitismSize; i++) {
            nextPopulation.add(newPopulation.get(i));
        }
    }

    private void update() {
        newPopulation.clear();
        newPopulation.addAll(nextPopulation);
        newPopulation.addAll(tempMutPopulation);
        sortPopulation(tempPopulation);
        for(Chromosome c : tempPopulation){
            if(newPopulation.size()<popSize){
                newPopulation.add(c);
            }else break;
        }
    }

    private void crossover() {
        tempPopulation.clear();
        crossoverStrategy.execute();
    }

    private void mutation() {
        tempMutPopulation.clear();
        mutationStrategy.execute();
    }
    @FunctionalInterface
    private interface CrossoverStrategy {
        void execute();
    }

    private CrossoverStrategy getCrossoverStrategy() {
        BestCostRouteCrossover.initialize(data);
        return this::bestCostRouteCrossover;
    }
    private void bestCostRouteCrossover() {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        crossoverChromosomes.clear();
        List<Callable<Void>> crossoverTasks = new ArrayList<>(crossSize);
        if(selectTechnique.equals("W")) {
            rouletteWheelSetup();
        }
        int index = 0;
        while(index < crossSize){
            Chromosome p1 = newPopulation.get(selectChromosome());
            Chromosome p2 = newPopulation.get(selectChromosome());
            int r1,r2;
            r1=rand.nextInt(caregiversNum);
            r2=rand.nextInt(caregiversNum);
            int finalR2 = r2;
//            BestCostRouteCrossover bc = new BestCostRouteCrossover(this, finalR2,p1,p2,rand);
//            bc.Crossover();
//            System.exit(1);
            crossoverTasks.add(()->{
                new BestCostRouteCrossover(this, finalR2,p1,p2,rand).run();
                return null;
            });
            index++;
            if (index < crossSize){
                int finalR1 = r1;
                crossoverTasks.add(() -> {
                    new BestCostRouteCrossover(this, finalR1, p2, p1, rand).run();
                    return null;
                });
                index++;
            }
        }
        invokeThreads(executor,crossoverTasks);
    }

    private void invokeThreads(ExecutorService service, List<Callable<Void>> crossoverTasks) {
        populateWithThreads(service, crossoverTasks, crossoverChromosomes, tempPopulation);
    }

    private void populateWithThreads(ExecutorService service, List<Callable<Void>> crossoverTasks, List<Chromosome> crossoverChromosomes, List<Chromosome> tempPopulation) {
        try {
            service.invokeAll(crossoverTasks);
            List<Chromosome> xChromosomes = crossoverChromosomes;
            synchronized (xChromosomes){
                tempPopulation.addAll(xChromosomes);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            service.shutdown();
        }
    }

    @FunctionalInterface
    private interface MutationStrategy{
        void execute();
    }

    private MutationStrategy getMutationStrategy() {
        SwapRouteMutation.initialize(data);
        return this::swapRouteMutation;
    }

    private void swapRouteMutation() {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mutationChromosomes.clear();
        List<Callable<Void>> mutationTasks = new ArrayList<>(mutSize);
        if(selectTechnique.equals("W")) {
            rouletteWheelSetup();
        }
        for(int i = 0; i < mutSize; i++){
            Chromosome p = newPopulation.get(selectChromosome());
            mutationTasks.add(()->{
                new SwapRouteMutation(this, p).run();
                return null;
            });
        }
        invokeMutationThreads(executor, mutationTasks);
    }

    private void invokeMutationThreads(ExecutorService service, List<Callable<Void>> mutationTasks) {
        populateWithThreads(service, mutationTasks, mutationChromosomes, tempMutPopulation);
    }

    @FunctionalInterface
    private interface SelectionStrategy{
        int execute();
    }
    private SelectionStrategy getSelectionStrategy() {
        return switch (selectTechnique) {
            case "T" -> this::tournamentSelection;
            case "W" -> this::rouletteSelection;
            default -> this::randomSelection;
        };
    }
    private int tournamentSelection() {
        int[] candidates = new int[TSRate];
        for (int i = 0; i < TSRate; i++) {
            candidates[i] = rand.nextInt(popSize);
        }

        // Find minimum index without full sorting
        int minIndex = 0;
        for (int i = 1; i < TSRate; i++) {
            if (newPopulation.get(candidates[i]).getFitness() <
                    newPopulation.get(candidates[minIndex]).getFitness()) {
                minIndex = i;
            }
        }
        return (Math.random() < 0.8) ? candidates[minIndex] :
                candidates[rand.nextInt(TSRate)];
    }
    private void rouletteWheelSetup(){
        double total = 0.0;
        double lambda = 1e-6;
        for (int i = 0; i < newPopulation.size(); i++){
            popProbabilities[i] = 1 / (newPopulation.get(i).getFitness()+lambda);
            total+=popProbabilities[i];
        }
        for(int i = 0; i < popProbabilities.length; i++){
            popProbabilities[i] = (popProbabilities[i] / total);
        }
    }

    private int rouletteSelection() {
        double rand = Math.random();
        double cumulativeFitness = 0.0;
        for(int i = 0; i < newPopulation.size(); i++){
            cumulativeFitness +=popProbabilities[i];
            if(rand<=cumulativeFitness)
                return i;
        }
        return (int)(rand*popSize);
    }

    private int randomSelection() {
        return rand.nextInt(popSize);
    }

    private int selectChromosome() {
        return selectionStrategy.execute();
    }
    private void sortPopulation(List<Chromosome> population) {
        population.sort(Comparator.comparingDouble(Chromosome::getFitness));
    }

    public List<Chromosome> getCrossoverChromosomes() {
        return crossoverChromosomes;
    }

    public List<Chromosome> getMutationChromosomes() {
        return mutationChromosomes;
    }

    private void performanceUpdate(int generation) {
        sortPopulation(newPopulation);
        if (generation > 0 && bestChromosome.getFitness() == newPopulation.get(0).getFitness()) {
            terminator++;
        } else {
            terminator = 0;
        }
        bestChromosome = newPopulation.get(0);
        double averageFitness = newPopulation.stream().mapToDouble(Chromosome::getFitness).sum() / popSize;

        System.out.println("Time at: " + getTotalTimeSeconds() + " CPU Timer " + String.format("%.3f", getTotalCPUTimeSeconds()) + " seconds Generation " + generation + " Generation without Improvement: "+ terminator +" Best fitness: " + Math.round(bestChromosome.getFitness() * 1000.0) / 1000.0  + " Average fitness: " + averageFitness);
        if (generation == gen) {
//            bestChromosome.showSolution(generation);
            System.out.println("Time at: " + getTotalTimeSeconds() + " CPU Timer " + String.format("%.3f", getTotalCPUTimeSeconds()) + " seconds Generation " + generation + " Fitness: " + Math.round(bestChromosome.getFitness() * 1000.0) / 1000.0 + " Total Distance: " + bestChromosome.getTotalTravelCost() + " Total Tardiness: " + bestChromosome.getTotalTardiness() + " Highest Tardiness: " + bestChromosome.getHighestTardiness());
        }
    }

    public void startTimer() {
        this.startCpuTime = osBean.getProcessCpuTime();
        this.startTime = System.currentTimeMillis();
    }

    public double getTotalCPUTimeSeconds() {
        long endCpuTime = osBean.getProcessCpuTime();
        return (endCpuTime - startCpuTime) / 1_000_000_000.0;
    }

    public double getTotalTimeSeconds() {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1_000.0;
    }

}
