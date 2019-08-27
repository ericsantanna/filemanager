package com.ericsantanna.filemanager.main

import com.ericsantanna.filemanager.utils.FxmlUtils
import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.stage.Stage

class Main extends Application {
    @Override
    void start(Stage stage) throws Exception {
        def fxml = FxmlUtils.loadFxml("/com/ericsantanna/filemanager/views/mainWindow/main-window.fxml")
        Scene scene = new Scene(fxml.node, 800, 600)
        stage.setOnCloseRequest({
            Platform.exit()
            System.exit(0)
        })
        stage.title = "File Manager"
        stage.scene = scene
        stage.show()
    }

    static void main(String[] args) {
        launch(Main, args)
    }
}
