package org.example;

import org.example.Data.Caregiver;
import org.example.Data.InstancesClass;
import org.example.Data.Patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SwapRouteMutation implements Runnable {
    @Override
    public void run() {
        ga.getMutationChromosomes().add(mutate());
    }
    public final GeneticAlgorithm ga;
    private final Random rand;
    private final Chromosome ch;
    private final List<Integer> chGenes;
    private static Patient[] allPatients;
    private static Caregiver[] allCaregivers;
    private final CaregiverPair[] caregiverPairs;

    public static void initialize(InstancesClass instances) {
        allPatients = instances.getPatients();
        allCaregivers = instances.getCaregivers();
    }
    public SwapRouteMutation(GeneticAlgorithm ga, Chromosome ch) {
        this.ga = ga;
        this.rand =new Random(System.nanoTime());
        this.ch = ch;
        this.chGenes =new ArrayList<>(ch.getGenes());
        this.caregiverPairs = ch.getCaregivers();
    }
    public Chromosome mutate() {
        Chromosome mutatedChromosome;
        CaregiverPair[] mutatedCaregiverPair =new CaregiverPair[caregiverPairs.length];

        int[] routeIndices = selectMutationRoutes();
        int r1 = routeIndices[0];
        int r2 = routeIndices[1];
        for(int p : chGenes) {
            Patient patient = allPatients[p];
            CaregiverPair caregiverPair = caregiverPairs[p];
            if(caregiverPair.getSecond()>=0) {
                if (caregiverPair.getFirst() == r1 || caregiverPair.getFirst() == r2 || caregiverPair.getSecond() == r1 || caregiverPair.getSecond() == r2) {
                    Set<Integer> firstCaregiverSet = patient.getPossibleFirstCaregiver();
                    Set<Integer> secondCaregiverSet = patient.getPossibleSecondCaregiver();
                    if(caregiverPair.getFirst()== r1 && caregiverPair.getSecond()==r2 || caregiverPair.getFirst()== r2 && caregiverPair.getSecond()==r1 ) {
                        if(caregiverPair.getFirst()== r1 && caregiverPair.getSecond()==r2) {
                            if(firstCaregiverSet.contains(r2)&&secondCaregiverSet.contains(r1)) {
                                mutatedCaregiverPair[p] = new CaregiverPair(r2,r1);
                            }
                            else {
                                mutatedCaregiverPair[p] = caregiverPair;
                            }
                        }else {
                            if(firstCaregiverSet.contains(r1)&&secondCaregiverSet.contains(r2)) {
                                mutatedCaregiverPair[p] = new CaregiverPair(r1,r2);
                            }
                            else {
                                mutatedCaregiverPair[p] = caregiverPair;
                            }
                        }
                    }
                    else if (caregiverPair.getFirst() == r1 || caregiverPair.getSecond() == r1) {
                        compactibilityCheck(mutatedCaregiverPair, r1, r2, p, caregiverPair, firstCaregiverSet, secondCaregiverSet);
                    } else {
                        compactibilityCheck(mutatedCaregiverPair, r2, r1, p, caregiverPair, firstCaregiverSet, secondCaregiverSet);
                    }
                }else {
                    mutatedCaregiverPair[p] = caregiverPair;
                }

            }else  {
                if (caregiverPair.getFirst() == r1||caregiverPair.getFirst() == r2) {
                    Set<Integer> firstCaregiverSet = patient.getPossibleFirstCaregiver();
                    if (caregiverPair.getFirst() == r1) {
                       if(firstCaregiverSet.contains(r2)) {
                           mutatedCaregiverPair[p] = new CaregiverPair(r2,-1);
                       }else{
                           mutatedCaregiverPair[p] = caregiverPair;
                       }

                    }else {
                        if(firstCaregiverSet.contains(r1)) {
                            mutatedCaregiverPair[p] = new CaregiverPair(r1,-1);
                        }else{
                            mutatedCaregiverPair[p] = caregiverPair;
                        }
                    }
                }else {
                    mutatedCaregiverPair[p] = caregiverPair;
                }
            }
        }
        mutatedChromosome = new Chromosome(chGenes,0.0,mutatedCaregiverPair,caregiverPairs.length,false,true);
        return mutatedChromosome;
    }

    private void compactibilityCheck(CaregiverPair[] mutatedCaregiverPair, int r1, int r2, int p, CaregiverPair caregiverPair, Set<Integer> firstCaregiverSet, Set<Integer> secondCaregiverSet) {
        if (caregiverPair.getFirst() == r1) {
            if(firstCaregiverSet.contains(r2)&&secondCaregiverSet.contains(caregiverPair.getSecond())) {
                mutatedCaregiverPair[p] = new CaregiverPair(r2, caregiverPair.getSecond());
            }else {
                mutatedCaregiverPair[p] = caregiverPair;
            }
        } else {
            if(firstCaregiverSet.contains(caregiverPair.getFirst())&&secondCaregiverSet.contains(r2)) {
                mutatedCaregiverPair[p] = new CaregiverPair(caregiverPair.getFirst(), r2);
            }else {
                mutatedCaregiverPair[p] = caregiverPair;
            }
        }
    }

    private int[] selectMutationRoutes() {
        int r1 = rand.nextInt(allCaregivers.length);
        int r2;
        do {
            r2 = rand.nextInt(allCaregivers.length);
        } while (r1 == r2);
        return new int[]{r1, r2};
    }
}
