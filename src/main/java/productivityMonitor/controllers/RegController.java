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

public class RegController {
    private Stage mainStage;

    public void setMainStage(Stage mainStage){
        this.mainStage=mainStage;
    }

    private Stage thisStage;

    public void setThisStage(Stage thisStage){
        this.thisStage=thisStage;
    }

    private final HttpClient client = HttpClient.newHttpClient();

    // Функция для POST-запросов на авторизацию
    private int sendRegRequest(String email,String password, String username){
        try{
            String json = String.format("{\"email\": \"%s\", \"password\": \"%s\", \"username\": \"%s\"}", email, password,username);

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

    @FXML
    private ImageView iconImageView;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Button authButton;
    @FXML
    private void handleAuthButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authView.fxml"));
        Parent authentificationRoot = fxmlLoader.load();

        AuthController authController = fxmlLoader.getController();
        authController.setMainStage(mainStage);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(authentificationRoot));
        authController.setThisStage(stage);
        stage.setTitle("Authentification");
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    private Button regButton;
    @FXML
    private void handleRegButton(ActionEvent event){
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String username = usernameTextField.getText();

        if(email.isEmpty()||password.isEmpty())
        {
            System.out.println("Email и Password не должны быть пустыми!");
            return;
        }

        int status = sendRegRequest(email,password,username);

        if(status==201){
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

    private Image iconImage = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    public void initialize(){
        iconImageView.setImage(iconImage);
    }
}
