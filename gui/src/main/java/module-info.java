module com.ericsantanna.filemanager.gui {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.desktop;
    exports com.ericsantanna.filemanager.main;
    exports com.ericsantanna.filemanager.mainWindow;
    opens com.ericsantanna.filemanager.mainWindow;
    opens com.ericsantanna.filemanager.models;
}