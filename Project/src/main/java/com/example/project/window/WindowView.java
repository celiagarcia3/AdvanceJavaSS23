package com.example.project.window;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Objects;

public class WindowView {
    private Parent root=null;
    private WindowController controller=null;
    public WindowView() throws IOException {
        try(var ins= Objects.requireNonNull(getClass().getResource("Window.fxml")).openStream()) {
            var fxmlLoader = new FXMLLoader();
            fxmlLoader.load(ins);

            controller = fxmlLoader.getController();
            root = fxmlLoader.getRoot();
        }
    }

    public Parent getRoot() {
        return root;
    }

    public WindowController getController() {
        return controller;
    }
}
