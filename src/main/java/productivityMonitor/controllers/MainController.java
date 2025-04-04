package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import productivityMonitor.FocusWebSocketServer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static productivityMonitor.utils.SharedData.minutes;
import static productivityMonitor.utils.SharedData.processList;

public class MainController {
    private Stage authStage = null;
    @FXML
    private Button profileButton;
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/authView.fxml"));
        Parent root = fxmlLoader.load();

        AuthController authController = fxmlLoader.getController();
        authController.setMainStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        authStage=new Stage();
        authController.setThisStage(authStage);
        authStage.setTitle("Authentification");
        authStage.setScene(new Scene(root));
        authStage.setResizable(false);
        authStage.show();
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


    @FXML
    private Button runButton;
    @FXML
    private ImageView runImageView; // Кнопка для запуска фокусировки

    @FXML
    private Button runSettingsButton;
    @FXML
    private ImageView settingsImageView; // Кнопка для настроек фокусировки

    @FXML
    private Button timerButton;
    @FXML
    private ImageView timerImageView; // Кнопка для настройки таймера

    @FXML
    private ImageView mainImageView;

    @FXML
    private TextArea consoleTextArea; // Консоль

    @FXML
    private Label clockLabel; // Часы


    // Иконки
    private Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm()),
            settingsImg = new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()),
            timerImg = new Image(getClass().getResource("/images/clock-ico.png").toExternalForm()),
            pauseImg = new Image(getClass().getResource("/images/pause-ico.png").toExternalForm()),
            iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());


    // Флаг для работы монитора
    boolean runFlag = false;

    // Поток для работы монитора
    private Thread runThread;

    // Заблокировать все кнопки
    private void disableAllButtons(){
        profileButton.setDisable(true);
        settingsButton.setDisable(true);
        statisticsButton.setDisable(true);
        achievementsButton.setDisable(true);
        notesButton.setDisable(true);
        plansButton.setDisable(true);
        runSettingsButton.setDisable(true);
        timerButton.setDisable(true);
    }

    // Разблокировать все кнопки
    private void enableAllButtons(){
        profileButton.setDisable(false);
        settingsButton.setDisable(false);
        statisticsButton.setDisable(false);
        achievementsButton.setDisable(false);
        notesButton.setDisable(false);
        plansButton.setDisable(false);
        runSettingsButton.setDisable(false);
        timerButton.setDisable(false);
    }

    // Запуск потока монитора
    @FXML
    private void handleRunButton(ActionEvent event) {
        System.out.println("Кнопка Run нажата!");

        runWebSocketServer();

        if (!runFlag) {
            disableAllButtons();
            runImageView.setImage(pauseImg);
            runFlag = true;
            runThread = new Thread(runMonitor);
            runThread.start();
        } else {
            enableAllButtons();
            runImageView.setImage(runImg);
            runFlag = false;
            runThread.interrupt();
            runThread=null;
        }
    }

    // Задача для закрытия процессов
    Runnable runMonitor = () -> {
        if(minutes==0) {
            consoleTextArea.appendText("Монитор запущен!\n");
            while (runFlag) {
                try {
                    closeProcess(processList);
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    consoleTextArea.appendText("Монитор остановлен!\n");
                    return;
                }
            }
        } else {
            long endTime = System.currentTimeMillis()+minutes * 60 * 1000;
            consoleTextArea.appendText("Монитор запущен с таймеров на "+minutes+" минут!\n");
            while (runFlag&&System.currentTimeMillis()<endTime){
                try{
                    closeProcess(processList);
                    Thread.sleep(2000);
                } catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    consoleTextArea.appendText("Монитор прерван!\n");
                    return;
                }
            }
            consoleTextArea.appendText("Монитор завершил работу! Время вышло!\n");
        }
        runFlag=false;
    };

    private void runWebSocketServer(){
        FocusWebSocketServer server = new FocusWebSocketServer(8081);
        server.addToBlacklist("youtube.com");
        Thread serverThread = new Thread(() -> {
            server.start();
            System.out.println("🟢 Сервер запущен");
        });
        serverThread.setDaemon(true); // Чтобы завершался при закрытии приложения
        serverThread.start();
    }

    // Окно для настройки запуска
    private Stage runSettingsStage = null;

    @FXML
    private void handleRunSettingsButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Settings нажата!");
        if(runSettingsStage!=null&&runSettingsStage.isShowing()){
            runSettingsStage.toFront();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/runSettingsView.fxml"));
        Parent root = fxmlLoader.load();

        runSettingsStage = new Stage();
        runSettingsStage.setTitle("Process Settings");
        runSettingsStage.setScene(new Scene(root,400,600));
        runSettingsStage.setMinWidth(400);
        runSettingsStage.setMinHeight(550);
        runSettingsStage.show();
    }

    // Окно для настройки таймера
    private Stage timerStage = null;

    @FXML
    private void handleTimerButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Timer нажата!");
        if(timerStage!=null&&timerStage.isShowing()){
            timerStage.toFront();
            return;
        }

        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getResource("/fxml/timerView.fxml"));
        Parent root = fxmlLoader.load();

        timerStage = new Stage();
        timerStage.setTitle("Timer");
        timerStage.setScene(new Scene(root,300,300));
        timerStage.setMinWidth(200);
        timerStage.setMinHeight(200);
        timerStage.show();
    }

    @FXML
    public void initialize(){
        runImageView.setImage(runImg);
        settingsImageView.setImage(settingsImg);
        timerImageView.setImage(timerImg);
        mainImageView.setImage(iconImg);

        // Часы в главном меню
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateTime(clockLabel));
            }
        },0,1000);
    }

    // Функция для обновления времени
    private void updateTime(Label label) {
        Date now = new Date();
        // Форматируем время в нужный формат
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.LL.yyyy");

        String currentTime = sdf.format(now);

        label.setText(currentTime);
    }

    // Закрывает процессы по имени
    private void closeProcess(List<String> list) {
        for (String pn : list) {
            try {
                ProcessBuilder builder = new ProcessBuilder("taskkill", "/IM", pn, "/F");
                Process process = builder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    consoleTextArea.appendText("Процесс " + pn + " был завершен\n");
                } else {
                    // Это также вызывается, когда процесса не было или он не найден, и из-за этого мусорится консоль
                    //consoleTextArea.appendText("Ошибка при завершении процесса " + pn + "\n");
                }
            } catch (Exception e) {
                consoleTextArea.appendText("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
            }
        }
    }



}
