package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.SettingsService.colorList;
import static productivityMonitor.services.SettingsService.langList;
import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.services.StageService.replaceMainScene;
import static productivityMonitor.services.TokenManager.*;

public class SettingsController {
    // Button
    @FXML private Button profileButton;
    @FXML private Button statisticsButton;
    @FXML private Button settingsButton;
    @FXML private Button achievementsButton;
    @FXML private Button notesButton;
    @FXML private Button plansButton;

    // ImageView
    @FXML private ImageView mainImageView;

    // Label
    @FXML private Label selectLangLabel;
    @FXML private Label selectColorLabel;

    // ComboBox
    @FXML private ComboBox<String> langComboBox;
    @FXML private ComboBox<String> colorComboBox;

    // Stage
    private Stage authStage = null;

    // Image
    private final Image iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Текущий выбранный цвет
    public static String currentColor = "Purple";

    // Нажатие кнопок навигационной области
    @FXML private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml", bundle.getString("app.title"));
    }
    @FXML private void handleProfileButton(ActionEvent event) throws IOException {
        if (isAccessTokenValid() && getUser().isUserActive) {
            replaceMainScene("/fxml/profileView.fxml", bundle.getString("profile"));
        } else {
            if (refreshAccessToken()) {
                updateUser();
                replaceMainScene("/fxml/profileView.fxml", bundle.getString("profile"));
            } else {
                if (authStage != null && authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage = new Stage();
                createScene("/fxml/authView.fxml", bundle.getString("auth.title"), authStage, false);
            }
        }
    }
    @FXML private void handleStatisticsButton(ActionEvent action) {
        // Реализуйте, если нужно
    }
    @FXML private void handleSettingsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml", bundle.getString("settings"));
    }
    @FXML private void handleAchievementsButton(ActionEvent event) {
        // Реализуйте, если нужно
    }
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml", bundle.getString("notes"));
    }
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml", bundle.getString("plans"));
    }

    // Применение локализации
    private void applyLocalization() {
        selectLangLabel.setText(bundle.getString("select.language"));
        selectColorLabel.setText(bundle.getString("select.color"));
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        achievementsButton.setText(bundle.getString("achievements"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    @FXML
    public void initialize() {
        // Инициализация локализации (по умолчанию английский)
        setLocalization("en");
        mainImageView.setImage(iconImg);
        settingsButton.setDisable(true);

        // Инициализация Combo visantBox для языков
        langComboBox.setItems(langList);
        langComboBox.setValue("English");

        // Инициализация ComboBox для цветов
        colorComboBox.setItems(colorList);
        colorComboBox.setValue(currentColor);

        // Обработчик смены языка
        langComboBox.setOnAction(e -> {
            String selectedLang = langComboBox.getValue();
            switch (selectedLang) {
                case "English" -> setLocalization("en");
                case "Русский" -> setLocalization("ru");
            }
        });
    }
}