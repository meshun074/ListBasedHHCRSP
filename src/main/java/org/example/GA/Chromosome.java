package org.example;

import org.example.BCRCBRKGA.RKChromosome;
import org.example.Data.InstancesClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class Chromosome {
    private List<Integer> genes;
    private double fitness;
    private double totalTravelCost;
    private double totalTardiness;
    private long moveID;
    private double highestTardiness;
    private Shift[] caregiversRoute;
    private final CaregiverPair[] caregivers;
    private final InstancesClass instances = Main.instance;

    public Chromosome(int caregivers, int patients) {
        this.caregivers = new CaregiverPair[caregivers];
        fitness = 0;
        genes = new ArrayList<>(patients);
        caregiversRoute = new Shift[caregivers];
    }

    public Chromosome(List<Integer> genes, double fitness, CaregiverPair[] caregivers, int caregiversNumber, boolean isNew, boolean newShift) {
        if (isNew) {
            this.genes = new ArrayList<>(genes);
            this.caregivers = Arrays.copyOf(caregivers, genes.size());
        }else {
            this.genes = genes;
            this.caregivers = caregivers;
        }
        this.fitness = fitness;
        if(newShift)
            caregiversRoute = new Shift[caregiversNumber];
        for(int i=0;i<caregiversNumber;i++){
            Shift s = new Shift(instances.getCaregivers()[i],new ArrayList<>(),0.0);
            caregiversRoute[i]=s;
        }
    }

    public long getMoveID() {
        return moveID;
    }

    public void setMoveID(long moveID) {
        this.moveID = moveID;
    }

    public Shift[] getCaregiversRoute() {
        return caregiversRoute;
    }

    public void setCaregiversRoute(Shift[] caregiversRoute) {
        this.caregiversRoute = caregiversRoute;
    }

    public double getHighestTardiness() {
        return highestTardiness;
    }

    public void setHighestTardiness(double highestTardiness) {
        this.highestTardiness = highestTardiness;
    }

    public double getTotalTardiness() {
        return totalTardiness;
    }

    public void setTotalTardiness(double totalTardiness) {
        this.totalTardiness = totalTardiness;
    }
    public void updateTotalTardiness(double totalTardiness) {
        this.totalTardiness += totalTardiness;
    }

    public double getTotalTravelCost() {
        return totalTravelCost;
    }

    public void setTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost = totalTravelCost;
    }
    public void updateTotalTravelCost(double totalTravelCost) {
        this.totalTravelCost += totalTravelCost;
    }


    public CaregiverPair[] getCaregivers() {
        return caregivers;
    }
    public void setCaregivers(CaregiverPair caregiverPair, int gene) {
       this.caregivers[gene] = caregiverPair;
    }

    public double getFitness() {
        return fitness;
    }
    public double getOriginalFitness() {
        return (1/3d*totalTravelCost)+(1/3d*totalTardiness)+(1/3d*highestTardiness);
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public LinkedHashSet<Integer> getGenesSet() {
        return new LinkedHashSet<>(genes);
    }
    public List<Integer> getGenes() {
        return genes;
    }
    public ArrayList<Integer> getGenesCopy() {
        return new ArrayList<>(genes);
    }

    public void setNewGenes(List<Integer> genes) {
        this.genes = new ArrayList<>(genes);
    }
    public void setGenes(List<Integer> genes) {
        this.genes = genes;
    }
    public void updateGenes(ArrayList<Integer> genes){
        this.genes=genes;
    }
    public void showSolution(int index) {
//        System.out.print("\n Best Solution : "+index+" "+fitness+" "+totalTravelCost+" "+totalTardiness+" "+highestTardiness+"\n");
        System.out.println("\n Best Solution: " + index+"\n");
        for(int i=0;i< caregiversRoute.length;i++){
            Shift Caregiver = caregiversRoute[i];
            System.out.println("Route - "+ Caregiver.getRoute());
            System.out.println("Travel Cost to patients\n"+Caregiver.getTravelCost());
            System.out.println("Service completed time at patients\n"+Caregiver.getCurrentTime());
//            System.out.println("Route total tardiness: "+Caregiver.getTardiness());
            System.out.println("Route total tardiness: "+Caregiver.getTardiness().get(Caregiver.getTardiness().size()-1)+" Route Highest tardiness: "+Caregiver.getMaxTardiness().get(Caregiver.getMaxTardiness().size()-1));
//            System.out.println("Waiting Time: "+Caregiver.getTotalWaitingTime());
//            System.out.println("Overtime: "+Caregiver.getOvertime());
            System.out.println();
        }

    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<genes.size();i++){
           sb.append(String.format("%d (%d, %d)\t", genes.get(i), caregivers[genes.get(i)].getFirst(), caregivers[genes.get(i)].getSecond()));
        }

    return sb.toString();}
}
