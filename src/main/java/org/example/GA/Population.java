package org.example.GA;

import org.example.Data.Patient;
import org.example.Main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Population {
    public static final Patient[] allPatients = Main.instance.getPatients();

    public static ArrayList<Chromosome> initialize(int populationSize, int chromosomeLength, int caregiversCount) {
        List<Integer> patients= new ArrayList<>();
        CaregiverPair[] caregivers = new CaregiverPair[chromosomeLength];
        ArrayList<Chromosome> population = new ArrayList<>();
        for (int s = 0; s < chromosomeLength; s++) {
            patients.add(s);
        }
        for (int i =  0; i < populationSize; i++) {
            Collections.shuffle(patients);
            for(int j = 0; j < chromosomeLength; j++){
                int index = patients.get(j);
                Patient p = allPatients[index];
                caregivers[index] = p.getRandomCaregiverPair();
            }
            Chromosome c = new Chromosome(patients,0.0,caregivers,caregiversCount,true,true);
            population.add(c);
        }
        return population;
    }
}
