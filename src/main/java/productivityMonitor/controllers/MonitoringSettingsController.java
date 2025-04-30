package productivityMonitor.controllers;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import productivityMonitor.FocusMode;
import productivityMonitor.utils.CustomMode;
import productivityMonitor.utils.JsonUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

    @FXML
    private Button deleteUrlButton;

    @FXML
    private Button addUrlButton;

    //TODO: Решить проблему с таймером в кастомных режимах

    //TODO: Решить проблему с блокировкой кнопок и режимом редактирования(open,create,edit)
    @FXML
    private void handleOpenButton(ActionEvent event){
        // Создание окна выбора файла
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с кастомным режимом");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files","*.json")
        );

        // Указание пути по умолчанию
        File defaultDirectory = new File("C:\\Users\\mingi\\Documents");
        if(defaultDirectory.exists()){
            fileChooser.setInitialDirectory(defaultDirectory);
        }

        Stage stage = (Stage) openButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if(selectedFile!=null){
            try{
                // Проверка, что файл соответствует структуре CustomMode
                if(!isCustomModeFile(selectedFile)){
                    consoleTextArea.appendText("ОШИБКА: файл не является валидным CustomMode\n");
                    return;
                }

                try {
                    CustomMode customMode=JsonUtils.loadCustomModeFromFile(selectedFile.getAbsolutePath());

                    // Добавляем режим в оба хранилища
                    if(!customModeListOb.containsKey(customMode.getName())){
                        customModeListOb.put(customMode.getName(),customMode);
                        customModeList.add(customMode.getName());
                        consoleTextArea.appendText("\""+customMode.getName()+"\" Добавлен!\n");
                    } else {
                        consoleTextArea.appendText("Режим с таким именем уже существует!\n");
                        // TODO: Добавить обработку для случая, когда режимы одинакового имени
                    }
                } catch (IOException e) {
                    System.out.println("ОШИБКА ПРИ ОТКРЫТИИ ФАЙЛА CustomMode: "+e.getMessage());
                    consoleTextArea.appendText("ОШИБКА ПРИ ОТКРЫТИИ ФАЙЛА CustomMode: "+e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e){
                System.out.println("Ошибка загрузки файла: " + e.getMessage());
                consoleTextArea.appendText("Ошибка загрузки файла: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        }
    }

    // Проверка, что файл соответствует Классу
    private boolean isCustomModeFile(File file){
        try{
            // Пробуем загрузить файл как CustomMode
            CustomMode temp = JsonUtils.loadCustomModeFromFile(file.getAbsolutePath());

            return temp!=null
                    &&temp.getName()!=null
                    && temp.getModeName() != null
                    && temp.getProcessList() != null
                    && temp.getUrlList() != null;
        }catch (Exception e){
            return false;
        }
    }

    @FXML
    private void handleCreateButton(ActionEvent event){
        createCustomModeInterface();
    }

    @FXML
    private void handleEditButton(ActionEvent event) {
        String selectedMode = customModeListComboBox.getValue();

        if(selectedMode == null || selectedMode.equals("<НЕТ>")) {
            consoleTextArea.appendText("Не выбран кастомный режим для редактирования!\n");
            return;
        }

        customModeNameTextField.setText(customModeListComboBox.getValue());
        editCustomModeInterface();
    }

    @FXML
    private void handleSaveButton(ActionEvent event) {
        //TODO: Ошибка: Что делать, если названия одинаковы

        String fileName = "newCustomMode";
        if (!customModeNameTextField.getText().isEmpty()) {
            fileName = customModeNameTextField.getText();
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите папку для сохранения");
        File defaultDirectory = new File("C:\\Users\\mingi\\Documents");
        if (defaultDirectory.exists()) {
            directoryChooser.setInitialDirectory(defaultDirectory);
        }

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            String filePath = selectedDirectory.getAbsolutePath() + File.separator + fileName + ".json";

            try {
                // Создаём объект CustomMode
                CustomMode customMode = new CustomMode(
                        fileName,
                        currentMode,
                        new ArrayList<>(processList),  // Копируем список, а не передаём ссылку
                        new ArrayList<>(urlList),     // Копируем список, а не передаём ссылку
                        isDomainBlockerActive
                );

                JsonUtils.saveCustomModeToFile(customMode, filePath);

                // Добавляем режим в оба хранилища
                if (!customModeListOb.containsKey(fileName)) {
                    customModeListOb.put(fileName, customMode);
                    customModeList.add(fileName);
                }else{
                    customModeListOb.put(fileName, customMode);
                }

                // Обновляем ComboBox
                customModeListComboBox.setItems(FXCollections.observableArrayList(customModeList));
                //customModeListComboBox.setValue(fileName);  // Устанавливаем выбранный режим


                clearInterface();

                consoleTextArea.appendText("Файл успешно сохранен: " + filePath + "\n");
            } catch (Exception e) {
                consoleTextArea.appendText("ОШИБКА СОХРАНЕНИЯ ФАЙЛА: " + e.getMessage() + "\n");
                e.printStackTrace();
            }
        } else {
            consoleTextArea.appendText("Сохранение отменено пользователем\n");
        }
    }


    @FXML
    private void handleCancelButton(ActionEvent event){
        clearInterface();
        customModeListComboBox.setValue("<НЕТ>");
    }

    // Метод для возвращения интерфейса к исходному варианту
    private void clearInterface(){
        openButton.setDisable(false);
        createButton.setDisable(false);
        editButton.setDisable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        customModeNameTextField.setVisible(false);
        customModeListComboBox.setDisable(false);
        modeListComboBox.setDisable(false);
        processListComboBox.setDisable(false);
        deleteButton.setDisable(false);
        inputTextField.setDisable(false);
        addButton.setDisable(false);
        blockDomainCheckBox.setDisable(false);
        urlListComboBox.setDisable(false);
        deleteUrlButton.setDisable(false);
        inputUrlTextField.setDisable(false);
        addUrlButton.setDisable(false);
        consoleTextArea.setDisable(false);
        customModeNameTextField.setText("Новый режим");
        customModeListComboBox.setValue("<НЕТ>");
    }

    // Переход к созданию кастомного режима
    private void createCustomModeInterface(){
        openButton.setDisable(true);
        createButton.setDisable(true);
        editButton.setDisable(true);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        customModeNameTextField.setVisible(true);
        customModeListComboBox.setDisable(true);
        modeListComboBox.setDisable(false);
        processListComboBox.setDisable(false);
        deleteButton.setDisable(false);
        inputTextField.setDisable(false);
        addButton.setDisable(false);
        blockDomainCheckBox.setDisable(false);
        urlListComboBox.setDisable(false);
        deleteUrlButton.setDisable(false);
        inputUrlTextField.setDisable(false);
        addUrlButton.setDisable(false);
        consoleTextArea.setDisable(false);
    }

    // Режим редактирования кастомного режима
    private void editCustomModeInterface(){
        openButton.setDisable(true);
        createButton.setDisable(true);
        editButton.setDisable(true);
        saveButton.setVisible(true);
        cancelButton.setVisible(true);
        customModeNameTextField.setVisible(false);
        customModeListComboBox.setDisable(true);
        modeListComboBox.setDisable(false);
        processListComboBox.setDisable(false);
        deleteButton.setDisable(false);
        inputTextField.setDisable(false);
        addButton.setDisable(false);
        blockDomainCheckBox.setDisable(false);
        urlListComboBox.setDisable(false);
        deleteUrlButton.setDisable(false);
        inputUrlTextField.setDisable(false);
        addUrlButton.setDisable(false);
        consoleTextArea.setDisable(false);
    }

    // Интерфейс при выборе кастомного режима
    private void customModeInterface(){
        openButton.setDisable(false);
        createButton.setDisable(false);
        editButton.setDisable(false);
        saveButton.setVisible(false);
        cancelButton.setVisible(false);
        customModeNameTextField.setVisible(false);
        customModeListComboBox.setDisable(false);
        modeListComboBox.setDisable(true);
        processListComboBox.setDisable(false);
        deleteButton.setDisable(true);
        inputTextField.setDisable(true);
        addButton.setDisable(true);
        blockDomainCheckBox.setDisable(true);
        urlListComboBox.setDisable(false);
        deleteUrlButton.setDisable(true);
        inputUrlTextField.setDisable(true);
        addUrlButton.setDisable(true);
        consoleTextArea.setDisable(false);
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

        // Добавление пустого кастомного режима
        if(!customModeListOb.containsKey("<НЕТ>")) {
            CustomMode emptyCustomMode = new CustomMode("<НЕТ>", "FullLockdown", new ArrayList<>(), new ArrayList<>(), true);
            customModeListOb.put("<НЕТ>", emptyCustomMode);

            if (!customModeList.contains("<НЕТ>")) {
                customModeList.add("<НЕТ>");
            }

            customModeListComboBox.setValue("<НЕТ>");
        }

        customModeListComboBox.setOnAction(actionEvent -> {
            String selectedMode = customModeListComboBox.getValue();
            if (selectedMode == null) {
                consoleTextArea.appendText("Ошибка: не выбран режим\n");
                return;
            }

            CustomMode customMode = customModeListOb.get(selectedMode);
            if (customMode == null) {
                consoleTextArea.appendText("Ошибка: режим '" + selectedMode + "' не найден\n");
                return;
            }

            // Установка значений
            modeListComboBox.setValue(customMode.modeName);
            processList.clear();
            processList.setAll(customMode.getProcessList());
            isDomainBlockerActive = customMode.isDomainBlockerActive();
            blockDomainCheckBox.setSelected(isDomainBlockerActive);
            urlList.clear();
            urlList.setAll(customMode.getUrlList());

            // Управление блокировкой элементов
            if(!selectedMode.equals("<НЕТ>")) {
                customModeInterface();
            } else {
                clearInterface();
            }
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
