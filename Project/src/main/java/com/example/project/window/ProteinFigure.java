package com.example.project.window;

import com.example.project.model.PdbAtom;
import com.example.project.model.PdbComplex;
import com.example.project.model.PdbMonomer;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.util.Pair;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ProteinFigure {

    //Variable for the average coordinates of the molecule atoms
    static double  avgX= 0.0;
    static double  avgY= 0.0;
    static double  avgZ= 0.0;


    public static void compute(PdbComplex model, Group balls, Group sticks, Group ribbons, Map<PdbMonomer, List<Shape3D>> item2shapes) {


            // Clear the groups before adding new nodes
            balls.getChildren().clear();
            sticks.getChildren().clear();
            ribbons.getChildren().clear();


            /*Atomic variable to keep track of the total number of atoms. While integers are object representations of literals
            and are therefore immutable, you can basically only read them. AtomicIntegers are containers for those values.
            You can read and set them. Same as assigning a value to variable. An AtomicInteger is used in applications such
            as atomically incremented counters. In the context of concurrent programming, using an AtomicInteger can help
            prevent race conditions and ensure thread safety when multiple threads need to access or modify the same integer
            value. It provides guarantees of atomicity, visibility, and ordering, making it useful in scenarios where
            concurrent access to an integer variable is required.

            Sources:
            1. https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicInteger.html#:~:text=An%20AtomicInteger%20is%20used%20in,deal%20with%20numerically%2Dbased%20classes.
            2. Stackoverflow

             */

            AtomicInteger numAtoms = new AtomicInteger(0);

            // Arrays to hold the sum of coordinates (X, Y, Z)
            double[] sumX = new double[1];
            double[] sumY = new double[1];
            double[] sumZ = new double[1];


            // Parallel processing of polymers
            model.getPolymers().forEach(polymer -> {
                // Iterate through all atoms in the polymer
                polymer.getAllAtoms().forEach(atom -> {
                    Point3D coordinates = atom.getCoordinates();

                    // Atomic operation to update sumX, sumY, and sumZ and increment numAtoms
                    synchronized (ProteinFigure.class) {
                        sumX[0] += coordinates.getX();
                        sumY[0] += coordinates.getY();
                        sumZ[0] += coordinates.getZ();
                        numAtoms.incrementAndGet();
                    }
                });
            });

            // Calculate average coordinates
            avgX = sumX[0] / numAtoms.get();
            avgY = sumY[0] / numAtoms.get();
            avgZ = sumZ[0] / numAtoms.get();

            // Iterate through the polymers and monomer to get the atoms
            model.getPolymers().forEach(polymer -> {
                polymer.getMonomers().forEach(monomer -> {

                    List<Shape3D> spheres = new ArrayList<>(); //List to save all the sphere of the monomer

                    monomer.getAtoms().forEach(atom -> {

                        if(atom.getAtomicSpecies().getSymbol()!= "H"){

                            // Subtract the average coordinates from each atom's coordinates
                            double x = atom.getCoordinates().getX() - avgX;
                            double y = atom.getCoordinates().getY() - avgY;
                            double z = atom.getCoordinates().getZ() - avgZ;

                            //Create the point for the exact location of the atom in the pane
                            Point3D coordinates = new Point3D(x, y, z);

                            //Generate a sphere for the atom
                            Sphere sphere = drawSphere(atom, coordinates);

                            //Add a user data with the atom the sphere belongs to
                            sphere.setUserData(atom);

                            // Add the sphere to the balls group
                            synchronized (balls) {
                                balls.getChildren().add(sphere);
                            }

                            //Add the sphere also to the list of spheres of the monomer
                            spheres.add(sphere);

                        }
                    });

                    //Add a new entry to the map for each monomer with its shapes
                    item2shapes.put(monomer, spheres);



                });

                // Create Cylinders for the bonds of the polymer
                Map<PdbMonomer, List<Pair<PdbAtom, PdbAtom>>> bondAtoms = polymer.getBondedAtoms();
                bondAtoms.entrySet().forEach(entry -> {

                    //Get the monomer  we are studying
                    PdbMonomer monomer = entry.getKey();

                    //Get a list of all the bonds presented on the monomer
                    List<Pair<PdbAtom, PdbAtom>> bonds = entry.getValue();

                    //List to save all the sphere of the monomer
                    List<Shape3D> cylinders = new ArrayList<>();

                    bonds.forEach(bond -> {

                        // Create a cylinder for the bond
                        Cylinder cylinder = drawCylinder(bond.getKey(), bond.getValue());

                        // Add a user data with the atom the bond belongs to (first atom will always be chosen)
                        cylinder.setUserData(bond.getKey());

                        // Add the cylinder to the sticks group
                        synchronized (sticks) {
                            sticks.getChildren().add(cylinder);
                        }

                        //Add the cylinder also to the list of cylinders of the monomer
                        cylinders.add(cylinder);
                    });



                    // Check if the monomer is already on the map
                    if (item2shapes.containsKey(monomer)) {
                        item2shapes.get(monomer).addAll(cylinders);
                    } else {
                        // Key doesn't exist, create a new entry
                        item2shapes.put(monomer, new ArrayList<>(cylinders));
                    }
                });

                //Create triangle meshes  between the carbons alpha anf beta of connected aminoacids
                polymer.getConnectedMonomerPairs().forEach(pair ->{

                    //Get alpha and beta carbons for the first aminoacid
                    PdbMonomer monomer1 = pair.getKey();
                    Point3D alpha1 = monomer1.getA().getCoordinates();
                    alpha1 = new Point3D(alpha1.getX() - avgX, alpha1.getY() - avgY, alpha1.getZ() - avgZ);
                    Point3D beta1;
                    if(monomer1.getB() != null){
                        beta1=monomer1.getB().getCoordinates();
                        beta1 = new Point3D(beta1.getX() - avgX, beta1.getY() - avgY, beta1.getZ() - avgZ);
                    }else{
                        beta1=null;
                    }
                    //Get alpha and beta carbons for the second aminoacid
                    PdbMonomer monomer2 = pair.getValue();
                    Point3D alpha2 = monomer2.getA().getCoordinates();
                    alpha2 = new Point3D(alpha2.getX() - avgX, alpha2.getY() - avgY, alpha2.getZ() - avgZ);
                    Point3D beta2;
                    if(monomer2.getB() != null){
                        beta2=monomer2.getB().getCoordinates();
                        beta2 = new Point3D(beta2.getX() - avgX, beta2.getY() - avgY, beta2.getZ() - avgZ);
                    }else{
                        beta2=null;
                    }


                    //Create the mesh
                    MeshView ribbon = drawRibbon(alpha1, beta2, alpha2, beta2);

                    // Add the triangle mesh to the ribbons group
                    synchronized (ribbons) {
                        ribbons.getChildren().add(ribbon);
                    }

                    // Check if the monomer is already on the map (default assign to the aminacid 1)
                    if (item2shapes.containsKey(monomer1)) {
                        item2shapes.get(monomer1).add(ribbon);
                    } else {
                        // Key doesn't exist, create a new entry
                        List<Shape3D> list = new ArrayList<>();
                        list.add(ribbon);
                        item2shapes.put(monomer1, new ArrayList<>(list));
                    }
                });

            });
    }

    //Method to set Up the colors of the components of the protein
    public static void colorBy(String colorFormat, Group balls, Group sticks){

        switch (colorFormat) {
                case "Atom":
                    //Iterate through every sphere and set the color to the atomspecies color
                    balls.getChildren().forEach(node -> {
                        if (node instanceof Sphere) {
                            Sphere sphere = (Sphere) node;
                            PdbAtom atom = (PdbAtom) sphere.getUserData();
                            Color color = atom.getAtomicSpecies().getColor();
                            sphere.setMaterial(Material.generateMaterial(color));
                        }
                    });

                    //Iterate through every stick and set the default color orange
                    sticks.getChildren().forEach(node -> {
                        if (node instanceof Cylinder) {
                            Cylinder cylinder = (Cylinder) node;
                            cylinder.setMaterial(Material.generateMaterial(Color.ORANGE));

                        }
                    });
                    break;

                case "Aminoacid":

                    // Store amino acids and a random assigned color in a map
                    Map<String, Color> aminoAcidColorMap = new HashMap<>();
                    List<String> aminoAcids = new ArrayList<>(Arrays.asList("A", "R", "N", "D", "C", "E", "Q", "G", "H",
                            "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V"));
                    for (String aminoacid : aminoAcids) {
                        aminoAcidColorMap.put(aminoacid, generateRandomColor());
                    }

                    //Iterate through balls and sticks and assign the color to the shapes depending on the aminoacid they form part
                    balls.getChildren().forEach(node -> {
                        if (node instanceof Sphere) {
                            Sphere sphere = (Sphere) node;
                            PdbAtom atom = (PdbAtom) sphere.getUserData();
                            Color color = aminoAcidColorMap.get(atom.getResidue());
                            sphere.setMaterial(Material.generateMaterial(color));
                        }
                    });
                    sticks.getChildren().forEach(node -> {
                        if (node instanceof Cylinder) {
                            Cylinder cylinder = (Cylinder) node;
                            PdbAtom atom = (PdbAtom) cylinder.getUserData();
                            Color color = aminoAcidColorMap.get(atom.getResidue());
                            cylinder.setMaterial(Material.generateMaterial(color));
                        }
                    });
                    break;

                //case "Secondary structure":
                //break;
                case "Molecule":

                    // Store polymers and a random assigned color in a map
                    Map<String, Color> polymerColorMap = new HashMap<>();

                    //Iterate through balls and sticks and assign the color to the shapes depending on the polymer they form part
                    balls.getChildren().forEach(node -> {
                        if (node instanceof Sphere) {
                            Sphere sphere = (Sphere) node;
                            PdbAtom atom = (PdbAtom) sphere.getUserData();
                            String polymer = atom.getChain();

                            if (polymerColorMap.containsKey(polymer)) {
                                Color color = polymerColorMap.get(polymer);
                                sphere.setMaterial(Material.generateMaterial(color));
                            } else {
                                Color color = generateRandomColor();
                                sphere.setMaterial(Material.generateMaterial(color));
                                polymerColorMap.put(polymer, color);
                            }
                        }

                    });

                    sticks.getChildren().forEach(node -> {
                        if (node instanceof Cylinder) {
                            Cylinder cylinder = (Cylinder) node;
                            PdbAtom atom = (PdbAtom) cylinder.getUserData();
                            String polymer = atom.getChain();
                            Color color = polymerColorMap.get(polymer);
                            cylinder.setMaterial(Material.generateMaterial(color));
                        }
                    });
                    break;
            }

            balls.requestLayout();
            sticks.requestLayout();

    }

    public static Color generateRandomColor(){

        Random random = new Random();

        // Generate random RGB values

        float red = random.nextFloat();
        float green = random.nextFloat();
        float blue = random.nextFloat();

        // Create a new Color object
        Color color = new Color(red, green, blue, 1);

        return color;
    }

    //Class for the material style of the shapes
    public static class Material{
        public static PhongMaterial generateMaterial(Color color){
            var material = new PhongMaterial();
            material.setDiffuseColor(color);
            material.setSpecularColor(Color.WHITE);
            return material;
        }
    }

    public static Sphere drawSphere(PdbAtom atom, Point3D coordinates){

        // Create a sphere for the atom
        Sphere sphere = new Sphere(atom.getAtomicSpecies().getRadiusPM(),6);
        sphere.setTranslateX(coordinates.getX());
        sphere.setTranslateY(coordinates.getY());
        sphere.setTranslateZ(coordinates.getZ());

        return sphere;

    }

    public static Cylinder drawCylinder(PdbAtom atom1, PdbAtom atom2){

        //Substract the averages to their coordenates
        double x1 = atom1.getCoordinates().getX() - avgX;
        double y1 = atom1.getCoordinates().getY() - avgY;
        double z1 = atom1.getCoordinates().getZ() - avgZ;

        double x2 = atom2.getCoordinates().getX() - avgX;
        double y2 = atom2.getCoordinates().getY() - avgY;
        double z2 = atom2.getCoordinates().getZ() - avgZ;

        Point3D pointA = new Point3D(x1, y1, z1);
        Point3D pointB = new Point3D(x2, y2, z2);


        return Stick.create(pointA, pointB);
    }


    private static MeshView drawRibbon(Point3D alphaCarbon, Point3D betaCarbon, Point3D alphaCarbon2, Point3D betaCarbon2) {

        // Determine the distance between alpha and virtual beta carbon
        double distance = 1.5;

        // Generate a virtual beta carbon point at a determined distance from the alpha carbon
        double deltaX = 0.2;
        double deltaY = 0.2;    // Direction
        double deltaZ = 0.2;

        if (betaCarbon == null) {
            betaCarbon = new Point3D(
                    alphaCarbon.getX() + deltaX * distance,
                    alphaCarbon.getY() + deltaY * distance,
                    alphaCarbon.getZ() + deltaZ * distance
            );
        }
        if(betaCarbon2 == null){
            betaCarbon2 = new Point3D(
                    alphaCarbon2.getX() + deltaX * distance,
                    alphaCarbon2.getY() + deltaY * distance,
                    alphaCarbon2.getZ() + deltaZ * distance
            );
        }

        // Create array of vertices for the triangles
        float[] points = {
                (float) alphaCarbon.getX(), (float) alphaCarbon.getY(), (float) alphaCarbon.getZ(),
                (float) betaCarbon.getX(), (float) betaCarbon.getY(), (float)betaCarbon.getZ(),
                (float) alphaCarbon.getX(), (float)alphaCarbon2.getY(), (float)alphaCarbon.getZ(),
                (float) betaCarbon2.getX(), (float) betaCarbon2.getY(), (float) betaCarbon2.getZ(),
                (float)(2 * alphaCarbon.getX() - betaCarbon.getX()), (float)(2 * alphaCarbon.getY() - betaCarbon.getY()), (float)(2 * alphaCarbon.getZ() - betaCarbon.getZ()),
                (float)(2 * alphaCarbon2.getX() - betaCarbon2.getX()), (float)(2 * alphaCarbon2.getY() - betaCarbon2.getY()), (float)(2 * alphaCarbon2.getZ() - betaCarbon2.getZ()),
        };

        //Create array of the faces (both side visible)
        int faces[] = {
                0 , 0 , 1 , 0 , 4 , 0 ,
                0 , 0 , 4 , 0 , 5 , 0 ,
                1 , 0 , 2 , 0 , 3 , 0 ,
                1 , 0 , 3 , 0 , 4 , 0 ,

                0 , 0 , 4 , 0 , 1 , 0 , // sames triangles facing the other way
                0 , 0 , 5 , 0 , 4 , 0 ,
                1 , 0 , 3 , 0 , 2 , 0 ,
                1 , 0 , 4 , 0 , 3 , 0 ,


        };

        // Create array of texture coordinates for mapping textures onto the ribbon
        float[] texCoords = {
                0 , 0 ,
                0 , 0.5f ,
                0 , 1 ,
                1 , 1 ,
                1 , 0.5f ,
                1 , 0
        };


        //Create smoothing group membership for the ribbon faces
        int[] smoothing = {1, 1, 1, 1, 2, 2, 2, 2};


        //Create the mesh
        var mesh = new TriangleMesh();
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);
        mesh.getFaces().addAll(faces);
        mesh.getFaceSmoothingGroups().addAll(smoothing);


        // Create a MeshView from the TriangleMesh
        MeshView ribbonMeshView = new MeshView(mesh);

        // Set material for the ribbon
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.RED);
        material.setSpecularColor(Color.WHITE);
        ribbonMeshView.setMaterial(material);



        return ribbonMeshView;
    }


    public static class Stick {
        public static final double DEFAULT_RADIUS = 0.08;

        public static Cylinder create(Point3D a, Point3D b) {
            var YAXIS = new Point3D(0, 100, 0);
            var midpoint = a.midpoint(b);
            var direction = b.subtract(a);
            var perpendicularAxis = YAXIS.crossProduct(direction);
            var angle = YAXIS.angle(direction);
            var cylinder = new Cylinder(DEFAULT_RADIUS, 100,4);
            cylinder.setRotationAxis(perpendicularAxis);
            cylinder.setRotate(angle);
            cylinder.setTranslateX(midpoint.getX());
            cylinder.setTranslateY(midpoint.getY());
            cylinder.setTranslateZ(midpoint.getZ());
            cylinder.setScaleY(a.distance(b) / cylinder.getHeight());
            return cylinder;
        }
    }




}
