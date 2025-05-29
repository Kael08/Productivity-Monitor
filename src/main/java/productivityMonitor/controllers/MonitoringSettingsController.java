package productivityMonitor.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;
import productivityMonitor.services.MonitoringManager;
import productivityMonitor.models.CustomMode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static productivityMonitor.controllers.SettingsController.getLang;
import static productivityMonitor.services.MonitoringManager.currentMode;
import static productivityMonitor.services.MonitoringManager.isWebSocketServerActive;
import static productivityMonitor.services.SettingsService.mainStylePath;
import static productivityMonitor.services.SettingsService.monitoringSettingsStylePath;
import static productivityMonitor.services.TokenManager.getAccessToken;
import static productivityMonitor.services.TokenManager.refreshAccessToken;
import static productivityMonitor.utils.DataLoader.loadCustomModeFromFile;
import static productivityMonitor.utils.DataLoader.saveCustomModeToFile;

public class MonitoringSettingsController {
    // Pane
    @FXML private BorderPane rootPane;

    // ComboBox
    @FXML private ComboBox<String> processListComboBox;
    @FXML private ComboBox<String> modeListComboBox;
    @FXML private ComboBox<String> urlListComboBox;
    @FXML private ComboBox<String> customModeListComboBox;

    // Button
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button openButton;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteUrlButton;
    @FXML private Button addUrlButton;
    @FXML private Button selectExeButton;

    @FXML private TextField inputTextField;
    @FXML private TextField inputUrlTextField;
    @FXML private TextField customModeNameTextField;

    // TextArea
    @FXML private TextArea consoleTextArea;

    // CheckBox
    @FXML private CheckBox blockDomainCheckBox;

    // Label
    @FXML private Label selectCustomModeLabel;
    @FXML private Label selectModeLabel;
    @FXML private Label blockDomainLabel;

    public static ObservableList<String> processList = FXCollections.observableArrayList();
    public static ObservableList<String> urlList = FXCollections.observableArrayList();
    public static ObservableList<String> modeList = FXCollections.observableArrayList("FullLockdown", "Mindfulness", "Sailor's Knot", "Delay Gratification", "Pomodoro");
    public static ObservableList<String> customModeList = FXCollections.observableArrayList();
    public static Map<String, CustomMode> customModeListOb = new HashMap<>();

    private final String API_BASE_URL = "http://localhost:3000";

    private final HttpClient httpClient = HttpClient. newHttpClient();

    private MonitoringManager monitoringManager;
    private ResourceBundle bundle;

    public void setMonitoringManager(MonitoringManager monitoringManager) {
        this.monitoringManager = monitoringManager;
    }

    public void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    private void applyLocalization() {
        openButton.setText(bundle.getString("monitoringSettings.open"));
        createButton.setText(bundle.getString("monitoringSettings.create"));
        editButton.setText(bundle.getString("monitoringSettings.edit"));
        saveButton.setText(bundle.getString("monitoringSettings.save"));
        cancelButton.setText(bundle.getString("monitoringSettings.cancel"));
        customModeNameTextField.setPromptText(bundle.getString("monitoringSettings.customModeName"));
        selectCustomModeLabel.setText(bundle.getString("monitoringSettings.selectCustomMode"));
        selectModeLabel.setText(bundle.getString("monitoringSettings.selectMode"));
        deleteButton.setText(bundle.getString("monitoringSettings.delete"));
        addButton.setText(bundle.getString("monitoringSettings.add"));
        blockDomainLabel.setText(bundle.getString("monitoringSettings.blockURL"));
        deleteUrlButton.setText(bundle.getString("monitoringSettings.deleteURL"));
        addUrlButton.setText(bundle.getString("monitoringSettings.addURL"));
        selectExeButton.setText(bundle.getString("monitoringSettings.selectExe"));
    }

    @FXML private void handleSelectExeButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("monitoringSettings.selectExeFile"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Executable Files", "*.exe"));

        File defaultDirectory = new File("C:\\Program Files");
        if (defaultDirectory.exists()) {
            fileChooser.setInitialDirectory(defaultDirectory);
        }

        Stage stage = (Stage) selectExeButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Запускаем .exe для получения имени процесса
                ProcessBuilder pb = new ProcessBuilder(selectedFile.getAbsolutePath());
                Process process = pb.start();

                // Получаем информацию о процессе
                ProcessHandle.Info info = process.info();
                String processName = info.command()
                        .map(path -> new File(path).getName())
                        .orElse(selectedFile.getName());

                // Уничтожаем процесс, чтобы не оставлять его открытым
                process.destroy();

                // Проверяем, что имя процесса заканчивается на .exe
                if (!processName.toLowerCase().endsWith(".exe")) {
                    consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorInvalidProcess"), processName) + "\n");
                    return;
                }

                // Добавляем процесс в список
                if (!processList.contains(processName)) {
                    processList.add(processName);
                    consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.processAdded"), processName) + "\n");
                } else {
                    consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorProcessExists"), processName) + "\n");
                }

            } catch (IOException e) {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorLaunchExe"), e.getMessage()) + "\n");
            }
        } else {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.selectCanceled") + "\n");
        }
    }

    @FXML private void handleOpenButton(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с кастомным режимом");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File defaultDirectory = new File("C:\\Users\\mingi\\Documents");
        if (defaultDirectory.exists()) {
            fileChooser.setInitialDirectory(defaultDirectory);
        }

        Stage stage = (Stage) openButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                if (!isCustomModeFile(selectedFile)) {
                    consoleTextArea.appendText(bundle.getString("monitoringSettings.errorInvalidCustomMode") + "\n");
                    return;
                }
                try {
                    CustomMode customMode = loadCustomModeFromFile(selectedFile.getAbsolutePath());
                    if (!customModeListOb.containsKey(customMode.getName())) {
                        customModeListOb.put(customMode.getName(), customMode);
                        customModeList.add(customMode.getName());
                        consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.customModeAdded"))+customMode.getName() + "\n");
                    } else {
                        consoleTextArea.appendText(bundle.getString("monitoringSettings.errorDuplicateMode") + "\n");
                    }
                } catch (IOException e) {
                    consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorOpenCustomMode"))+e.getMessage() + "\n");
                }
            } catch (Exception e) {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorLoadFile"))+e.getMessage() + "\n");
            }
        }
    }

    @FXML private void handleCreateButton(ActionEvent event) {
        createCustomModeInterface();
    }

    @FXML private void handleEditButton(ActionEvent event) {
        String selectedMode = customModeListComboBox.getValue();
        if (selectedMode == null || selectedMode.equals("<НЕТ>")) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorNoModeSelected") + "\n");
            return;
        }
        customModeNameTextField.setText(customModeListComboBox.getValue());
        editCustomModeInterface();
    }

    @FXML private void handleSaveButton(ActionEvent event) {
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
                CustomMode customMode = new CustomMode(
                        fileName,
                        currentMode.getName(),
                        new ArrayList<>(processList),
                        new ArrayList<>(urlList),
                        isWebSocketServerActive
                );

                // Сохранение кастомного режима на сервере
                if(refreshAccessToken()){
                    saveCustomModeOnServer(customMode);
                }

                saveCustomModeToFile(customMode, filePath);
                if (!customModeListOb.containsKey(fileName)) {
                    customModeListOb.put(fileName, customMode);
                    customModeList.add(fileName);
                } else {
                    customModeListOb.put(fileName, customMode);
                }
                customModeListComboBox.setItems(FXCollections.observableArrayList(customModeList));
                clearInterface();
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.fileSaved"))+filePath + "\n");
            } catch (Exception e) {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorSaveFile"))+e.getMessage() + "\n");
            }
        } else {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.saveCanceled") + "\n");
        }
    }

    @FXML private void handleCancelButton(ActionEvent event) {
        clearInterface();
        customModeListComboBox.setValue("<НЕТ>");
    }

    private boolean isCustomModeFile(File file) {
        try {
            CustomMode temp = loadCustomModeFromFile(file.getAbsolutePath());
            return temp != null
                    && temp.getName() != null
                    && temp.getModeName() != null
                    && temp.getProcessList() != null
                    && temp.getUrlList() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveCustomModeOnServer(CustomMode customMode) throws IOException, InterruptedException {
        JSONObject json=new JSONObject();
        json.put("name",customMode.getName());
        json.put("mode_name",customMode.getModeName());
        json.put("process_list",customMode.getProcessList());
        json.put("url_list",customMode.getUrlList());
        json.put("is_domain_blocker_active",customMode.isWebSocketServerActive());

        String authToken = getAccessToken();

        if(authToken==null|authToken.isEmpty()){
            System.out.println("ОШИБКА ПРИ ЗАГРУЗКЕ КАСТОМНОГО РЕЖИМА НА СЕРВЕР: Access-токен пуст!");
            return;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/customModes/add"))
                .header("Authorization", "Bearer " + authToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем, есть ли ошибка о существующем режиме
        if (response.statusCode() == 409) {

            HttpRequest patchRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/customModes/update"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json.toString()))
                    .build();

            HttpResponse<String> patchResponse = httpClient.send(patchRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("PATCH /customModes/update Response Code: " + patchResponse.statusCode());
            System.out.println("PATCH /customModes/update Response Body: " + patchResponse.body());
            if(patchResponse.statusCode()==200){
                System.out.println("Кастомный режим успешно обновлен на сервере!");
            }else{
                System.out.println("Ошибка при попытке сохранить кастомный режим на сервер!");
            }
        }else{
            System.out.println("Кастомный режим успешно сохранен на сервере!");
        }
    }

    private void clearInterface() {
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

    private void createCustomModeInterface() {
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

    private void editCustomModeInterface() {
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

    private void customModeInterface() {
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

    @FXML private void handleDeleteProcess(ActionEvent event) {
        String selectedProcess = processListComboBox.getValue();
        if (selectedProcess == null) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorNoProcess") + "\n");
        } else {
            Boolean isRemoved = processList.removeIf(process -> process.equals(selectedProcess));
            if (isRemoved) {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.processAdded"))+selectedProcess + "\n");
            } else {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorProcessNotFound"))+selectedProcess + "\n");
            }
        }
    }

    @FXML private void handleAddProcess(ActionEvent event) {
        String processName = inputTextField.getText();
        if (processName.isEmpty()) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorEmptyProcess") + "\n");
            return;
        } else if (processName.equals(".exe")) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorInvalidProcess") + "\n");
            return;
        }
        if (!processName.endsWith(".exe")) {
            processName += ".exe";
        }
        inputTextField.clear();
        consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.processAdded"))+processName + "\n");
        processList.add(processName);
    }

    @FXML private void handleDeleteUrl(ActionEvent event) {
        String selectedUrl = urlListComboBox.getValue();
        if (selectedUrl == null) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorNoURL") + "\n");
        } else {
            Boolean isRemoved = urlList.removeIf(url -> url.equals(selectedUrl));
            if (isRemoved) {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.urlAdded"))+selectedUrl + "\n");
            } else {
                consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.errorURLNotFound")) +selectedUrl+ "\n");
            }
        }
    }

    @FXML private void handleAddUrl(ActionEvent event) {
        String urlName = inputUrlTextField.getText();
        if (urlName.isEmpty()) {
            consoleTextArea.appendText(bundle.getString("monitoringSettings.errorEmptyURL") + "\n");
            return;
        }
        inputUrlTextField.clear();
        consoleTextArea.appendText(String.format(bundle.getString("monitoringSettings.urlAdded"))+urlName + "\n");
        urlList.add(urlName);
    }

    public void initialize() {
        setLocalization(getLang());

        processListComboBox.setItems(processList);
        urlListComboBox.setItems(urlList);
        modeListComboBox.setItems(modeList);
        modeListComboBox.setValue(currentMode.getName());
        customModeListComboBox.setItems(customModeList);

        blockDomainCheckBox.setSelected(isWebSocketServerActive);
        blockDomainCheckBox.setOnAction(event -> {
            if (blockDomainCheckBox.isSelected()) {
                isWebSocketServerActive = true;
            } else {
                isWebSocketServerActive = false;
            }
        });

        if (!customModeListOb.containsKey("<НЕТ>")) {
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
            modeListComboBox.setValue(customMode.modeName);
            processList.clear();
            processList.setAll(customMode.getProcessList());
            isWebSocketServerActive = customMode.isWebSocketServerActive();
            blockDomainCheckBox.setSelected(isWebSocketServerActive);
            urlList.clear();
            urlList.setAll(customMode.getUrlList());
            if (!selectedMode.equals("<НЕТ>")) {
                customModeInterface();
            } else {
                clearInterface();
            }
        });

        modeListComboBox.setOnAction(actionEvent -> {
            switch (modeListComboBox.getValue()) {
                case "FullLockdown":
                    Platform.runLater(() -> monitoringManager.setMode("FullLockdown"));
                    break;
                case "Mindfulness":
                    Platform.runLater(() -> monitoringManager.setMode("Mindfulness"));
                    break;
                case "Sailor's Knot":
                    Platform.runLater(() -> monitoringManager.setMode("Sailor's Knot"));
                    break;
                case "Delay Gratification":
                    Platform.runLater(() -> monitoringManager.setMode("Delay Gratification"));
                    break;
                case "Pomodoro":
                    Platform.runLater(() -> monitoringManager.setMode("Pomodoro"));
                    break;
            }
        });

        rootPane.getStylesheets().add(getClass().getResource(monitoringSettingsStylePath).toExternalForm());
    }
}