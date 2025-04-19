package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.Random;

import static productivityMonitor.FocusMode.isTaskCompleted;
import static productivityMonitor.utils.SharedData.sailorsKnotTextList;

public class SailorsKnotWindowController {
    private Stage thisStage;

    public SailorsKnotWindowController(){}

    public void setThisStage(Stage thisStage){
        this.thisStage=thisStage;
    }

    @FXML
    private Label enterTextField;

    @FXML
    private TextArea taskTextArea;

    @FXML
    private TextArea answerTextArea;

    @FXML
    private Button enterButton;

    @FXML
    private void handleEnter(ActionEvent actionEvent){
        String task = taskTextArea.getText(),
        answer=answerTextArea.getText();

        if(task.equals(answer)){
            isTaskCompleted=true;
            thisStage.close();
        } else{
            answerTextArea.setText("");
        }
    }

    @FXML
    private void initialize(){
        Random rand = new Random();

        taskTextArea.setText(sailorsKnotTextList.get(rand.nextInt(sailorsKnotTextList.size())));
    }
}
