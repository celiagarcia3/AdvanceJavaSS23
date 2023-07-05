package com.example.project;

import com.example.project.model.PdbComplex;
import com.example.project.window.WindowPresenter;
import com.example.project.window.WindowView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class MainPdbViewer extends Application {
    public void start(Stage stage) throws Exception {
        var view=new WindowView();
        List<PdbComplex> model= new ArrayList<>();
        var presenter=new WindowPresenter(stage,view,model);

        stage.setScene(new Scene(view.getRoot()));
        stage.setTitle("PDB Viewer");
        stage.show();
    }
}
