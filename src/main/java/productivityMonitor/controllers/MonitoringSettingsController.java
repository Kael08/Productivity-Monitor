package productivityMonitor.controllers;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import productivityMonitor.FocusMode;
import productivityMonitor.utils.CustomMode;
import productivityMonitor.utils.JsonUtils;

import java.io.File;
import java.io.FileWriter;

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

    @FXML
    private Button openButton;

    @FXML
    private Button createButton;

    @FXML
    private Button editButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private boolean isEditing=false;

    @FXML
    private void handleOpenButton(ActionEvent event){

    }

    @FXML
    private void handleCreateButton(ActionEvent event){
        if(!isEditing){
            isEditing=true;
            saveButton.setVisible(true);
            cancelButton.setVisible(true);
            customModeNameTextField.setVisible(true);
        }
    }

    @FXML
    private void handleEditButton(ActionEvent event){

    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        String fileName = "newCustomMode";
        if (!customModeNameTextField.getText().isEmpty()) {
            fileName = customModeNameTextField.getText();
        }

        // Открываем окно выбора папки
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку для сохранения");

        // Можно поставить папку по умолчанию
        File defaultDirectory = new File("C:\\Users\\mingi\\Documents");
        if (defaultDirectory.exists()) {
            directoryChooser.setInitialDirectory(defaultDirectory);
        }

        Stage stage = (Stage) saveButton.getScene().getWindow(); // Получаем текущее окно
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName + ".json";

            try {
                // Создаем объект с данными
                CustomMode customMode = new CustomMode(fileName, currentMode, processList, urlList, isDomainBlockerActive);

                JsonUtils.saveCustomModeToFile(customMode, filePath);

                // TODO: Дописать добавление кастомного режима в customModeList
                customModeList.add(fileName);
                customModeListComboBox.setValue(fileName);
                customModeListOb.put(fileName,customMode);

                // Выход из режима редактирования
                isEditing = false;
                saveButton.setVisible(false);
                cancelButton.setVisible(false);
                customModeNameTextField.setVisible(false);
                customModeNameTextField.setText("Новый режим");

                consoleTextArea.appendText("Файл успешно сохранен: " + filePath + "\n");
            } catch (Exception e) {
                System.out.println("ОШИБКА: " + e.getMessage());
                consoleTextArea.appendText("ОШИБКА СОХРАНЕНИЯ ФАЙЛА: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        } else {
            consoleTextArea.appendText("Сохранение отменено пользователем\n");
        }
    }


    @FXML
    private void handleCancelButton(ActionEvent event){
        if(isEditing){
            isEditing=false;
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
            customModeNameTextField.setVisible(false);
        }
    }

    @FXML
    private TextField customModeNameTextField;

    @FXML
    private Label selectCustomModeLabel;

    @FXML
    private ComboBox<String> customModeListComboBox;

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

        inputTextField.clear();
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

        inputUrlTextField.clear();
        consoleTextArea.appendText("Домен "+urlName+" добавлен\n");
        urlList.add(urlName);
    }

    public void initialize(){
        processListComboBox.setItems(processList);
        urlListComboBox.setItems(urlList);
        modeListComboBox.setItems(modeList);
        modeListComboBox.setValue(currentMode);
        customModeListComboBox.setItems(customModeList);

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

        customModeListComboBox.setOnAction(actionEvent->{
            // TODO: Добавить запрет редактирования режима при выборе кастомного режима (под вопросом)
            // Получение экземпляра кастомного режима
            CustomMode customMode=customModeListOb.get(customModeListComboBox.getValue());

            // Установка режима
            modeListComboBox.setValue(customMode.modeName);

            // Установка процессов
            processList.clear();
            processList.setAll(customMode.getProcessList());

            // Установка флага для блокировки доменов
            isDomainBlockerActive=customMode.isDomainBlockerActive();

            // Установка списка доменов
            urlList.clear();
            urlList.setAll(customMode.getUrlList());
        });

        modeListComboBox.setOnAction(actionEvent -> {
            switch (modeListComboBox.getValue()){
                case "FullLockdown":
                    Platform.runLater(()->{
                        currentMode="FullLockdown";
                        focusMode.setFullLockdownMode();
                    });
                    break;
                case "Mindfulness":
                    Platform.runLater(()->{
                        currentMode="Mindfulness";
                        focusMode.setMindfulness();
                    });
                    break;
                case "Sailor's Knot":
                    Platform.runLater(()->{
                        currentMode="Sailor's Knot";
                        focusMode.setSailorsKnot();
                    });
                    break;
                case "Delay Gratification":
                    Platform.runLater(()->{
                        currentMode="Delay Gratification";
                        focusMode.setDelayGratification();
                    });
                    break;
                case "Pomodoro":
                    Platform.runLater(()->{
                        currentMode="Pomodoro";
                        focusMode.setPomodoro();
                    });
                    break;
            }
        });
    }
}
