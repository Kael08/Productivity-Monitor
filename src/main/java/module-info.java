module eneev.monitor1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires jdk.compiler;
    requires Java.WebSocket;
    requires com.google.gson;
    requires org.json;


    opens productivityMonitor to javafx.fxml;
    exports productivityMonitor;
    exports productivityMonitor.controllers;
    opens productivityMonitor.controllers to javafx.fxml;
    exports productivityMonitor.utils;
    opens productivityMonitor.utils to javafx.fxml, com.google.gson;
}