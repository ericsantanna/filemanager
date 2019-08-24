package com.ericsantanna.filemanager.controllers


import javafx.event.Event
import javafx.scene.Node
import javafx.stage.Stage

abstract class Controller {
    void closeWindow(Event event) {
        ((event.source as Node).scene.window as Stage).close()
    }
}
