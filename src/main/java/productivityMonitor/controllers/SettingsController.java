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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.application.MainApp.MainStage;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.SettingsService.*;
import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.services.StageService.replaceMainScene;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.utils.DataLoader.saveLocalizationToFile;

public class SettingsController {
    // Pane
    @FXML private BorderPane rootPane;

    // Button
    @FXML private Button profileButton;
    @FXML private Button statisticsButton;
    @FXML private Button settingsButton;
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
    private final Image iconImg = new Image(getClass().getResource("/images/purple/icon.png").toExternalForm());

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Нажатие кнопок навигационной области
    @FXML private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml", "Productivity Monitor");
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
    @FXML private void handleStatisticsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/statisticsView.fxml",bundle.getString("statistics"));
    }// Нажатие кнопки статистики
    @FXML private void handleSettingsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml", bundle.getString("settings"));
    }
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml", bundle.getString("notes"));
    }
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml", bundle.getString("plans"));
    }

    // Применение локализации
    private void applyLocalization() {
        MainStage.setTitle(bundle.getString("settings"));
        selectLangLabel.setText(bundle.getString("select.language"));
        selectColorLabel.setText(bundle.getString("select.color"));
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
        localization=lang;
        saveLocalizationToFile(lang);
    }

    public static String getLang(){
        return localization;
    }

    @FXML
    public void initialize() {
        // Запомни!!! Что в ComboBox - цвет с большой буквы, а в файле он с маленькой!!!

        // Инициализация локализации (по умолчанию английский)
        setLocalization(localization);

        // Установка цвета интерфейса
        setUIColor(UIColor);

        mainImageView.setImage(iconImg);
        settingsButton.setDisable(true);

        // Инициализация ComboBox для цветов
        colorComboBox.setItems(colorList);

        colorComboBox.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Rectangle colorRect = new Rectangle(30, 20);
                    colorRect.setStroke(Color.BLACK);
                    colorRect.setStrokeWidth(1);
                    switch (item.toLowerCase()) {
                        case "purple":
                            colorRect.setFill(Color.PURPLE);
                            break;
                        case "green":
                            colorRect.setFill(Color.GREEN);
                            break;
                        case "black":
                            colorRect.setFill(Color.BLACK);
                            break;
                        case "red":
                            colorRect.setFill(Color.RED);
                            break;
                        case "blue":
                            colorRect.setFill(Color.BLUE);
                            break;
                        case "white":
                            colorRect.setFill(Color.WHITE);
                            break;
                        default:
                            colorRect.setFill(Color.GRAY);
                    }
                    HBox hbox = new HBox(colorRect);
                    hbox.setPrefWidth(150);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        colorComboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Rectangle colorRect = new Rectangle(30, 20);
                    colorRect.setStroke(Color.BLACK);
                    colorRect.setStrokeWidth(1);
                    switch (item.toLowerCase()) {
                        case "purple":
                            colorRect.setFill(Color.PURPLE);
                            break;
                        case "green":
                            colorRect.setFill(Color.GREEN);
                            break;
                        case "black":
                            colorRect.setFill(Color.BLACK);
                            break;
                        case "red":
                            colorRect.setFill(Color.RED);
                            break;
                        case "blue":
                            colorRect.setFill(Color.BLUE);
                            break;
                        case "white":
                            colorRect.setFill(Color.WHITE);
                            break;
                        default:
                            colorRect.setFill(Color.GRAY);
                    }
                    HBox hbox = new HBox(colorRect);
                    hbox.setPrefWidth(150);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });

        switch (UIColor){
            case "purple"->colorComboBox.setValue("Purple");
            case "green"->colorComboBox.setValue("Green");
            case "black"->colorComboBox.setValue("Black");
            case "red"->colorComboBox.setValue("Red");
            case "blue"->colorComboBox.setValue("Blue");
            case "white"->colorComboBox.setValue("White");
        }

        // Инициализация ComboBox для языков
        langComboBox.setItems(langList);
        switch (localization) {
            case "en" -> langComboBox.setValue("English");
            case "ru" -> langComboBox.setValue("Русский");
        }

        colorComboBox.setOnAction(e -> {
            String selectedColor = colorComboBox.getValue();
            switch (selectedColor) {
                case "Purple":
                    setUIColor("purple");
                    break;
                case "Green":
                    setUIColor("green");
                    break;
                case "Black":
                    setUIColor("black");
                    break;
                case "Red":
                    setUIColor("red");
                    break;
                case "Blue":
                    setUIColor("blue");
                    break;
                case "White":
                    setUIColor("white");
                    break;
            }
            // Очистка старых стилей и добавление нового
            rootPane.getStylesheets().clear();
            rootPane.getStylesheets().add(getClass().getResource(settingsStylePath).toExternalForm());
        });

        // Обработчик смены языка
        langComboBox.setOnAction(e -> {
            String selectedLang = langComboBox.getValue();
            switch (selectedLang) {
                case "English" -> setLocalization("en");
                case "Русский" -> setLocalization("ru");
            }
        });

        rootPane.getStylesheets().add(getClass().getResource(settingsStylePath).toExternalForm());
    }
}