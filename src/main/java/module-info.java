module eneev.monitor1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;


    opens productivityMonitor to javafx.fxml;
    exports productivityMonitor;
    exports productivityMonitor.controllers;
    opens productivityMonitor.controllers to javafx.fxml;
}