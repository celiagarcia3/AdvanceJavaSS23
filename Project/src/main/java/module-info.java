module com.example.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.json;


    opens com.example.project to javafx.fxml;
    exports com.example.project;
    opens com.example.project.window to javafx.fxml;
    exports com.example.project.window;

}