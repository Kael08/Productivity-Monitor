package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.services.SettingsService.*;
import static productivityMonitor.utils.DataLoader.saveLocalizationToFile;
import static productivityMonitor.utils.TimerUtils.minutes;

public class TimerController {
    // Pane
    @FXML private VBox rootVBox;

    @FXML private TextField hourTextField;

    @FXML private TextField minuteTextField;

    @FXML private TextArea consoleTextArea;

    @FXML private Button setButton;
    @FXML private void handleSetTime(ActionEvent event){
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

    @FXML private Button clearButton;

    @FXML private void handleClearTime(ActionEvent event){
        minutes = 0;
        consoleTextArea.appendText("Время убрано!\n");
    }

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("timer.title"));
        setButton.setText(bundle.getString("timer.set"));
        clearButton.setText(bundle.getString("timer.clear"));
        minuteTextField.setPromptText(bundle.getString("timer.minutes"));
        hourTextField.setPromptText(bundle.getString("timer.hours"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
        localization=lang;
        saveLocalizationToFile(lang);
    }

    public static String getLang(){
        return localization;
    }

    @FXML
    public void initialize(){
        setLocalization(getLang());

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

        rootVBox.getStylesheets().add(getClass().getResource(timerStylePath).toExternalForm());
    }
}
