package com.example.project.model;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

public class PdbAtom {

    private AtomicSpecies species;
    private String role;
    private String id;
    private String residue;
    private String chain;
    private int residueIndex;
    private Point3D coord;

    public PdbAtom(AtomicSpecies species, String role, String id, String residue, String chain, int residueIndex, Point3D coordinates) {
        this.species=species;
        this.role = role;
        this.id = id;
        this.residue=residue;
        this.chain=chain;
        this.residueIndex=residueIndex;
        this.coord = coordinates;
    }

    // Getters and setters
    public AtomicSpecies getAtomicSpecies(){
        return species;
    }

    public String getRole() {
        return role;
    }

    public String getId() {
        return id;
    }


    public String getResidue(){
        return residue;
    }

    public String getChain(){
        return chain;
    }
    public int getResidueIndex() {return  residueIndex; }

    public Point3D getCoordinates() {
        return coord;
    }


    public enum AtomicSpecies{
        CARBON("C", 0.2, Color.DARKGRAY),
        HYDROGEN("H", 0.1,Color.WHITE),
        OXYGEN("O", 0.3, Color.RED),
        NITROGEN("N", 0.5,Color.YELLOWGREEN),
        SULFUR("S", 0.4, Color.ORCHID),
        PHOSPHORUS("P",0.6, Color.LIGHTBLUE);

        private String symbol;
        private double radiusPM;
        private Color color;

        AtomicSpecies(String symbol, double radiusPM, Color color) {
            this.symbol = symbol;
            this.radiusPM = radiusPM;
            this.color = color;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getRadiusPM() {
            return radiusPM;
        }

        public Color getColor() {
            return color;
        }
    }
}



