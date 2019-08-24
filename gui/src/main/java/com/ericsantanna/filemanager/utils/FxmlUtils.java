package com.ericsantanna.filemanager.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class FxmlUtils {
    public static Parent loadFxml(String fxmlPath) throws Exception {
        var mainWindowFxml = FxmlUtils.class.getResource(fxmlPath);
        if(mainWindowFxml == null) {
            throw new Exception(fxmlPath + " not found");
        }
        return FXMLLoader.load(mainWindowFxml);
    }
}
