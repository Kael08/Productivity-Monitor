package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MainController {
    @FXML
    private Button runButton;
    @FXML
    private ImageView runImageView; // Кнопка для запуска фокусировки

    @FXML
    private Button settingsButton;
    @FXML
    private ImageView settingsImageView; // Кнопка для настроек фокусировки

    @FXML
    private Button timerButton;
    @FXML
    private ImageView timerImageView; // Кнопка для настройки таймера

    @FXML
    private Button addButton;
    @FXML
    private ImageView addImageView; // Кнопка для добавления нового процесса

    @FXML
    private Button deleteButton;
    @FXML
    private ImageView deleteImageView; // Кнопка для удаления процесса

    @FXML
    private TextArea consoleTextArea; // Консоль

    @FXML
    private void handleRunButton(ActionEvent event) {
        System.out.println("Кнопка Run нажата!");
        consoleTextArea.appendText("RUN"+"\n");

    }

    @FXML
    private void handleSettingsButton(ActionEvent event) {
        System.out.println("Кнопка Settings нажата!");
        consoleTextArea.appendText("SETTINGS"+"\n");
    }

    @FXML
    private void handleTimerButton(ActionEvent event) {
        System.out.println("Кнопка Timer нажата!");
        consoleTextArea.appendText("TIMER"+"\n");
    }

    @FXML
    private void handleDeleteButton(ActionEvent event) {
        System.out.println("Кнопка Delete нажата!");
        consoleTextArea.appendText("DELETE"+"\n");
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        System.out.println("Кнопка Add нажата!");
        consoleTextArea.appendText("ADD"+"\n");
    }

    @FXML
    public void initialize(){
        Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm()),
                settingsImg = new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()),
                timerImg = new Image(getClass().getResource("/images/clock-ico.png").toExternalForm()),
                addImg = new Image(getClass().getResource("/images/plus-ico.png").toExternalForm()),
                deleteImg = new Image(getClass().getResource("/images/minus-ico.png").toExternalForm());

        runImageView.setImage(runImg);
        settingsImageView.setImage(settingsImg);
        timerImageView.setImage(timerImg);
        addImageView.setImage(addImg);
        deleteImageView.setImage(deleteImg);

    }


}