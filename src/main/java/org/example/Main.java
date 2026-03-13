package org.example;

import org.example.Data.InstancesClass;
import org.example.Data.ReadData;
import org.example.GA.Chromosome;
import org.example.GA.EvaluationFunction;
import org.example.GA.GeneticAlgorithm;

import java.io.File;
import java.io.PrintStream;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static InstancesClass instance;
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java GeneticAlgorithmRunner <config-file>");
            return;
        }
        int runs =1;
        double avg = 0.0;
        double best = Double.MAX_VALUE;
        for (int i = 1; i <= runs; i++) {
            long randomSeed;
            long instanceNumber;
            String instanceName;

            try {
                // Read configuration file
                File configFile = new File(args[0]);
                if (args[0].contains("HHCRSP")) {
                    Config1 config1 = Config1.read(configFile);
                    instanceNumber = config1.getInstanceIndex();
                    instanceName = config1.getInstanceName();
                    int runCount = Integer.parseInt(args[1]);
                    randomSeed = System.currentTimeMillis() + runCount;
                    //create result directory
                    String resultDir = "src/main/java/org/example/Config_" + instanceName + "_" + instanceNumber + "_results";
                    new File(resultDir).mkdirs();
                    // Read dataset
                    PrintStream fileout = new PrintStream(resultDir + "/Result_" + instanceName + "_" + instanceNumber + "_" + runCount + "_" + "_" + randomSeed + ".txt");
                    System.setOut(fileout);
                    System.out.printf("Config : instanceNumber=%d, seed=%d\n", instanceNumber, randomSeed);

                    instance = ReadData.read(new File("src/main/java/org/example/Data/kummer/" + instanceName));

                } else {
                    Config config = Config.read(configFile);

                    // Extract parameters from JSON
                    int problemSize = config.getProblemSize();
                    instanceNumber = config.getInstanceIndex();

                    int runCount = Integer.parseInt(args[1]);
                    randomSeed = System.currentTimeMillis() + runCount;

                    String[] Instances = {"10", "25", "50", "75", "100", "200", "300"};
                    instanceName = Instances[problemSize];

//                    //create result directory
//                    String resultDir = "src/main/java/org/example/BCRCM_" + problemSize + "_" + instanceNumber + "_results";
//                    new File(resultDir).mkdirs();
//
//                    // Read dataset
//                    PrintStream fileout = new PrintStream(resultDir + "/Result_" + instanceName + "_" + instanceNumber + "_" + i + "_" + randomSeed + ".txt");
//                    System.setOut(fileout);
                    System.out.printf("Config Parameters: ProblemSize=%d, instanceNumber=%d, seed=%d\n", problemSize, instanceNumber, randomSeed);

                    instance = ReadData.read(new File("src/main/java/org/example/Data/instance/" + instanceName + "_" + instanceNumber + ".json"));
                }

                Configuration config = parseArguments.getConfiguration(args);

                GeneticAlgorithm ga = new GeneticAlgorithm(config, i,600, instance);
                Chromosome bestChromosome = ga.start();

                avg += bestChromosome.getFitness();
                if(bestChromosome.getFitness() <best) {
                    best = bestChromosome.getFitness();
                }

                assert bestChromosome != null;
                EvaluationFunction.EvaluateFitness(bestChromosome);
                System.out.println("----------------- Solution ----------------------");
                System.out.println("Instance_" + instanceName + "_" + instanceNumber + " Best Fitness: " + Math.round(bestChromosome.getFitness() * 1000.0) / 1000.0);
                System.out.println("Total Distance: " + bestChromosome.getTotalTravelCost() + " Total Tardiness: " + bestChromosome.getTotalTardiness() + " Highest Tardiness: " + bestChromosome.getHighestTardiness());
                System.out.println(bestChromosome);
                bestChromosome.showSolution(0);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        System.out.println("Best: " + best);
        System.out.println("Average Fitness: " + avg/runs);
        System.out.println("--------------- End ----------------");

    }
}