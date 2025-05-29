package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
import static productivityMonitor.services.StageService.replaceScene;
import static productivityMonitor.utils.DataLoader.saveLocalizationToFile;

public class RegController {
    // VBox
    @FXML private VBox rootVBox;

    // ImageView
    @FXML private ImageView iconImageView;

    // Image
    //private Image iconImage = new Image(getClass().getResource("/images/purple/icon.png").toExternalForm());

    // TextField
    @FXML private TextField loginTextField;
    @FXML private TextField passwordTextField;
    @FXML private TextField usernameTextField;

    // Button
    @FXML private Button authButton;
    @FXML private Button regButton;

    private final HttpClient client = HttpClient.newHttpClient();
    private Stage currentStage;

    // Функция для POST-запросов на авторизацию
    private int sendRegRequest(String login,String password, String username){
        try{
            String json = String.format("{\"login\": \"%s\", \"password\": \"%s\", \"username\": \"%s\"}", login, password,username);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/reg"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            return response.statusCode();
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    @FXML private void handleAuthButton(ActionEvent event) throws IOException {
        if(currentStage==null) {
            currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();// Получаем ссылку на текущую сцену
        }
        replaceScene("/fxml/authView.fxml","Authentification",currentStage,false);// Заменяем текущее окно на окно авторизации
    }// Нажатие кнопки перехода на авторизацию
    @FXML private void handleRegButton(ActionEvent event){
        String login = loginTextField.getText();
        String password = passwordTextField.getText();
        String username = usernameTextField.getText();

        if(login.isEmpty()||password.isEmpty()||username.isEmpty())
        {
            System.out.println("Все поля должны быть заполнены!");
            return;
        }

        int status = sendRegRequest(login,password,username);

        if(status==201){
            try {
                if(currentStage==null) {
                    currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();// Получаем ссылку на текущую сцену
                }
                replaceScene("/fxml/authView.fxml","Authentification",currentStage,false);// Заменяем текущее окно на окно авторизации
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }// Нажатие кнопки регистрации

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("reg.title"));
        loginTextField.setPromptText(bundle.getString("reg.login"));
        passwordTextField.setPromptText(bundle.getString("reg.password"));
        usernameTextField.setPromptText(bundle.getString("reg.username"));
        regButton.setText(bundle.getString("reg.signup"));
        authButton.setText(bundle.getString("reg.noacc"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
        localization=lang;
        saveLocalizationToFile(lang);
    }

    @FXML public void initialize(){
        setLocalization(getLang());

        iconImageView.setImage(iconImg);// Установка картинки для иконки

        rootVBox.getStylesheets().add(getClass().getResource(regStylePath).toExternalForm());
    }
}