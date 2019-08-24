package com.ericsantanna.filemanager.utils

import javafx.fxml.FXMLLoader
import javafx.scene.Parent

class FxmlUtils {
    static loadFxml(String fxmlPath) throws Exception {
        def mainWindowFxml = FxmlUtils.getResource(fxmlPath)
        if(mainWindowFxml == null) {
            throw new Exception(fxmlPath + " not found")
        }
        def loader = new FXMLLoader(mainWindowFxml)
        Parent node = loader.load()
        def controller = loader.getController()
        return [loader: loader, node: node, controller: controller]
    }
}
