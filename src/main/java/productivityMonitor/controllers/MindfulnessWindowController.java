package productivityMonitor.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.Random;

import static productivityMonitor.FocusMode.isPaused;
import static productivityMonitor.utils.SharedData.motivationMessagesList;
import static productivityMonitor.controllers.MainController.countAlertWindow;


public class MindfulnessWindowController {
    private Stage thisStage;

    public MindfulnessWindowController(){}

    public void setThisStage(Stage thisStage) {
        this.thisStage = thisStage;
    }

    @FXML
    private Label quoteLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Button agreeButton;

    @FXML
    private void handleAgree(){
        countAlertWindow--;
        isPaused=false;
        thisStage.close();
    }

    @FXML
    private Button refuseButton;

    @FXML
    private void handleRefuse(){
        //countAlertWindow++;
        isPaused=false;
        thisStage.close();
    }

    @FXML
    private void initialize(){
        Random rand = new Random();
        quoteLabel.setText(motivationMessagesList.get(rand.nextInt(motivationMessagesList.size())));
    }
}
