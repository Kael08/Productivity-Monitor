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

import static productivityMonitor.utils.SharedData.username;
import static productivityMonitor.utils.User.getUser;

public class ProfileController {
    //private final HttpClient client = HttpClient.newHttpClient();

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

    private void loadUserData(){
        usernameLabel.setText(getUser().getUsername());
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

        loadUserData();
    }
}
