module eneev.monitor1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires jdk.compiler;


    opens productivityMonitor to javafx.fxml;
    exports productivityMonitor;
    exports productivityMonitor.controllers;
    opens productivityMonitor.controllers to javafx.fxml;
}