package productivityMonitor.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.tools.javac.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import productivityMonitor.MainApp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static productivityMonitor.utils.SharedData.ACCESS_TOKEN;
import static productivityMonitor.utils.SharedData.isUserLogged;
import static productivityMonitor.utils.SharedData.username;

public class ProfileController {
    private final HttpClient client = HttpClient.newHttpClient();

    @FXML
    private ImageView mainImageView;

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button profileButton;
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/fxml/mainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/images/icon.png")));
        stage.setTitle("Productivity Monitor");
        stage.setMinWidth(850);
        stage.setMinHeight(500);

        stage.setOnCloseRequest(e-> {
            Platform.exit();
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }

    private void getUser(){
        try{
            System.out.println(ACCESS_TOKEN);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/users/me"))
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: "+response.statusCode());
            System.out.println("Response body: "+response.body());

            if(response.statusCode()==200) {
                isUserLogged=true;
                saveUsername(response.body());
            }
        }catch (Exception e){
            System.out.println("ОШИБКА:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveUsername(String responseBody){
        try{
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseBody,JsonObject.class);

            String usernameJson=jsonObject.get("username").getAsString();

            username=usernameJson.substring(1,usernameJson.length()-1);
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private Button settingsButton;
    @FXML
    private void handleSettingsButton(ActionEvent action){

    }

    @FXML
    private Button statisticsButton;
    @FXML
    private void handleStatisticsButton(ActionEvent action){

    }

    @FXML
    private Button achievementsButton;
    @FXML
    private void handleAchievementsButton(ActionEvent event){

    }

    @FXML
    private Button notesButton;
    @FXML
    private void handleNotesButton(ActionEvent action){

    }

    @FXML
    private Button plansButton;
    @FXML
    private void handlePlansButton(ActionEvent action){

    }

    // Иконки
    private Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm()),
                  avatarImg = new Image(getClass().getResource("/images/avatar-ico.png").toExternalForm());

    public void initialize(){
        mainImageView.setImage(iconImg);
        avatarImageView.setImage(avatarImg);

        getUser();
        if(isUserLogged){
            usernameLabel.setText(username);
        }
    }
}
