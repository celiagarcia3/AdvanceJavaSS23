package com.example.project.model;

import javafx.geometry.Point3D;

import java.io.IOException;
import java.util.*;

import com.example.project.model.PdbAtom.AtomicSpecies;

public class ReadPDB {
    public static List<PdbComplex> readPDBfile(String pdbfile) throws IOException {

        //Split the pdb file through \n
        String[] content = pdbfile.split("\n");

        // List to store atom lists for each model
        List<List<PdbAtom>> models = new ArrayList<>();

        // List to store atoms for the current model
        List<PdbAtom> currentModelAtoms = new ArrayList<>();

        for (String line : content) {
            if (line.startsWith("ATOM")) {
                // Add the atom to the current model's atoms list
                PdbAtom atom = parseAtom(line);
                currentModelAtoms.add(atom);
            } else if (line.startsWith("ENDMDL")) {
                // End of a model, add the current model's atoms list to the models list
                models.add(new ArrayList<>(currentModelAtoms));
                currentModelAtoms.clear();
            }
        }

        // Check if there are any remaining atoms after the last model
        if (!currentModelAtoms.isEmpty()) {
            models.add(new ArrayList<>(currentModelAtoms));
        }


        // Create the PDB complex for each model and return the list of complexes
        List<PdbComplex> pdbComplexes = new ArrayList<>();
        for (List<PdbAtom> modelAtoms : models) {

            // Organize the atoms in monomers
            List<PdbMonomer> monomers = separateAtomsByResidue(modelAtoms);

            // Organize the monomers in polymers
            List<PdbPolymer> polymers = separateMonomerByChain(monomers);

            // Create the PDB complex
            PdbComplex pdbComplex = new PdbComplex();
            for (PdbPolymer polymer : polymers) {
                pdbComplex.addPolymer(polymer);
            }

            pdbComplexes.add(pdbComplex);
        }

        return pdbComplexes;
    }


    public static PdbAtom parseAtom(String line){
        String role = line.substring(13, 16).trim();
        AtomicSpecies atomicSpecies = getAtomicSpecies(role);

        String id= line.substring(7,12).trim();
        String letter3Residue = line.substring(17,21).trim();
        String residue= threeToOneLetterCode(letter3Residue);
        String chain= line.substring(21,23).trim();

        int resdidueIndex = Integer.valueOf(line.substring(23,27).trim());

        double x = Double.parseDouble(line.substring(30, 38).trim());
        double y = Double.parseDouble(line.substring(38, 46).trim());
        double z = Double.parseDouble(line.substring(46, 54).trim());


        Point3D coordinates= new Point3D(x,y,z);

        return new PdbAtom(atomicSpecies,role,id,residue,chain,resdidueIndex,coordinates);
    }

    private static PdbAtom.AtomicSpecies getAtomicSpecies(String atomSymbol) {
        char firstChar = atomSymbol.charAt(0);

        switch (firstChar) {
            case 'C':
                return AtomicSpecies.CARBON;
            case 'H':
                return AtomicSpecies.HYDROGEN;
            case 'N':
                return AtomicSpecies.NITROGEN;
            case 'O':
                return AtomicSpecies.OXYGEN;
            case 'S':
                return AtomicSpecies.SULFUR;
            case 'P':
                return AtomicSpecies.PHOSPHORUS;
            default:
                throw new IllegalArgumentException("Unknown atom symbol: " + atomSymbol);
        }
    }

    public static String threeToOneLetterCode(String threeLetterCode) {
        switch (threeLetterCode.toUpperCase()) {
            case "ALA":
                return "A";
            case "ARG":
                return "R";
            case "ASN":
                return "N";
            case "ASP":
                return "D";
            case "CYS":
                return "C";
            case "GLN":
                return "Q";
            case "GLU":
                return "E";
            case "GLY":
                return "G";
            case "HIS":
                return "H";
            case "ILE":
                return "I";
            case "LEU":
                return "L";
            case "LYS":
                return "K";
            case "MET":
                return "M";
            case "PHE":
                return "F";
            case "PRO":
                return "P";
            case "SER":
                return "S";
            case "THR":
                return "T";
            case "TRP":
                return "W";
            case "TYR":
                return "Y";
            case "VAL":
                return "V";
            default:
                // Return an empty string or throw an exception for unknown codes
                return "";
        }
    }


    public static List<PdbMonomer> separateAtomsByResidue(List<PdbAtom> atoms) {
        Map<Integer, PdbMonomer> monomerMap = new HashMap<>();


        for (PdbAtom atom : atoms) {
            int residueIndex = atom.getResidueIndex();

            if (!monomerMap.containsKey(residueIndex)) {
                // Create a new monomer for the current residueIndex
                PdbMonomer monomer = new PdbMonomer(new ArrayList<>(), atom.getResidue(), residueIndex);
                monomerMap.put(residueIndex, monomer);
            }

            // Add the atom to the corresponding monomer
            monomerMap.get(residueIndex).getAtoms().add(atom);
        }


        return new ArrayList<>(monomerMap.values());
    }


    private static List<PdbPolymer> separateMonomerByChain(List<PdbMonomer> monomers) {

        List<PdbPolymer> polymers = new ArrayList<>();

        Map<String, List<PdbMonomer>> monomersBychain = new HashMap<>();

        for (PdbMonomer monomer : monomers) {
            for (PdbAtom atom : monomer.getAtoms()) {
                String chain = atom.getChain();
                monomersBychain.computeIfAbsent(chain, key -> new ArrayList<>()).add(monomer);
            }
        }

        int number = 1;
        for (Map.Entry<String, List<PdbMonomer>> entry : monomersBychain.entrySet()) {
            polymers.add(new PdbPolymer(entry.getValue(), number, entry.getKey()));
            number += 1;
        }

        return polymers;


    }

}
