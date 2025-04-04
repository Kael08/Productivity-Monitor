package productivityMonitor.controllers;

import com.sun.tools.javac.Main;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import productivityMonitor.MainApp;

import java.io.IOException;

public class ProfileController {
    @FXML
    private ImageView mainImageView;

    @FXML
    private ImageView avatarImageView;

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
    }
}
