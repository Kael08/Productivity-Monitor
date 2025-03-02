package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import productivityMonitor.utils.SharedData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

import static productivityMonitor.utils.SharedData.processList;

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
    private TextArea consoleTextArea; // Консоль

    @FXML
    private Label clockLabel; // Часы

    // Флаг для работы монитора
    boolean runFlag = false;

    // Поток для работы монитора
    private Thread runThread;

    @FXML
    private void handleRunButton(ActionEvent event) {
        System.out.println("Кнопка Run нажата!");

        if (!runFlag) {
            consoleTextArea.appendText("Монитор запущен!\n");
            runFlag = true;
            runThread = new Thread(runMonitor);
            runThread.start();
        } else {
            consoleTextArea.appendText("Монитор остановлен!\n");
            runFlag = false;
            if (runThread != null) {
                runThread.interrupt(); // Прерываем поток корректно
                runThread = null;
            }
        }
    }

    private Stage runSettingsStage = null;

    @FXML
    private void handleSettingsButton(ActionEvent event) throws IOException {
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
        Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm()),
                settingsImg = new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()),
                timerImg = new Image(getClass().getResource("/images/clock-ico.png").toExternalForm());

        runImageView.setImage(runImg);
        settingsImageView.setImage(settingsImg);
        timerImageView.setImage(timerImg);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateTime(clockLabel));
            }
        },0,1000);
    }

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

    // Задача для закрытия процессов
    Runnable runMonitor = () -> {
        while(runFlag){
            try {
                closeProcess(processList);
                Thread.currentThread().sleep(2000);
                System.out.println("Ещё один цикл");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Устанавливаем флаг прерывания
                System.out.println("Мониторинг был прерван.");
                return; // Прерываем выполнение потока
            }
        }
    };
}