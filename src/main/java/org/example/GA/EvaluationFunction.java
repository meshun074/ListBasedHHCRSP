package org.example.GA;

import org.example.Data.Patient;
import org.example.Main;

import java.util.Arrays;
import java.util.List;

public class EvaluationFunction {
    private static final Patient[] allPatients = Main.instance.getPatients();
    private static final double[][] distancesMatrix = Main.instance.getDistances();
    private static final int allCaregiverNum = Main.instance.getCaregivers().length;
    public static void EvaluateFitness(List<Chromosome> population) {
        for (Chromosome ch : population) {
            EvaluateFitness(ch);
        }
    }

    public static void EvaluateFitness(Chromosome ch){
        Shift[] caregiverRoute = ch.getCaregiversRoute();
        List<Integer> genes  = ch.getGenes();
        CaregiverPair[] caregivers = ch.getCaregivers();
        for (int i = 0; i < caregiverRoute.length; i++) {
            caregiverRoute[i].resetShift();
        }
        ch.setTotalTravelCost(0.0);
        ch.setFitness(0.0);
        ch.setHighestTardiness(0.0);
        ch.setTotalTardiness(0.0);
        for (int i = 0; i < genes.size(); i++) {
            Patient p = allPatients[genes.get(i)];
            CaregiverPair cp = caregivers[genes.get(i)];
            updateRoutes(ch, caregiverRoute, cp, p, genes.get(i));
        }
        for (Shift c : caregiverRoute) {
            int lastVisitedPatient = c.getRoute().isEmpty()?-1:c.getRoute().get(c.getRoute().size()-1);
            double distance = distancesMatrix[lastVisitedPatient+1][0];
            ch.updateTotalTravelCost(distance);
            c.updateTravelCost(distance);
        }
        updateFitness(ch);
    }
    public static void CrossEvaluation(Chromosome ch, double bestFitness) {
        Shift[] caregiverRoute = ch.getCaregiversRoute();
        List<Integer> genes  = ch.getGenes();
        CaregiverPair[] caregivers = ch.getCaregivers();
        for (int i = 0; i < caregiverRoute.length; i++) {
            caregiverRoute[i].resetShift();
        }
        ch.setTotalTravelCost(0.0);
        ch.setFitness(0.0);
        ch.setHighestTardiness(0.0);
        ch.setTotalTardiness(0.0);
        for (int i = 0; i < genes.size(); i++) {
            Patient p = allPatients[genes.get(i)];
            CaregiverPair cp = caregivers[genes.get(i)];
            updateRoutes(ch, caregiverRoute, cp, p, genes.get(i));
            updateFitness(ch);
            if(ch.getFitness() > bestFitness){
                return;
            }
        }
        for (Shift c : caregiverRoute) {
            int lastVisitedPatient = c.getRoute().isEmpty()?-1:c.getRoute().get(c.getRoute().size()-1);
            double distance = distancesMatrix[lastVisitedPatient+1][0];
            ch.updateTotalTravelCost(distance);
            c.updateTravelCost(distance);
        }
        updateFitness(ch);
    }
    private static void updateRoutes(Chromosome ch, Shift[] caregiverRoute, CaregiverPair cp, Patient p, int index) {
        Shift c1 = caregiverRoute[cp.getFirst()];
        int currentLocation1 = c1.getRoute().isEmpty() ? 0: c1.getRoute().get(c1.getRoute().size()-1) + 1;
        int nextLocation = index + 1;
        double arrivalTime1 = c1.getCurrentTime().get(c1.getCurrentTime().size()-1) + distancesMatrix[currentLocation1][nextLocation];
        double startTime1 = Math.max(arrivalTime1,p.getTime_window()[0]);

        if(p.getRequired_caregivers().length>1){
            Shift c2 = caregiverRoute[cp.getSecond()];
            int currentLocation2 = c2.getRoute().isEmpty() ? 0: c2.getRoute().get(c2.getRoute().size()-1) + 1;
            double arrivalTime2 = c2.getCurrentTime().get(c2.getCurrentTime().size()-1) + distancesMatrix[currentLocation2][nextLocation];
            double startTime2 = Math.max(arrivalTime2,p.getTime_window()[0]);
            if(p.getSynchronization().getType().equals("simultaneous")){
                double startTime = Math.max(startTime1,startTime2);
                double tardiness = startTime - p.getTime_window()[1];
                tardiness = 2 * Math.max(tardiness,0);
                double highestTardiness = Math.max(tardiness/2,ch.getHighestTardiness());
                ch.setHighestTardiness(highestTardiness);
                ch.updateTotalTardiness(tardiness);
                c1.setCurrentTime(startTime+p.getRequired_caregivers()[0].getDuration());
                c1.updateTardiness(tardiness/2);
                c2.setCurrentTime(startTime+p.getRequired_caregivers()[1].getDuration());
                c2.updateTardiness(tardiness/2);
            }else {
                startTime2 = Math.max(startTime2, startTime1+p.getSynchronization().getDistance()[0]);
                if(startTime2 -startTime1>p.getSynchronization().getDistance()[1])
                    startTime1 = startTime2 - p.getSynchronization().getDistance()[1];
                double tardiness1 = Math.max(0, startTime1-p.getTime_window()[1]);
                double tardiness2 = Math.max(0, startTime2-p.getTime_window()[1]);
                ch.updateTotalTardiness(tardiness1+tardiness2);
                double maxTardiness = Math.max(tardiness1,tardiness2);
                maxTardiness = Math.max(maxTardiness,ch.getHighestTardiness());
                ch.setHighestTardiness(maxTardiness);
                c1.setCurrentTime(startTime1+p.getRequired_caregivers()[0].getDuration());
                c1.updateTardiness(tardiness1);
                c2.setCurrentTime(startTime2+p.getRequired_caregivers()[1].getDuration());
                c2.updateTardiness(tardiness2);
            }
            double travelCost = distancesMatrix[currentLocation1][nextLocation] + distancesMatrix[currentLocation2][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            c1.updateRoute(index);
            c1.updateTravelCost(distancesMatrix[currentLocation1][nextLocation]);
            c2.updateRoute(index);
            c2.updateTravelCost(distancesMatrix[currentLocation2][nextLocation]);
        }else {
            double tardiness = startTime1-p.getTime_window()[1];
            tardiness = Math.max(0, tardiness);
            double maxTardiness = Math.max(tardiness,ch.getHighestTardiness());
            ch.setHighestTardiness(maxTardiness);
            ch.updateTotalTardiness(tardiness);
            double travelCost = distancesMatrix[currentLocation1][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            c1.setCurrentTime(startTime1+p.getRequired_caregivers()[0].getDuration());
            c1.updateTardiness(tardiness);
            c1.updateTravelCost(travelCost);
            c1.updateRoute(index);
        }
    }

    public static void CrossEvaluationNew(Chromosome ch, double bestFitness) {
        int[] routeEndPoint = new int[allCaregiverNum];
        double[] routesCurrentTime = new double[allCaregiverNum];
        Arrays.fill(routeEndPoint,-1);
        List<Integer> genes  = ch.getGenes();
        CaregiverPair[] caregivers = ch.getCaregivers();
        ch.setTotalTravelCost(0.0);
        ch.setFitness(0.0);
        ch.setHighestTardiness(0.0);
        ch.setTotalTardiness(0.0);
        for (int i = 0; i < genes.size(); i++) {
            Patient p = allPatients[genes.get(i)];
            CaregiverPair cp = caregivers[genes.get(i)];
            updateRoutes(ch, routeEndPoint, routesCurrentTime, cp, p, genes.get(i));
            updateFitness(ch);
            if(ch.getFitness() > bestFitness){
                return;
            }
        }
        for (int c : routeEndPoint) {
            double distance = distancesMatrix[c+1][0];
            ch.updateTotalTravelCost(distance);
        }
        updateFitness(ch);
    }

    private static void updateRoutes(Chromosome ch, int[] routeEndPoint, double[] routesCurrentTime, CaregiverPair cp, Patient p, int index) {
        int c1 = cp.getFirst();
        int currentLocation1 = routeEndPoint[c1]==-1 ? 0: routeEndPoint[c1] + 1;
        int nextLocation = index + 1;
        double arrivalTime1 = routesCurrentTime[c1] + distancesMatrix[currentLocation1][nextLocation];
        double startTime1 = Math.max(arrivalTime1,p.getTime_window()[0]);

        if(p.getRequired_caregivers().length>1){
            int c2 = cp.getSecond();
            int currentLocation2 = routeEndPoint[c2]==-1 ? 0: routeEndPoint[c2] + 1;
            double arrivalTime2 = routesCurrentTime[c2] + distancesMatrix[currentLocation2][nextLocation];
            double startTime2 = Math.max(arrivalTime2,p.getTime_window()[0]);
            if(p.getSynchronization().getType().equals("simultaneous")){
                double startTime = Math.max(startTime1,startTime2);
                double tardiness = startTime - p.getTime_window()[1];
                tardiness = 2 * Math.max(tardiness,0);
                double highestTardiness = Math.max(tardiness/2,ch.getHighestTardiness());
                ch.setHighestTardiness(highestTardiness);
                ch.updateTotalTardiness(tardiness);
                startTime1 = startTime;
                startTime2 = startTime;
            }else {
                startTime2 = Math.max(startTime2, startTime1+p.getSynchronization().getDistance()[0]);
                if(startTime2 -startTime1>p.getSynchronization().getDistance()[1])
                    startTime1 = startTime2 - p.getSynchronization().getDistance()[1];
                double tardiness1 = Math.max(0, startTime1-p.getTime_window()[1]);
                double tardiness2 = Math.max(0, startTime2-p.getTime_window()[1]);
                ch.updateTotalTardiness(tardiness1+tardiness2);
                double maxTardiness = Math.max(tardiness1,tardiness2);
                maxTardiness = Math.max(maxTardiness,ch.getHighestTardiness());
                ch.setHighestTardiness(maxTardiness);
            }
            routesCurrentTime[c1] = startTime1+p.getRequired_caregivers()[0].getDuration();
            routesCurrentTime[c2] = startTime2+p.getRequired_caregivers()[1].getDuration();
            double travelCost = distancesMatrix[currentLocation1][nextLocation] + distancesMatrix[currentLocation2][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            routeEndPoint[c1]=index;
            routeEndPoint[c2]=index;
        }else {
            double tardiness = startTime1-p.getTime_window()[1];
            tardiness = Math.max(0, tardiness);
            double maxTardiness = Math.max(tardiness,ch.getHighestTardiness());
            ch.setHighestTardiness(maxTardiness);
            ch.updateTotalTardiness(tardiness);
            double travelCost = distancesMatrix[currentLocation1][nextLocation];
            ch.updateTotalTravelCost(travelCost);
            routesCurrentTime[c1]=startTime1+p.getRequired_caregivers()[0].getDuration();
            routeEndPoint[c1] = index;
        }
    }

    private static void updateFitness(Chromosome ch){
        double fitness = (1/3d*ch.getTotalTravelCost())+(1/3d*ch.getTotalTardiness())+(1/3d*ch.getHighestTardiness());
        ch.setFitness(fitness);
    }
}
