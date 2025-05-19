package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.controllers.TimerController.getLang;
import static productivityMonitor.services.SettingsService.profileStylePath;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.StageService.replaceMainScene;

public class ProfileController {
    // Pane
    @FXML BorderPane rootPane;

    // Label
    @FXML private Label usernameLabel;

    // Button
    @FXML private Button logoutButton;
    @FXML private Button profileButton;
    @FXML private Button settingsButton;
    @FXML private Button statisticsButton;
    @FXML private Button achievementsButton;
    @FXML private Button notesButton;
    @FXML private Button plansButton;

    // ImageView
    @FXML private ImageView mainImageView;
    @FXML private ImageView avatarImageView;

    // Image
    private Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm()),
            avatarImg = new Image(getClass().getResource("/images/avatar-ico.png").toExternalForm());

    // Нажатие кнопок навигационной области
    @FXML private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor"); // Замена текущей сцены на главную сцену
    }// Нажатие иконки приложения
    @FXML private void handleProfileButton(ActionEvent event) throws IOException {
    }// Нажатие кнопки профиля
    @FXML private void handleStatisticsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/statisticsView.fxml",bundle.getString("statistics"));
    }// Нажатие кнопки статистики
    @FXML private void handleSettingsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml","Settings");
    }// Нажатие кнопки настроек
    @FXML private void handleAchievementsButton(ActionEvent event){

    }// Нажатие кнопки достижений
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml","Notes"); // Замена текущей сцены на сцену заметок
    }// Нажатие кнопки заметок
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml","Plans");// Замена текущей сцены на сцену планов
    }// Нажатие кнопки планов

    @FXML private void handleLogoutButton(ActionEvent event) throws IOException{
        clearTokens();// Очистка токенов
        getUser().deactivateUser();// Деактивация пользователя
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor");// Замена текущей сцены на главную
    }

    private void loadUserData(){
        usernameLabel.setText(getUser().getUsername());
    }

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("profile"));
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        achievementsButton.setText(bundle.getString("achievements"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
        logoutButton.setText(bundle.getString("profile.logout"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    public void initialize(){
        setLocalization(getLang());

        mainImageView.setImage(iconImg);
        avatarImageView.setImage(avatarImg);

        profileButton.setDisable(true);

        loadUserData();

        rootPane.getStylesheets().add(getClass().getResource(profileStylePath).toExternalForm());
    }
}
