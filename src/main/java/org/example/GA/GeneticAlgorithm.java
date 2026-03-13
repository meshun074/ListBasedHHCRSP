package org.example;

import com.sun.management.OperatingSystemMXBean;
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

    @FunctionalInterface
    private interface CrossoverStrategy {
        void execute();
    }

    private CrossoverStrategy getCrossoverStrategy() {
        BestCostRouteCrossover.initialize(data);
        return this::bestCostRouteCrossover;
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
        try {
            service.invokeAll(mutationTasks);
            List<Chromosome> xChromosomes = mutationChromosomes;
            synchronized (xChromosomes){
                tempMutPopulation.addAll(xChromosomes);
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            service.shutdown();
        }
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

}
