package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

import static productivityMonitor.utils.SharedData.minutes;

public class TimerController {
    @FXML
    private TextField hourTextField;

    @FXML
    private TextField minuteTextField;

    @FXML
    private TextArea consoleTextArea;

    @FXML
    private Button setButton;
    @FXML
    private void handleSetTime(ActionEvent event){
        int minuteVal =0;
        int hourVal = 0;

        if(!hourTextField.getText().isEmpty()&&Integer.parseInt(hourTextField.getText())!=0){
            hourVal=Integer.parseInt(hourTextField.getText());
        }

        if(!minuteTextField.getText().isEmpty()&&Integer.parseInt(minuteTextField.getText())!=0){
            minuteVal=Integer.parseInt(minuteTextField.getText());
        }

        if(hourVal==0&&minuteVal==0)
        {
            consoleTextArea.appendText("Значения не должны быть равны нулю или быть пустыми!\n");
        } else {
            minutes=0;
            minutes+=(hourVal*60)+minuteVal;
            consoleTextArea.appendText("Время установлено!\n");
        }
    }

    @FXML
    private Button clearButton;
    @FXML
    private void handleClearTime(ActionEvent event){
        minutes = 0;
        consoleTextArea.appendText("Время убрано!\n");
    }

    @FXML
    public void initialize(){
        // Фильтр для записи только двух цифр и только цифр в поля для записи времени
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,2}")) {
                return change;
            }
            return null;
        };

        hourTextField.setTextFormatter(new TextFormatter<>(filter));
        minuteTextField.setTextFormatter(new TextFormatter<>(filter));
    }
}
