package productivityMonitor.controllers.modeWindowControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ResourceBundle;

import static productivityMonitor.controllers.SettingsController.getLang;
import static productivityMonitor.services.MonitoringManager.isDelayOver;
import static productivityMonitor.services.MonitoringManager.isDelayRunning;

public class DelayGratificationWindowController implements ModeWindow {
    @FXML private Label infoLabel;
    @FXML private Label timerLabel;

    private int seconds = 300; // 5 минут
    private Stage currentStage;
    private ResourceBundle bundle;

    @Override
    public void setStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    private void applyLocalization() {
        infoLabel.setText(bundle.getString("delayGratification.info"));
    }

    private void updateTimer(Label label) {
        seconds--;
        if (seconds < 1) {
            isDelayOver = true;
            isDelayRunning = false;
            currentStage.close();
        } else {
            timerLabel.setText(calcTime(seconds));
        }
    }

    private String calcTime(int val) {
        int minutes = val / 60;
        int seconds = val % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    @FXML private void initialize() {
        setLocalization(getLang());

        timerLabel.setText(calcTime(seconds));
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updateTimer(timerLabel));
            }
        }, 0, 1000);
    }
}