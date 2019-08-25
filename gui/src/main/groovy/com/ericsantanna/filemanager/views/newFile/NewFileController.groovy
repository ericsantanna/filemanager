package com.ericsantanna.filemanager.views.newFile

import com.ericsantanna.filemanager.controllers.Controller
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TextField

import java.nio.file.Files
import java.nio.file.Path

class NewFileController extends Controller implements Initializable {
    Path currentPath

    @FXML private TextField newFileName

    @Override
    void initialize(URL location, ResourceBundle resources) {
        Platform.runLater({
            newFileName.requestFocus()
        })
    }

    @FXML
    void onCreate(ActionEvent event) {
        Files.createFile(currentPath.resolve(newFileName.text))
        closeWindow(event)
    }

    @FXML
    void onCancel(ActionEvent event) {
        closeWindow(event)
    }
}
