package com.example.project.window;

import com.example.project.model.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


public class WindowPresenter {


    //Initialize the model
    AtomicReference<PdbComplex> protein = new AtomicReference<>();

    //Create the groups for the different shapes and the final figure to add to the pane
    Group balls = new Group();
    Group sticks = new Group();
    Group ribbons = new Group();
    Group figure = new Group(balls, sticks, ribbons);

    //An observable list to save all the entries from pdb and filter them when searching
    ObservableList<String> entries;

    //Initializing the subscene
    SubScene subScene = new SubScene(figure, 600, 400, true, SceneAntialiasing.BALANCED);

    //Declaring the prespective camera
    private PerspectiveCamera camera;

    //Declaring an instance of the interface SelectionModel created at the end of the file
    SelectionModel<PdbMonomer> aminoacidSelection;

    //Map for the monomers on the texFlow. Useful for the selection.
    Map<PdbMonomer, Text> monomerMap = new HashMap<>();

    //Map to link the monomers with the nodes that represent it. Useful for the selection.
    Map<PdbMonomer, List<Shape3D>> item2shapes = new HashMap<>();

    //Create stacks for redo and undo manager
    UndoRedoManager undoManager = new UndoRedoManager() ;
    UndoRedoManager redoManager = new UndoRedoManager();


    public WindowPresenter(Stage stage, WindowView view, List<PdbComplex> model) throws IOException {



        //Initializing the selectionModel
        aminoacidSelection= new SelectionModel<PdbMonomer>() {

            private ObservableSet<PdbMonomer> selectedItems = FXCollections.observableSet();
            @Override
            public boolean isSelected(PdbMonomer pdbMonomer) {
                if(selectedItems.contains(pdbMonomer)){
                    return true;
                }
                return false;
            }

            @Override
            public boolean setSelected(PdbMonomer pdbMonomer, boolean select) {
                if (select) {
                    return selectedItems.add(pdbMonomer);
                } else {
                    return selectedItems.remove(pdbMonomer);
                }
            }

            @Override
            public boolean selectAll(Collection<PdbMonomer> list) {
                return selectedItems.addAll(list);
            }

            @Override
            public void clearSelection() {
                selectedItems.clear();
            }

            @Override
            public boolean clearSelection(PdbMonomer pdbMonomer) {
                return selectedItems.remove(pdbMonomer);
            }

            @Override
            public boolean clearSelection(Collection<PdbMonomer> list) {
                return selectedItems.removeAll(list);
            }

            @Override
            public ObservableSet<PdbMonomer> getSelectedItems() {
                return selectedItems;
            }
        };


        /**
         * Get the view's controller
         */

        var controller = view.getController();

        /**
         * Setting up the menu bar buttons
         *
         * -File: Close, Open, Save
         * -Edit: Un/redo, Copy,Show balls/sticks/ribbons
         * -View: Fullscreen, Exit fullscreen, light and dark mode
         * -Help: About
         *
         */

        /**
         *  Close Button
         */
        controller.getCloseButton().setOnAction(e -> Platform.exit());

        /**
         *  Open Button
         */
        controller.getOpenButton().setOnAction(e -> {
            var chooser = new FileChooser();
            var file = chooser.showOpenDialog(stage);
            if (file != null) {

                // Show the loading pane
                showLoadingPane(controller, stage);

                /*
                Using Task in a separate thread allowing the JAvaFX Application Thread to handle UI updates,
                including animation of the progress bar. The onSucceeded event is triggered when the task completes,
                allowing to perform any necessary UI updates after the background work is done.

                Sources:
                 - https://docs.oracle.com/javafx/2/api/javafx/concurrent/Task.html
                 - ChatGPT
                 */
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        //Reading the pdb and creating a String with all the content
                        List<String> contentLines = Files.readAllLines(file.toPath());
                        String content = String.join("\n", contentLines);

                        //Get the PddMolecule or molecules in case there are more than 1 conformation
                        List<PdbComplex> model = ReadPDB.readPDBfile(content);

                        //Set up the model (user choose the conformation if applicable and it is set to the protein atomicVariable)
                        setUpModel(model, controller);

                        // Execute  on the JavaFX application thread
                        Platform.runLater(() -> {
                            //Set up the model (user choose the conformation if applicable and it is set to the protein atomicVariable)
                            setUpModel(model, controller);

                            // Hide the loading pane
                            controller.getLoadingPane().setVisible(false);
                        });


                        return null;
                    }
                };

                // Start the task in a new thread
                Thread thread = new Thread(task);
                thread.start();
            }
        });


        /**
         *  Save Button
         */

        /**
         *  Un/redo Buttons
         */

        controller.getUndoButton().setOnAction(e -> undoManager.undo());
        controller.getUndoButton().textProperty().bind(undoManager.undoLabelProperty());
        controller.getUndoButton().disableProperty().bind(undoManager.canUndoProperty().not());

        controller.getRedoButton().setOnAction(e -> undoManager.redo());
        controller.getRedoButton().textProperty().bind(undoManager.redoLabelProperty());
        controller.getRedoButton().disableProperty().bind(undoManager.canRedoProperty().not());


        /**
         *  Copy Button
         */
        controller.getCopyButton().setOnAction(e -> {
            var content = new ClipboardContent();
            content.putImage(controller.getFigurePane().snapshot(null, null));
            Clipboard.getSystemClipboard().setContent(content);
        });

        /**
         *  Show balls, sticks and ribbons Button
         */
        controller.getShowBallsButton().setOnAction(e -> {
            controller.getBallsCheckBox().fire();
        });
        controller.getShowSticksButton().setOnAction(e -> {
            controller.getStickCheckBox().fire();
        });
        controller.getShowRibbonsButton().setOnAction(e -> {
            controller.getRibbonsCheckBox().fire();
        });

        //Disable items when no figure is presented
        controller.getShowBallsButton().disableProperty().bind(Bindings.isEmpty(balls.getChildren()));
        controller.getShowSticksButton().disableProperty().bind(Bindings.isEmpty(sticks.getChildren()));
        controller.getShowRibbonsButton().disableProperty().bind(Bindings.isEmpty(ribbons.getChildren()));



        /**
         *  Fullscreen and exit Button
         */
        controller.getFullScreenButton().setOnAction(e -> {
            stage.setFullScreen(true);
        });

        controller.getExitScreenButton().setOnAction(e -> {
            stage.setFullScreen(false);
        });


        /**
         *  Dark mode Button
         */
        controller.getDarkButton().setOnAction(e -> {
            controller.getRootPane().getStylesheets().clear(); // Clear existing stylesheets
            controller.getRootPane().getStylesheets().add(getClass().getResource("/css/dark-mode.css").toExternalForm());

            /*
               The listView and the textFlow could not be set from the css file as they are filled while running the
               program and its components had to be set one by one.
             */

            // Update cell styles of the view List
            controller.getEntriesList().setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);


                    if (getIndex() % 2 == 0) {
                        setText(item);
                        setStyle("-fx-control-inner-background: #606060;");
                    } else {
                        setText(item);
                        setStyle("-fx-control-inner-background: #666666;");
                    }
                }
            });

            //Updating the TextFlow
            TextFlow textFlow = controller.getTextFlow();
            List<Node> nodes = new ArrayList<>(textFlow.getChildren());

            for (Node node : nodes) {
                if (node instanceof Text) {
                    Text text = (Text) node;
                    text.setFill(Color.WHITE);
                }
            }

            textFlow.getChildren().setAll(nodes);




        });



        /**
         *  Light mode Button
         */
        controller.getLightButton().setOnAction(e -> {
            controller.getRootPane().getStylesheets().clear(); // Clear existing stylesheets

            /*
               The listView and the textFlow could not be set from the css file as they are filled while running the
               program and its components had to be set one by one.
             */

            // Update cell styles of the view List
            controller.getEntriesList().setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    setStyle("");


                }
            });

            //Updating the TextFlow
            TextFlow textFlow = controller.getTextFlow();
            List<Node> nodes = new ArrayList<>(textFlow.getChildren());

            for (Node node : nodes) {
                if (node instanceof Text) {
                    Text text = (Text) node;
                    text.setFill(Color.BLACK);
                }
            }

            textFlow.getChildren().setAll(nodes);

        });


        /**
         *  About Button
         */
        controller.getAboutButton().setOnAction(e -> {

            // Set the alignment constraints for the about pane
            controller.getAboutPane().setLayoutX((stage.getWidth() - controller.getAboutPane().getWidth()) / 2);
            controller.getAboutPane().setLayoutY((stage.getHeight() - controller.getAboutPane().getHeight()) / 2);
            controller.getAboutPane().setVisible(true);
            controller.getExitButton().setOnAction(ev -> controller.getAboutPane().setVisible(false));
        });



        /**
         *  Setting up the toolbar buttons
         *
         *  - Checkboxes
         *  - Sliders
         *  - ChoiceBox
         *
         */


        /**
         *  Checkboxes
         */
        controller.getBallsCheckBox().setOnAction(e -> {
            if (controller.getBallsCheckBox().isSelected()) {
                balls.getChildren().forEach(sphere -> { sphere.setVisible(true); });


            } else {
                balls.getChildren().forEach(sphere -> { sphere.setVisible(false); });
            }
        });

        controller.getStickCheckBox().setOnAction(e -> {
            if (controller.getStickCheckBox().isSelected()) {
                sticks.getChildren().forEach(cylinder -> { cylinder.setVisible(true); });
            } else {
                sticks.getChildren().forEach(cylinder -> { cylinder.setVisible(false); });
            }
        });

        controller.getRibbonsCheckBox().setOnAction(e -> {
            if (controller.getRibbonsCheckBox().isSelected()) {
                ribbons.getChildren().forEach(ribbon -> { ribbon.setVisible(true); });
            } else {
                ribbons.getChildren().forEach(ribbon -> { ribbon.setVisible(false); });
            }
        });

        //Disable tools when no figure is presented
        controller.getBallsCheckBox().disableProperty().bind(Bindings.isEmpty(balls.getChildren()));
        controller.getStickCheckBox().disableProperty().bind(Bindings.isEmpty(sticks.getChildren()));
        controller.getRibbonsCheckBox().disableProperty().bind(Bindings.isEmpty(ribbons.getChildren()));

        //Un- and redo properties
        controller.getBallsCheckBox().selectedProperty().addListener((v,o,n) -> {
            undoManager.add(new PropertyCommand<>("ballsCheck",(BooleanProperty)v,o,n));
        });
        controller.getStickCheckBox().selectedProperty().addListener((v,o,n) -> {
            undoManager.add(new PropertyCommand<>("sticksCheck",(BooleanProperty)v,o,n));
        });
        controller.getRibbonsCheckBox().selectedProperty().addListener((v,o,n) -> {
            undoManager.add(new PropertyCommand<>("ribbonsCheck",(BooleanProperty)v,o,n));
        });

        /**
         *  Sliders
         */
        controller.getBallSlider().valueProperty().addListener((observable, oldValue, newValue) -> {
            double change = (newValue.doubleValue() - oldValue.doubleValue()) * 0.1 + 1;
            balls.getChildren().forEach(node ->{
                if(node instanceof Sphere sphere) {
                    sphere.setRadius(sphere.getRadius() * change);   // Update the size of each sphere
                }
            });

        });

        controller.getStickSlider().valueProperty().addListener((observable, oldValue, newValue) -> {
            double change = (newValue.doubleValue() - oldValue.doubleValue()) * 0.5 + 1;
            sticks.getChildren().forEach(node ->{
                if(node instanceof Cylinder cylinder) {
                    cylinder.setRadius(cylinder.getRadius() * change);   // Update the size of each sphere
                }
            });

        });

        //TODO ribbons

        //Disable tools when no figure is presented
        controller.getStickSlider().disableProperty().bind(Bindings.isEmpty(sticks.getChildren()));
        controller.getBallSlider().disableProperty().bind(Bindings.isEmpty(balls.getChildren()));

        //Un- and redo properties
        controller.getBallSlider().valueProperty().addListener((v,o,n) -> {
            undoManager.add(new PropertyCommand<>("ballsSlider",(DoubleProperty)v,o,n));
        });
        controller.getStickSlider().valueProperty().addListener((v,o,n) -> {
            undoManager.add(new PropertyCommand<>("sticksSlider",(DoubleProperty)v,o,n));
        });



        /**
         * ChoiceBox
         */

        //Initialize the option for colours and set the default value "Atoms"
        controller.initializeOptions();

        controller.getColorChoiceButton().setOnAction(e -> {


            //Set the different coloring of the figure on each case
            String selection = (controller.getColorChoiceButton().getValue());

            if (selection != null) {

                //Color by choice the protein
                ProteinFigure.colorBy(selection, balls, sticks);
            }

        });

        //Disable tool when no figure is presented
        controller.getColorChoiceButton().disableProperty().bind(Bindings.isEmpty(balls.getChildren()));




          /**
             Setting up the other GUI components:

           - ListView
           - TextField
           - FigurePane
           - TextFlow

         **/


        /**
         * ListView
         */
        // Contact the web and show all PDBfiles
        entries = PDBWebClient.main(new String[0]);
        controller.getEntriesList().setItems(entries);

        //ListView selection --> show the entry selected. Get the selection from the list, the entry as a string.
        controller.getEntriesList().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Showing the loading pane
                showLoadingPane(controller, stage);

                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        // Get the selected entry from the list and save it at the first position of a string array
                        String[] entry = {newValue};

                        // Now use the WebClient class to get the content of the selected PDB file
                        PDBWebClient pdbWebClient = new PDBWebClient();
                        ObservableList<String> pdb = pdbWebClient.main(entry);


                        //Get the PddMolecule or molecules in case there are more than 1 conformation
                        List<PdbComplex> model = ReadPDB.readPDBfile(pdb.get(0));

                        // Execute  on the JavaFX application thread
                        Platform.runLater(() -> {
                            //Set up the model (user choose the conformation if applicable and it is set to the protein atomicVariable)
                            setUpModel(model, controller);
                            // Hide the loading pane
                            controller.getLoadingPane().setVisible(false);
                        });

                        return null;
                    }
                };

                // Start the task in a new thread
                Thread thread = new Thread(task);
                thread.start();
            }
        });



        /**
         * TextField
         */

        // Filter the ListView
        controller.getPDBentryField().textProperty().addListener((observable, oldValue, newValue) -> {
            String filterText = newValue.toLowerCase(); // Convert the entered text to lowercase for case-insensitive filtering

            // Create a new filtered list based on the original list
            FilteredList<String> filteredList = new FilteredList<>(entries);
            filteredList.setPredicate(item -> item.toLowerCase().contains(filterText));

            // Set the filtered list as the items for the ListView
            controller.getEntriesList().setItems(filteredList);
        });


        /**
         * FigurePane
         */

        //Zoom in/out by mouse
        controller.getFigurePane().setOnScroll(e -> {
            if (!controller.getBallSlider().isDisable()|| !controller.getStickSlider().isDisable()) {

                var delta = e.getDeltaY();
                if (delta > 0)
                    camera.setTranslateZ(1.1 * camera.getTranslateZ());
                else if (delta < 0)
                    camera.setTranslateZ(1 / 1.1 * camera.getTranslateZ());
            }
        });


        //Listening to the selection and representing them on the pane and textflow
        aminoacidSelection.getSelectedItems().addListener((SetChangeListener<? super PdbMonomer>) c -> {
            Text node = monomerMap.get(c.getElementAdded());
            if (c.wasAdded()) {
                Platform.runLater(() -> {
                    if (aminoacidSelection.getSelectedItems().size() == 1) {  // Previously nothing selected
                        updateOpacity(flatten(item2shapes.values()), 0.1);
                    }

                    updateOpacity(item2shapes.get(c.getElementAdded()), 1.0);
                    node.setFill(Color.RED);
                });

            } else if (c.wasRemoved()) {
                Platform.runLater(() -> {
                    if (aminoacidSelection.getSelectedItems().size() > 0) {
                        updateOpacity(item2shapes.get(c.getElementRemoved()), 0.1);
                    } else {    // Nothing selected
                        updateOpacity(flatten(item2shapes.values()), 1.0);
                    }
                    node.setFill(Color.BLACK);
                });
            }
        });

    }


    /**
     * ADDITIONAL METHODS AND CLASSES FOR THE WINDOW PRESENTER
     */

    /**
     * Setup model set the protein to the first model but creates a ChoiceBox for the user to select which model he
     * wants to show, and a listener to the ChoiceBox will set the selection as the protein to compute. In case there
     * is just one model, mo choicebox is created and just the unique model is shown.
     * @param model
     * @param controller
     */


    public void setUpModel(List<PdbComplex> model, WindowController controller){
        if(model.size() > 1) {

            //Make the label and choicebox visible so the user can use a conformation
            ChoiceBox<String> models = controller.getModelsChoiceBox();
            models.setVisible(true);
            controller.getModelLabel().setVisible(true);


            //Create an Observablelist with all the possible models and set them on the choicebox (predetermined is 1)
            ObservableList<String> items = FXCollections.observableArrayList();
            for(int i=1; i <= model.size(); i++){
                items.add("Model " + i);
            }
            models.setItems(items);


            //Listen to the selections on the choicebox and set the protein to the PdbComplex selected.
            models.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                int selectedIndex = newValue.intValue();
                protein.set(model.get(selectedIndex));
                setUpFigure(controller);
            });

            models.setValue(items.get(0));
            protein.set(model.get(0));
            setUpFigure(controller);



        }else{
            protein.set(model.get(0));
            setUpFigure(controller);
        }

    }

    public void setUpFigure(WindowController controller){

            //Compute the figure nodes
            ProteinFigure.compute(protein.get(), balls, sticks, ribbons, item2shapes);

            // Color the protein by choice
            ProteinFigure.colorBy(controller.getColorChoiceButton().getValue(), balls, sticks);

            // Set up the figure on the pane and the amino acid sequence on the textFlow
            setUpScene(controller);
            setUpFlowText(controller, protein);
    }

    /**
     * SetupScene Method: Create the subscene and add the figure. Also set up the rotation of the figure.
     * @param controller
     */

    public void setUpScene(WindowController controller) {

        //Bind the subscene with the FigurePane so they resize together
        subScene.widthProperty().bind(controller.getFigurePane().widthProperty());
        subScene.heightProperty().bind(controller.getFigurePane().heightProperty());

        //setup camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        camera.setTranslateZ(-100);

        //Ading the scene to the pane (delete any possible previous children)
        subScene.setCamera(camera);
        controller.getFigurePane().getChildren().clear();
        controller.getFigurePane().getChildren().add(subScene);

        //Add to the pane also the checkboxes for each polymer of the protein complex and let the user select.
        userPolymerSelection(controller);

        //Set rotation of the figure
        for (Node node : figure.getChildren()) {
            MouseRotate3D.setup(node, figure);
        }

        //Handle selection
        for (Node node : figure.getChildren()) {
            figureSelectionHandle(node);
        }

        //Both checkbox balls and sricks selected at the start. but ribbons not.
        controller.getStickCheckBox().setSelected(true);
        controller.getBallsCheckBox().setSelected(true);
        controller.getRibbonsCheckBox().setSelected(true);
        controller.getRibbonsCheckBox().fire();


    }

    /**
     * userPolymerSelection creates a RadioButton for every polymer presented on the PDB molecule, add them to the pane
     * and assigned also its functionality. Also a radioButton to show the whole molecule is created.
     * @param controller
     */

    public void userPolymerSelection(WindowController controller) {

        VBox checkboxes = new VBox();

        //Access all the polymers to: Create an individual RadioButton to select it, set the action of the button.
        protein.get().getPolymers().forEach(polymer -> {

            //Create the radioButton and add it to the VBox
            RadioButton polymerSelection = new RadioButton("Chain " + polymer.getLabel());
            checkboxes.getChildren().add(polymerSelection);

            //Functionality of the radioButton
            polymerSelection.setOnAction(e -> {

                //Clear the figure to add the new nodes that we want to show
                balls.getChildren().clear();
                sticks.getChildren().clear();
                ribbons.getChildren().clear();
                aminoacidSelection.clearSelection();

                //Unselect all others RadioButtons: we have a button for every monomer or for all
                checkboxes.getChildren().forEach(child -> {
                    if (!polymerSelection.equals(child)) {
                        ((RadioButton) child).setSelected(false);
                    }
                });

                //Access the shapes on the map item2shapes and getting for  the polymer selected
                polymer.getMonomers().forEach(monomer -> {
                    for (Shape3D shape : item2shapes.get(monomer)) {
                        if (shape instanceof Sphere && !balls.getChildren().contains(shape)) {
                            balls.getChildren().add(shape);
                        } else if (shape instanceof Cylinder && !sticks.getChildren().contains(shape)) {
                            sticks.getChildren().add(shape);
                        } else if (shape instanceof MeshView && !ribbons.getChildren().contains(shape)) {
                            ribbons.getChildren().add(shape);
                        }
                    }

                });

            });
        });

        //Add a last radioButton to show all the polymers
        RadioButton showAll = new RadioButton("All");
        checkboxes.getChildren().add(showAll);

        //Functionality of the radioButton ShowAll
        showAll.setOnAction(e -> {

            //Clear the figure to add the new nodes that we want to show
            balls.getChildren().clear();
            sticks.getChildren().clear();

            //Unselect all others RadioButtons: we have a button for every monomer or for all
            checkboxes.getChildren().forEach(child -> {
                if (!showAll.equals(child)) {
                    ((RadioButton) child).setSelected(false);
                }
            });

            //Access the shapes on the map item2shapes and getting for all the polymers
            protein.get().getPolymers().forEach(polymer ->{
                polymer.getMonomers().forEach(monomer -> {
                    for (Shape3D shape : item2shapes.get(monomer)) {
                        if(shape instanceof Sphere && !balls.getChildren().contains(shape)){
                            balls.getChildren().add(shape);
                        }else if(shape instanceof Cylinder && !sticks.getChildren().contains(shape)){
                            sticks.getChildren().add(shape);
                        }else if (shape instanceof MeshView && !ribbons.getChildren().contains(shape)) {
                            ribbons.getChildren().add(shape);
                        }
                    }

                });
            });

        });


        controller.getFigurePane().getChildren().add(checkboxes);
        checkboxes.setAlignment(Pos.TOP_RIGHT);
        showAll.fire();
    }


    /**
     * SetupFlowText: Fill the flow text with the aminoacid sequence of the protein
     * @param controller
     * @param protein
     */

    public void setUpFlowText(WindowController controller, AtomicReference<PdbComplex> protein) {

        TextFlow texFlow = controller.getTextFlow();
        texFlow.getChildren().clear();
        PdbComplex complex = protein.get();
        monomerMap.clear();

        for (PdbPolymer polymer : complex.getPolymers()) {
            Text header = new Text("Chain" + polymer.getLabel() + "\n");
            texFlow.getChildren().add(header);

            //To avoid printing the same monomers on the textFlow
            Set<PdbMonomer> printedMonomers = new HashSet<>();

            for (PdbMonomer monomer : polymer.getMonomers()) {

                if (!printedMonomers.contains(monomer)) {

                    //Create a tex node with the label of the monomer
                    Text node = new Text(monomer.getLabel());
                    monomerMap.put(monomer,node);
                    printedMonomers.add(monomer);

                    // Set the color of the text depending on the mode
                    if (controller.getRootPane().getStylesheets().isEmpty()) {
                        node.setFill(Color.BLACK);
                    } else {
                        node.setFill(Color.WHITE);
                    }

                    // Add event handler for the text node
                    textSelectionHandle(node, monomer);

                    //Add the node to the TextFlow
                    texFlow.getChildren().add(node);
                }

            }
            texFlow.getChildren().add(new Text("\n"));
        }
    }



    /**
     * FigureSelection method handles the selection of a node on the figurePane. it looks for the aminoacid that this
     * node is representing and add it to the selectedItems of the aminoacidSelection.
     * @param node
     */

    public void figureSelectionHandle(Node node){


        node.setOnMouseClicked(e -> {

            if(!e.isShiftDown()){
                aminoacidSelection.clearSelection();
            }

            // Get the amino acid selected
            PdbMonomer aminoacidSelected = null;
            for (Map.Entry<PdbMonomer, List<Shape3D>> entry : item2shapes.entrySet()) {
                List<Shape3D> shapes = entry.getValue();
                for (Shape3D shape : shapes) {
                    if (shape instanceof Sphere && node instanceof Sphere && shape.equals(node)) {
                        aminoacidSelected = entry.getKey();
                        break;
                    } else if (shape instanceof Cylinder && node instanceof Cylinder && shape.equals(node)) {
                        aminoacidSelected = entry.getKey();
                        break;
                    } else if (shape instanceof MeshView && node instanceof MeshView && shape.equals(node)) {
                        aminoacidSelected = entry.getKey();
                        break;
                    }
                }
                if (aminoacidSelected != null) {
                    break; // Exit the loop if a match is found
                }
            }

            if(aminoacidSelected!=null) {
                //Put the monomer on the selected items or take it out depending on if it was already selected or not
                aminoacidSelection.setSelected(aminoacidSelected, !aminoacidSelection.isSelected(aminoacidSelected));
            }
        });

    }

    /**
     * TextSelectio handles the selection of any node (text, one letter aminoacid label) on the texflow and looks for
     * the aminoacid that is represented saving it on the selectedItems.
     * @param node
     * @param monomer
     */

    public void textSelectionHandle (Text node, PdbMonomer monomer){

        node.setOnMouseClicked(e -> {
            if(!e.isShiftDown()){
                aminoacidSelection.clearSelection();
            }


            // Put the monomer on the selected items or take it out depending on if it was already selected or not
            aminoacidSelection.setSelected(monomer, !aminoacidSelection.isSelected(monomer));

        });
    }



    /**
     * UpdateOpacity changes the opacity of a 3D shape to a given value.
     * @param list
     * @param opacity
     * @param <T>
     */
    public static <T> void updateOpacity(Collection<? extends Shape3D> list, double opacity) {
        if (list != null) {
            for (var shape : list) {
                var color = ((PhongMaterial) shape.getMaterial()).getDiffuseColor();
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
                ((PhongMaterial) shape.getMaterial()).setDiffuseColor(color);
            }
        }
    }


    /**
     * Flatten concatenate a list of lists
     * @param lists
     * @return
     * @param <T>
     */
    public static <T> Collection <? extends T> flatten (Collection <?extends Collection <? extends T>> lists){
        return lists.stream().flatMap(Collection ::stream).collect(Collectors.toList());
    }

    /**
     * ShowLoadingPane makes the LoadingPane visible and  set its layout on the midlle of the scene
     * @param controller
     * @param stage
     */

    public void showLoadingPane(WindowController controller, Stage stage) {

        // Bind the min width and min height of the loadingPane to the width and height of the stage
        StackPane load = controller.getLoadingPane();
        load.minWidthProperty().bind(stage.widthProperty());
        load.minHeightProperty().bind(stage.heightProperty());

        //Add darker backgorund
        load.setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);"); // Darker background

        //Make the pane visible
        load.setVisible(true);

    }

    /**
     * MouseRotate Class set the rotation of a given node in a figure
     */
    public static class MouseRotate3D {
        private static double xPrev;
        private static double yPrev;

        public static void setup(Node target, Node figure) {
            target.setOnMousePressed(e -> {
                xPrev = e.getSceneX();
                yPrev = e.getSceneY();
            });

            target.setOnMouseDragged(e -> {
                var dx = e.getSceneX() - xPrev;
                var dy = e.getSceneY() - yPrev;
                final var orthogonalAxis = new Point3D(dy, -dx, 0);
                var rotate = new Rotate(0.25 * orthogonalAxis.magnitude(), orthogonalAxis);
                if (figure.getTransforms().isEmpty()) {
                    figure.getTransforms().add(rotate);
                } else {
                    var oldTransform = figure.getTransforms().get(0);
                    var newTransform = rotate.createConcatenation(oldTransform);
                    figure.getTransforms().set(0, newTransform);
                }
                xPrev = e.getSceneX();
                yPrev = e.getSceneY();
            });
        }
    }

    /**
     * SelectionModel interface provides methods to add, remove or access selected elements
     * @param <T>
     */

    public interface SelectionModel<T>{
        boolean isSelected(T t);
        boolean setSelected (T t, boolean select);
        boolean selectAll (Collection<T> list);
        void clearSelection();
        boolean clearSelection(T t);
        boolean clearSelection (Collection <T> list);
        ObservableSet<T> getSelectedItems();
    }

    //All classes and interfaces related to the undo/redo functionality
    /**
     * command interface
     * Daniel Huson 6.2023
     */
    public interface Command {
        void undo();

        void redo();

        String name();

        boolean canUndo();

        boolean canRedo();
    }

    /**
     * simple command
     * Daniel Huson 6.2023
     */
    public class SimpleCommand implements Command {
        private final String name;
        private final Runnable runUndo;
        private final Runnable runRedo;

        public SimpleCommand(String name, Runnable runUndo, Runnable runRedo) {
            this.name = name;
            this.runUndo = runUndo;
            this.runRedo = runRedo;
        }

        @Override
        public void undo() {
            runUndo.run();
        }

        @Override
        public void redo() {
            runRedo.run();
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean canUndo() {
            return runUndo != null;
        }

        @Override
        public boolean canRedo() {
            return runRedo != null;
        }
    }


    /**
     * property command
     * Daniel Huson 6.2023
     */
    public class PropertyCommand<T> extends SimpleCommand {
        public PropertyCommand(String name, Property<T> v, T oldValue, T newValue) {
            super(name, () -> v.setValue(oldValue), () -> v.setValue(newValue));
        }
    }

    /**
     * Manages the undo and redo stacks
     * Daniel Huson, 6.2023
     */
    public class UndoRedoManager {
        private final ObservableList<Command> undoStack = FXCollections.observableArrayList();
        private final ObservableList<Command> redoStack = FXCollections.observableArrayList();

        private final StringProperty undoLabel = new SimpleStringProperty("Undo");
        private final StringProperty redoLabel = new SimpleStringProperty("Redo");
        private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
        private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

        private final BooleanProperty inUndoRedo = new SimpleBooleanProperty(false);
        // this is used to prevent adding an undoable event when undoing or redoing an event
        // when undoing or redoing changes a property that is being observed so as to add to the undo stack

        public UndoRedoManager() {
            undoStack.addListener((InvalidationListener) e -> {
                undoLabel.set("Undo " + (undoStack.size() == 0 ? "-" : undoStack.get(undoStack.size() - 1).name()));
            });
            redoStack.addListener((InvalidationListener) e -> {
                redoLabel.set("Redo " + (redoStack.size() == 0 ? "-" : redoStack.get(redoStack.size() - 1).name()));
            });
            canUndo.bind(Bindings.size(undoStack).isNotEqualTo(0));
            canRedo.bind(Bindings.size(redoStack).isNotEqualTo(0));
        }

        public void undo() {
            inUndoRedo.set(true);
            try {
                if (isCanUndo()) {
                    var command = undoStack.remove(undoStack.size() - 1);
                    command.undo();
                    if (command.canRedo())
                        redoStack.add(command);
                }
            } finally {
                inUndoRedo.set(false);
            }
        }

        public void redo() {
            inUndoRedo.set(true);
            try {
                if (isCanRedo()) {
                    var command = redoStack.remove(redoStack.size() - 1);
                    command.redo();
                    if (command.canUndo())
                        undoStack.add(command);
                }
            } finally {
                inUndoRedo.set(false);
            }
        }

        public void add(Command command) {
            if (!isInUndoRedo()) {
                if (command.canUndo())
                    undoStack.add(command);
                else
                    undoStack.clear();
            }
        }

        public void addAndExecute(Command command) {
            if (!isInUndoRedo()) {
                add(command);
                command.redo();
            }
        }


        public void clear() {
            undoStack.clear();
            redoStack.clear();
        }

        public String getUndoLabel() {
            return undoLabel.get();
        }

        public ReadOnlyStringProperty undoLabelProperty() {
            return undoLabel;
        }

        public String getRedoLabel() {
            return redoLabel.get();
        }

        public ReadOnlyStringProperty redoLabelProperty() {
            return redoLabel;
        }

        public boolean isCanUndo() {
            return canUndo.get();
        }

        public ReadOnlyBooleanProperty canUndoProperty() {
            return canUndo;
        }

        public boolean isCanRedo() {
            return canRedo.get();
        }

        public ReadOnlyBooleanProperty canRedoProperty() {
            return canRedo;
        }

        public boolean isInUndoRedo() {
            return inUndoRedo.get();
        }

        public ReadOnlyBooleanProperty inUndoRedoProperty() {
            return inUndoRedo;
        }
    }




}
