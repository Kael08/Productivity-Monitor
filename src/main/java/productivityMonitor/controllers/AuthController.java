package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthController {
    private Stage thisStage;

    public void setThisStage(Stage thisStage){
        this.thisStage = thisStage;
    }

    private final HttpClient client = HttpClient.newHttpClient();

    // Функция для POST-запросов на авторизацию
    private int sendAuthRequest(String email,String password){
        /*try{
            String json = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/auth"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());
            return response.statusCode();
        } catch (Exception e){
            e.printStackTrace();
        }

        return 0;*/
        return 200;
    }

    private Stage mainStage;

    public void setMainStage(Stage mainStage){
        this.mainStage=mainStage;
    }

    @FXML
    private ImageView iconImageView;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button authButton;
    @FXML
    private void handleAuthButton(ActionEvent event){
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        if(email.isEmpty()||password.isEmpty())
        {
            System.out.println("Email и Password не должны быть пустыми!");
            return;
        }

        int status = sendAuthRequest(email,password);

        if(status==200){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profileView.fxml"));
                Parent root = loader.load();

                // Устанавливаем новую сцену в главное окно
                mainStage.setScene(new Scene(root));
                mainStage.setTitle("Profile");
                mainStage.show();
                thisStage.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private Button regButton;
    @FXML
    private void handleRegButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/regView.fxml"));
        Parent registrationRoot = fxmlLoader.load();

        RegController regController = fxmlLoader.getController();
        regController.setMainStage(mainStage);

        Stage regStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        regController.setThisStage(regStage);
        regStage.setScene(new Scene(registrationRoot));
        regStage.setTitle("Registration");
        regStage.setResizable(false);
        regStage.show();
    }

    private Image iconImage = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    public void initialize(){
        iconImageView.setImage(iconImage);
    }
}
