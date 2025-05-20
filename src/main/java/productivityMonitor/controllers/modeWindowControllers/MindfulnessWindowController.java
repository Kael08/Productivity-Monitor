package productivityMonitor.controllers.modeWindowControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;

import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import static productivityMonitor.controllers.SettingsController.getLang;
import static productivityMonitor.services.MonitoringManager.*;
import static productivityMonitor.services.SettingsService.mindfulnessWindowStylePath;
import static productivityMonitor.services.SettingsService.statisticsStylePath;

public class MindfulnessWindowController implements ModeWindow {
    @FXML private BorderPane rootPane;

    @FXML private Label quoteLabel;
    @FXML private Label messageLabel;
    @FXML private Button agreeButton;
    @FXML private Button refuseButton;

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
        messageLabel.setText(bundle.getString("mindfulness.message"));
        agreeButton.setText(bundle.getString("mindfulness.agree"));
        refuseButton.setText(bundle.getString("mindfulness.refuse"));
    }

    @FXML private void handleAgree() {
        countAlertWindow--;
        isPaused = false;
        currentStage.close();
    }

    @FXML private void handleRefuse() {
        isPaused = false;
        currentStage.close();
    }

    @FXML private void initialize() {
        setLocalization(getLang());

        Random rand = new Random();
        quoteLabel.setText(motivationMessagesList.get(rand.nextInt(motivationMessagesList.size())));

        rootPane.getStylesheets().add(getClass().getResource(mindfulnessWindowStylePath).toExternalForm());
    }
}