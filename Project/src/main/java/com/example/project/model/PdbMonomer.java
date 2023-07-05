package com.example.project.model;


import java.util.List;

public class PdbMonomer {
    private List<PdbAtom> atoms;
    private String label;
    int index;
    private String secondaryStructure;

    public PdbMonomer(List<PdbAtom> atoms, String label, int index) {
        this.atoms = atoms;
        this.label = label;
        this.index = index;
        //this.secondaryStructure = secondaryStructure;
    }

    // Getters and setters
    public List<PdbAtom> getAtoms() {
        return atoms;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() { return index;}

    public String getSecondaryStructure() {
        return secondaryStructure;
    }

    public void setSecondaryStructure(String secondaryStructure) {
        this.secondaryStructure = secondaryStructure;
    }

    //Method to get just the alpha carbon
    public PdbAtom getA() {
        for (PdbAtom atom : getAtoms()) {
            if (atom.getRole().equals("CA")) {
                return atom;
            }
        }
        return null;
    }

    //Method to get the beta carbon
    public PdbAtom getB() {
        for (PdbAtom atom : getAtoms()) {
            if (atom.getRole().equals("CB")) {
                return atom;
            }
        }
        return null;
    }




}
