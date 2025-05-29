package productivityMonitor.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
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

    // Image
    //private Image iconImage = new Image(getClass().getResource("/images/purple/icon.png").toExternalForm());

    // TextField
    @FXML private TextField loginTextField;
    @FXML private TextField passwordTextField;

    // Button
    @FXML private Button authButton;
    @FXML private Button regButton;

    private Stage currentStage;
    private final HttpClient client = HttpClient.newHttpClient();

    // Функция для POST-запросов на авторизацию
    private int sendAuthRequest(String login,String password){
        try{
            String json = String.format("{\"login\": \"%s\", \"password\": \"%s\"}",login,password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/auth"))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: "+response.statusCode());
            System.out.println("Response body: "+response.body());

            saveTokens(response.body());

            return response.statusCode();
        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }
    // Функция для сохранения токенов и загрузки данных пользователя
    private void saveTokens(String responseBody){
        try(FileWriter fileWriter=new FileWriter("src/main/resources/data/REFRESH_TOKEN.json")){
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
            setTokens(jsonObject.get("accessToken").getAsString(),jsonObject.get("refreshToken").getAsString());
            updateUser();
        }catch (Exception e){
            System.out.println("ОШИБКА:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void handleAuthButton(ActionEvent event){
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        if(login.isEmpty()||password.isEmpty())
        {
            System.out.println("Все поля должны быть заполнены!");
            return;
        }

        int status = sendAuthRequest(login,password);

        if(status==200){
            try {
                // Замена основной сцены на сцену профиля
                replaceMainScene("/fxml/profileView.fxml","Profile");
                if(currentStage==null) {
                    currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();// Получаем ссылку на текущую сцену
                }
                currentStage.close();// Закрытие окна авторизации
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }// Нажатие кнопки аутентификации
    @FXML private void handleRegButton(ActionEvent event) throws IOException {
        if(currentStage==null) {
            currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();// Получаем ссылку на текущую сцену
        }
        replaceScene("/fxml/regView.fxml","Registration",currentStage,false);
    }// Нажатие кнопки перехода на регистрацию

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
        localization=lang;
        saveLocalizationToFile(lang);
    }

    @FXML public void initialize(){
        setLocalization(getLang());
        iconImageView.setImage(iconImg);// Установка картинки для иконки

        rootVBox.getStylesheets().add(getClass().getResource(authStylePath).toExternalForm());
    }
}