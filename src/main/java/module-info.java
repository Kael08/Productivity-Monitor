module eneev.productivityMonitor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires jdk.compiler;
    requires Java.WebSocket;
    requires com.google.gson;
    requires org.json;

    exports productivityMonitor.controllers;
    opens productivityMonitor.controllers to javafx.fxml;
    exports productivityMonitor.utils;
    opens productivityMonitor.utils to javafx.fxml, com.google.gson;
    exports productivityMonitor.models;
    opens productivityMonitor.models to com.google.gson, javafx.fxml;
    exports productivityMonitor.application;
    opens productivityMonitor.application to javafx.fxml;
    exports productivityMonitor.services;
    opens productivityMonitor.services to com.google.gson, javafx.fxml;
    exports productivityMonitor.controllers.modeWindowControllers;
    opens productivityMonitor.controllers.modeWindowControllers to javafx.fxml;
}