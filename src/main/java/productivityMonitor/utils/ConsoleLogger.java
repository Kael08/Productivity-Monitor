package productivityMonitor.utils;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class ConsoleLogger {
    private final TextArea consoleTextArea;

    public ConsoleLogger(TextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
    }

    public void log(String message) {
        if (consoleTextArea == null) {
            System.out.println("ConsoleTextArea is null! Message: " + message);
            return;
        }
        Platform.runLater(() -> consoleTextArea.appendText(message));
    }
}