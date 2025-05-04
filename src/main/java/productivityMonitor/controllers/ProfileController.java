package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.StageService.replaceMainScene;

public class ProfileController {
    //private final HttpClient client = HttpClient.newHttpClient();

    @FXML
    private ImageView mainImageView;

    // Нажатие кнопок навигационной области
    @FXML
    private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor"); // Замена текущей сцены на главную сцену
    }
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {

    }// Кнопка заблокирована
    @FXML
    private void handleStatisticsButton(ActionEvent action){

    }
    @FXML
    private void handleSettingsButton(ActionEvent action){

    }
    @FXML
    private void handleAchievementsButton(ActionEvent event){

    }
    @FXML
    private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml","Notes");// Замена текущей сцены на сцену заметок
    }
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml","Plans");// Замена текущей сцены на сцену планов
    }

    @FXML
    private ImageView avatarImageView;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button logoutButton;
    @FXML
    private void handleLogoutButton(ActionEvent event) throws IOException{
        clearTokens();// Очистка токенов
        getUser().deactivateUser();// Деактивация пользователя
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor");// Замена текущей сцены на главную
    }


    @FXML
    private Button profileButton;

    private void loadUserData(){
        usernameLabel.setText(getUser().getUsername());
    }

    @FXML
    private Button settingsButton;


    @FXML
    private Button statisticsButton;


    @FXML
    private Button achievementsButton;


    // Заметки
    @FXML
    private Button notesButton;


    @FXML
    private Button plansButton;



    // Иконки
    private Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm()),
                  avatarImg = new Image(getClass().getResource("/images/avatar-ico.png").toExternalForm());

    public void initialize(){
        mainImageView.setImage(iconImg);
        avatarImageView.setImage(avatarImg);

        profileButton.setDisable(true);

        loadUserData();
    }
}
