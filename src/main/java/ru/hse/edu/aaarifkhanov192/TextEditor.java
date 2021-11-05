package ru.hse.edu.aaarifkhanov192;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class TextEditor extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TextEditor.class.getResource("/ru.hse.edu.aaarifkhanov192/editor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("TextEditor");
        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}