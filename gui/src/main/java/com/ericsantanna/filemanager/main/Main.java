package com.ericsantanna.filemanager.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var mainWindowFxml = getClass().getResource("/fxml/mainWindow/mainWindow.fxml");
        if(mainWindowFxml == null) {
            throw new Exception("/fxml/mainWindow/mainWindow.fxml not found");
        }
        Parent root = FXMLLoader.load(mainWindowFxml);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("File Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
