package productivityMonitor.controllers.modeWindowControllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;
import java.util.Random;

import static productivityMonitor.services.MonitoringManager.isTaskCompleted;
import static productivityMonitor.utils.SharedData.sailorsKnotTextList;

public class SailorsKnotWindowController implements ModeWindow {
    // Label
    @FXML private Label enterTextLabel;

    // TextArea
    @FXML private TextArea taskTextArea;
    @FXML private TextArea answerTextArea;

    // Button
    @FXML private Button enterButton;

    Stage currentStage;

    @Override
    public void setStage(Stage currentStage){
        this.currentStage=currentStage;
    }

    @FXML private void handleEnter(ActionEvent event){
        String task = taskTextArea.getText(),
        answer=answerTextArea.getText();

        if(task.equals(answer)){
            isTaskCompleted=true;

            currentStage.close();
        } else{
            answerTextArea.setText("");
        }
    }

    @FXML private void initialize(){
        Random rand = new Random();
        taskTextArea.setText(sailorsKnotTextList.get(rand.nextInt(sailorsKnotTextList.size())));
    }
}
