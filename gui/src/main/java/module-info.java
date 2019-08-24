module com.ericsantanna.filemanager.gui {
    requires javafx.controls;
    requires javafx.fxml;
//    requires static lombok;
    requires java.desktop;
    exports com.ericsantanna.filemanager.main;
    exports com.ericsantanna.filemanager.controllers;
    exports com.ericsantanna.filemanager.models;
    opens com.ericsantanna.filemanager.controllers;
    opens com.ericsantanna.filemanager.models;
}