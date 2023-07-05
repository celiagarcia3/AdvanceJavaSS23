package com.example.project.model;

import javafx.util.Pair;

import java.util.*;

public class PdbPolymer {
    private List<PdbMonomer> monomers;
    private int number;
    private String label;

    public PdbPolymer(List<PdbMonomer> monomers, int number, String label) {
        this.monomers = monomers;
        this.number = number;
        this.label = label;
    }

    // Getters and setters

    public List<PdbMonomer> getMonomers() {
        return monomers;
    }
    public int getSize(){
        return monomers.size();
    }

    public void setMonomers(ArrayList<PdbMonomer> monomers) {
        this.monomers = monomers;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    //Method to get the list of atoms of the whole polymer
    public ArrayList<PdbAtom> getAllAtoms() {
        ArrayList<PdbAtom> allAtoms = new ArrayList<>();
        for (PdbMonomer monomer : monomers) {
            allAtoms.addAll(monomer.getAtoms());
        }
        return allAtoms;
    }

    //Method to get the pairs of atoms that are bounded by the distance between them
    public Map<PdbMonomer, List<Pair<PdbAtom,PdbAtom>>> getBondedAtoms() {

        Map<PdbMonomer, List<Pair<PdbAtom, PdbAtom>>> bondedAtoms = new HashMap<>();

        getMonomers().forEach(monomerA -> {
            List<Pair<PdbAtom, PdbAtom>> bonds = new ArrayList<>();
            monomerA.getAtoms().forEach(atomA -> {
                monomerA.getAtoms().forEach(atomB -> {
                    if(!atomB.equals(atomA)) {
                        double distance = atomA.getCoordinates().distance(atomB.getCoordinates());
                        if (distance <= 2) {
                            Pair<PdbAtom, PdbAtom> bond = new Pair<>(atomA, atomB);
                            if (!bonds.contains(bond)) {
                                bonds.add(bond);
                            }
                        }
                    }
                });

                getMonomers().forEach(monomerB -> {
                    if(!monomerB.equals(monomerA)) {
                        monomerB.getAtoms().forEach(atomB -> {
                            if (!atomB.equals(atomA)) {
                                double distance = atomA.getCoordinates().distance(atomB.getCoordinates());
                                if (distance <= 2) {
                                    Pair<PdbAtom, PdbAtom> bond = new Pair<>(atomA, atomB);
                                    if (!bonds.contains(bond)) {
                                        bonds.add(bond);
                                    }
                                }
                            }
                        });
                    }
                });
            });

            bondedAtoms.put(monomerA, bonds); // Associate monomerA with its bonds

        });

        return bondedAtoms;
    }

    // Method to get a list of pairs (monomer, monomer) for each two connected monomers
    public List<Pair<PdbMonomer, PdbMonomer>> getConnectedMonomerPairs() {
        List<Pair<PdbMonomer, PdbMonomer>> connectedPairs = new ArrayList<>();

        // Retrieve the map of bonded atoms
        Map<PdbMonomer, List<Pair<PdbAtom, PdbAtom>>> bondedAtoms = getBondedAtoms();

        // Iterate through each monomer and its bonded atoms
        for (Map.Entry<PdbMonomer, List<Pair<PdbAtom, PdbAtom>>> entry : bondedAtoms.entrySet()) {
            PdbMonomer monomerA = entry.getKey();
            List<Pair<PdbAtom, PdbAtom>> bonds = entry.getValue();

            // Iterate through each bonded atom in the monomer
            for (Pair<PdbAtom, PdbAtom> bond : bonds) {
                PdbAtom atomA = bond.getKey();
                PdbAtom atomB = bond.getValue();

                // Find the monomer containing the bonded atom B
                PdbMonomer monomerB = getMonomerForAtom(atomB);

                // If a connected monomer is found, create a pair and add it to the list
                if (monomerB != null) {
                    Pair<PdbMonomer, PdbMonomer> connectedPair = new Pair<>(monomerA, monomerB);
                    if (!connectedPairs.contains(connectedPair)) {
                        connectedPairs.add(connectedPair);
                    }
                }
            }
        }

        // Return the list of connected monomer pairs
        return connectedPairs;
    }

    // Helper method to get the monomer containing a specific atom
    private PdbMonomer getMonomerForAtom(PdbAtom atom) {
        for (PdbMonomer monomer : monomers) {
            if (monomer.getAtoms().contains(atom)) {
                return monomer;
            }
        }
        return null;
    }




}
