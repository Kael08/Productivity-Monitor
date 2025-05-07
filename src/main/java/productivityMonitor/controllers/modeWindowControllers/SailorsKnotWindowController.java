package productivityMonitor.controllers.modeWindowControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;

import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

import static productivityMonitor.services.MonitoringManager.isTaskCompleted;
import static productivityMonitor.services.MonitoringManager.sailorsKnotTextList;

public class SailorsKnotWindowController implements ModeWindow {
    @FXML private Label enterTextLabel;
    @FXML private TextArea taskTextArea;
    @FXML private TextArea answerTextArea;
    @FXML private Button enterButton;

    Stage currentStage;
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
        enterTextLabel.setText(bundle.getString("sailorsKnot.enterText"));
        enterButton.setText(bundle.getString("sailorsKnot.enter"));
    }

    @FXML private void handleEnter(ActionEvent event) {
        String task = taskTextArea.getText();
        String answer = answerTextArea.getText();
        if (task.equals(answer)) {
            isTaskCompleted = true;
            currentStage.close();
        } else {
            answerTextArea.setText("");
        }
    }

    @FXML private void initialize() {
        Random rand = new Random();
        taskTextArea.setText(sailorsKnotTextList.get(rand.nextInt(sailorsKnotTextList.size())));
    }
}