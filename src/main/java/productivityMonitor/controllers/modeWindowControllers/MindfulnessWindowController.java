package productivityMonitor.controllers.modeWindowControllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;
import java.util.Random;

import static productivityMonitor.services.MonitoringManager.isPaused;
import static productivityMonitor.utils.SharedData.motivationMessagesList;
import static productivityMonitor.services.MonitoringManager.countAlertWindow;


public class MindfulnessWindowController implements ModeWindow {
    // Label
    @FXML private Label quoteLabel;
    @FXML private Label messageLabel;

    // Button
    @FXML private Button agreeButton;
    @FXML private Button refuseButton;

    private Stage currentStage;

    @Override
    public void setStage(Stage currentStage){
        this.currentStage=currentStage;
    }

    @FXML private void handleAgree(){
        countAlertWindow--;
        isPaused=false;
        currentStage.close();
    }

    @FXML private void handleRefuse(){
        //countAlertWindow++;
        isPaused=false;
        currentStage.close();
    }

    @FXML private void initialize(){
        Random rand = new Random();
        quoteLabel.setText(motivationMessagesList.get(rand.nextInt(motivationMessagesList.size())));
    }
}
