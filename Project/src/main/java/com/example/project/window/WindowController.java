
package com.example.project.window;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

public class WindowController {

    @FXML
    private TextField PDBentryField;

    @FXML
    private MenuItem aboutButton;

    @FXML
    private Slider ballSlider;

    @FXML
    private CheckBox ballsCheckBox;

    @FXML
    private MenuItem closeButton;

    @FXML
    private StackPane loadingPane;


    @FXML
    private AnchorPane rootPane;

    @FXML
    private ChoiceBox<String> colorChoiceButton;
    void initializeOptions() {
        ObservableList<String> items = FXCollections.observableArrayList(
                "Atom",
                "Aminoacid",
                "Secondary structure",
                "Molecule");
        getColorChoiceButton().setItems(items);
        getColorChoiceButton().setValue(items.get(0));
    }


    @FXML
    private MenuItem copyButton;

    @FXML
    private MenuItem darkButton;

    @FXML
    private ListView<String> entriesList;

    @FXML
    private MenuItem exitScreenButton;

    @FXML
    private Pane figurePane;

    @FXML
    private MenuItem fullScreenButton;

    @FXML
    private MenuItem lightButton;

    @FXML
    private MenuItem openButton;

    @FXML
    private MenuItem redoButton;

    @FXML
    private Slider ribbonSlider;

    @FXML
    private CheckBox ribbonsCheckBox;

    @FXML
    private MenuItem saveButton;

    @FXML
    private MenuItem showBallsButton;

    @FXML
    private MenuItem showRibbonsButton;

    @FXML
    private MenuItem showSticksButton;

    @FXML
    private CheckBox stickCheckBox;

    @FXML
    private Slider stickSlider;

    @FXML
    private TextFlow textFlow;

    @FXML
    private MenuItem undoButton;

    @FXML
    private TitledPane aboutPane;

    @FXML
    private Button exitButton;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Label modelLabel;

    @FXML
    private ChoiceBox<String> modelsChoiceBox;

    public Label getModelLabel() {
        return modelLabel;
    }

    public ChoiceBox<String> getModelsChoiceBox() {
        return modelsChoiceBox;
    }

    public AnchorPane getRootPane() {
        return rootPane;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public StackPane getLoadingPane() {
        return loadingPane;
    }

    public Button getExitButton() {
        return exitButton;
    }

    public TitledPane getAboutPane() {
        return aboutPane;
    }

    public ListView<String> getEntriesList() {
        return entriesList;
    }

    public TextField getPDBentryField() {
        return PDBentryField;
    }

    public MenuItem getAboutButton() {
        return aboutButton;
    }

    public Slider getBallSlider() {
        return ballSlider;
    }

    public CheckBox getBallsCheckBox() {
        return ballsCheckBox;
    }

    public MenuItem getCloseButton() {
        return closeButton;
    }

    public ChoiceBox<String> getColorChoiceButton() {
        return colorChoiceButton;
    }

    public MenuItem getCopyButton() {
        return copyButton;
    }

    public MenuItem getDarkButton() {
        return darkButton;
    }


    public MenuItem getExitScreenButton() {
        return exitScreenButton;
    }

    public Pane getFigurePane() {
        return figurePane;
    }

    public MenuItem getFullScreenButton() {
        return fullScreenButton;
    }

    public MenuItem getLightButton() {
        return lightButton;
    }

    public MenuItem getOpenButton() {
        return openButton;
    }

    public MenuItem getRedoButton() {
        return redoButton;
    }

    public Slider getRibbonSlider() {
        return ribbonSlider;
    }

    public CheckBox getRibbonsCheckBox() {
        return ribbonsCheckBox;
    }

    public MenuItem getSaveButton() {
        return saveButton;
    }

    public MenuItem getShowBallsButton() {
        return showBallsButton;
    }

    public MenuItem getShowRibbonsButton() {
        return showRibbonsButton;
    }

    public MenuItem getShowSticksButton() {
        return showSticksButton;
    }

    public CheckBox getStickCheckBox() {
        return stickCheckBox;
    }

    public Slider getStickSlider() {
        return stickSlider;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public MenuItem getUndoButton() {
        return undoButton;
    }
}

