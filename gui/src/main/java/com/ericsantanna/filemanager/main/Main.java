package com.ericsantanna.filemanager.main;

import com.ericsantanna.filemanager.utils.FxmlUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent fxml = FxmlUtils.loadFxml("/fxml/mainWindow/main-window.fxml");
        Scene scene = new Scene(fxml, 800, 600);
        stage.setOnCloseRequest((windowEvent) -> {
            Platform.exit();
            System.exit(0);
        });
        stage.setTitle("File Manager");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
