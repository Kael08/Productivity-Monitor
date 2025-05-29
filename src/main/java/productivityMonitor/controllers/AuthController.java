package productivityMonitor.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.controllers.TimerController.getLang;
import static productivityMonitor.services.SettingsService.*;
import static productivityMonitor.services.TokenManager.setTokens;
import static productivityMonitor.services.TokenManager.updateUser;
import static productivityMonitor.services.StageService.replaceMainScene;
import static productivityMonitor.services.StageService.replaceScene;
import static productivityMonitor.utils.DataLoader.saveLocalizationToFile;

public class AuthController {
    // VBox
    @FXML private VBox rootVBox;

    // ImageView
    @FXML private ImageView iconImageView;

    // TextField
    @FXML private TextField loginTextField;
    @FXML private PasswordField passwordTextField;

    // Button
    @FXML private Button authButton;
    @FXML private Button regButton;

    private Stage currentStage;
    private final HttpClient client = HttpClient.newHttpClient();

    // Функция для POST-запросов на авторизацию
    private int sendAuthRequest(String login, String password) {
        try {
            String json = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", login, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/auth"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() == 200) {
                saveTokens(response.body());
            } else {
                loginTextField.clear();
                passwordTextField.clear();
            }

            return response.statusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Функция для сохранения токенов и загрузки данных пользователя
    private void saveTokens(String responseBody) {
        try (FileWriter fileWriter = new FileWriter("src/main/resources/data/REFRESH_TOKEN.json")) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            setTokens(jsonObject.get("accessToken").getAsString(), jsonObject.get("refreshToken").getAsString());
            updateUser();
        } catch (Exception e) {
            System.out.println("ОШИБКА ПРИ СОХРАНЕНИИ ТОКЕНОВ:" + e.getMessage());
            e.printStackTrace();
        }
    }

    // Показ ошибки в Alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Проверка полей на пустоту и минимальную длину
    private boolean validateFields(String login, String password) {
        if (login.isEmpty() || password.isEmpty()) {
            showErrorAlert(bundle.getString("error.empty_fields"), bundle.getString("error.fill_all_fields"));
            return false;
        }
        if (login.length() < 3) {
            showErrorAlert(bundle.getString("error.short_login"), bundle.getString("error.login_min_length"));
            return false;
        }
        if (password.length() < 6) {
            showErrorAlert(bundle.getString("error.short_password"), bundle.getString("error.password_min_length"));
            return false;
        }
        return true;
    }

    @FXML
    private void handleAuthButton(ActionEvent event) {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        if (!validateFields(login, password)) {
            return;
        }

        int status = sendAuthRequest(login, password);

        switch (status) {
            case 200:
                try {
                    replaceMainScene("/fxml/profileView.fxml", "Profile");
                    if (currentStage == null) {
                        currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    }
                    currentStage.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case 401:
                showErrorAlert(bundle.getString("error.auth_failed"), bundle.getString("error.invalid_credentials"));
                break;
            case 404:
                showErrorAlert(bundle.getString("error.user_not_found"), bundle.getString("error.user_not_exists"));
                break;
            case 0:
                showErrorAlert(bundle.getString("error.connection_failed"), bundle.getString("error.server_unavailable"));
                break;
            default:
                showErrorAlert(bundle.getString("error.unknown_error"), bundle.getString("error.try_again_later"));
        }
    }

    @FXML
    private void handleRegButton(ActionEvent event) throws IOException {
        if (currentStage == null) {
            currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        }
        replaceScene("/fxml/regView.fxml", "Registration", currentStage, false);
    }

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("auth.title"));
        loginTextField.setPromptText(bundle.getString("auth.login"));
        passwordTextField.setPromptText(bundle.getString("auth.password"));
        regButton.setText(bundle.getString("auth.noacc"));
        authButton.setText(bundle.getString("auth.signin"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
        localization = lang;
        saveLocalizationToFile(lang);
    }

    @FXML
    public void initialize() {
        setLocalization(getLang());
        iconImageView.setImage(iconImg);
        rootVBox.getStylesheets().add(getClass().getResource(authStylePath).toExternalForm());
    }
}