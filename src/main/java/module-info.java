module eneev.monitor1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens productivityMonitor to javafx.fxml;
    exports productivityMonitor;
    exports productivityMonitor.controllers;
    opens productivityMonitor.controllers to javafx.fxml;
}