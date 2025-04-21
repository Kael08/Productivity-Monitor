package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import productivityMonitor.FocusMode;

import static productivityMonitor.utils.SharedData.*;

public class MonitoringSettingsController {
    @FXML
    private ComboBox<String> processListComboBox;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextArea consoleTextArea;

    @FXML
    private TextField inputTextField;

    @FXML
    private TextField inputUrlTextField;

    @FXML
    private CheckBox blockDomainCheckBox;

    @FXML
    private ComboBox<String> modeListComboBox;

    @FXML
    private ComboBox<String> urlListComboBox;

    private FocusMode focusMode;


    public void setFocusMode(FocusMode focusMode){
        this.focusMode=focusMode;
    }

    @FXML
    private void handleDeleteProcess(ActionEvent event){
        String selectedProcess = processListComboBox.getValue();
        if(selectedProcess==null){
            consoleTextArea.appendText("Ошибка: процесс не выбран\n");
        } else {
            Boolean isRemoved = processList.removeIf(process->process.equals(selectedProcess));

            if(isRemoved){
                consoleTextArea.appendText("Процесс успешно удален!\n");
            } else {
                consoleTextArea.appendText("Ошибка: Процесс с именем '" + selectedProcess + "' не найден!\n");
            }
        }
    }

    @FXML
    private void handleAddProcess(ActionEvent event){
        String processName = inputTextField.getText();

        if(processName.isEmpty()) {
            consoleTextArea.appendText("Ошибка: Название процесса не может быть пустым!\n");
            return;
        } else if(processName.equals(".exe")) {
            consoleTextArea.appendText("Ошибка: Введите название процесса!\n");
            return;
        }

        if(!processName.endsWith(".exe")){
            processName+=".exe";
        }

        consoleTextArea.appendText("Процесс "+processName+" добавлен\n");
        processList.add(processName);
    }

    @FXML
    private void handleDeleteUrl(ActionEvent event){
        String selectedUrl = urlListComboBox.getValue();

        if(selectedUrl==null){
            consoleTextArea.appendText("Ошибка: домен не выбран\n");
        } else {
            Boolean isRemoved = urlList.removeIf(url->url.equals(selectedUrl));

            if(isRemoved){
                consoleTextArea.appendText("Домен успешно удален!\n");
            } else {
                consoleTextArea.appendText("Ошибка: домен "+selectedUrl+" не найден!\n");
            }
        }
    }

    @FXML
    private void handleAddUrl(ActionEvent event){
        String urlName = inputUrlTextField.getText();

        if(urlName.isEmpty()){
            consoleTextArea.appendText("Ошибка: Домен не может быть пустым!\n");
            return;
        }

        consoleTextArea.appendText("Домен "+urlName+" добавлен\n");
        urlList.add(urlName);
    }

    public void initialize(){
        processListComboBox.setItems(processList);
        urlListComboBox.setItems(urlList);
        modeListComboBox.setItems(modeList);
        modeListComboBox.setValue("FullLockdown");

        blockDomainCheckBox.setSelected(isWebSocketServerActive);
        blockDomainCheckBox.setSelected(isDomainBlockerActive);

        blockDomainCheckBox.setOnAction(event->{
            if(blockDomainCheckBox.isSelected()) {
                isWebSocketServerActive = true;
                isDomainBlockerActive=true;
            }
            else {
                isWebSocketServerActive = false;
                isDomainBlockerActive=false;
            }
        });

        modeListComboBox.setOnAction(actionEvent -> {
            switch (modeListComboBox.getValue()){
                case "FullLockdown":
                    Platform.runLater(()->{
                        focusMode.setFullLockdownMode();
                    });
                    break;
                case "Mindfulless":
                    Platform.runLater(()->{
                        focusMode.setMindfulness();
                    });
                    break;
                case "Sailors's Knot":
                    Platform.runLater(()->{
                        focusMode.setSailorsKnot();
                    });
                    break;
                case "Delay Gratification":
                    Platform.runLater(()->{
                        focusMode.setDelayGratification();
                    });
                    break;
                case "Pomodoro":
                    Platform.runLater(()->{
                        focusMode.setPomodoro();
                    });
                    break;
            }
        });
    }
}
