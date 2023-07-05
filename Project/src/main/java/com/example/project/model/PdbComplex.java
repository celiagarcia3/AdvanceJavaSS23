package com.example.project.model;

import javafx.util.Pair;

import java.util.ArrayList;

public class PdbComplex {
    private ArrayList<PdbPolymer> polymers;

    // Constructor
    public PdbComplex() {
        polymers = new ArrayList<>();
    }

    // Getters and setters
    public ArrayList<PdbPolymer> getPolymers() {
        return polymers;
    }

    // Other methods
    public void addPolymer(PdbPolymer polymer) {
        polymers.add(polymer);
    }

    public void removePolymer(PdbPolymer polymer) {
        polymers.remove(polymer);
    }

    public int getPolymerCount() {
        return polymers.size();
    }
    public int getMonomerCount() {
        int numMonomers=0;
        for(PdbPolymer polymer: polymers){
            numMonomers += polymer.getSize();
        }
        return  numMonomers;
    }


    public PdbPolymer getPolymer(int index) {
        if (index >= 0 && index < polymers.size()) {
            return polymers.get(index);
        }
        return null;
    }
}
