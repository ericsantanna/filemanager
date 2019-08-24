package com.ericsantanna.filemanager.controllers

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField

import java.nio.file.Files
import java.nio.file.Path

class NewFolderController extends Controller implements Initializable {
    Path currentPath

    @FXML private TextField newFolderName

    @Override
    void initialize(URL location, ResourceBundle resources) {
        Platform.runLater({
            newFolderName.requestFocus()
        })
    }

    @FXML
    void onCreate(ActionEvent event) {
        Files.createDirectories(currentPath.resolve(newFolderName.text))
        closeWindow(event)
    }
    @FXML
    void onCancel(ActionEvent event) {
        closeWindow(event)
    }
}
