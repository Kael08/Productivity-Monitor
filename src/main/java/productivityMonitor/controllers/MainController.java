package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import productivityMonitor.services.FocusMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.utils.SharedData.*;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.StageService.replaceMainScene;

public class MainController {
    // ImageView
    @FXML private ImageView mainImageView;// Иконка приложения
    @FXML private ImageView runImageView;// Иконка запуска мониторинга
    @FXML private ImageView settingsImageView;// Иконка настроек мониторинга
    @FXML private ImageView timerImageView;// Иконка установки таймера мониторинга

    // Image
    private final Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm()),
            settingsImg = new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()),
            timerImg = new Image(getClass().getResource("/images/clock-ico.png").toExternalForm()),
            pauseImg = new Image(getClass().getResource("/images/pause-ico.png").toExternalForm()),
            iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());

    // Label
    @FXML private Label clockLabel;// Часы
    @FXML private Label timerLabel;// Динамический таймер мониторинга
    @FXML private Label pomodoroTimerLabel;// Динамический таймер режима Pomodoro!

    // Button
    @FXML private Button profileButton;// Кнопка профиля
    @FXML private Button statisticsButton;// Кнопка статистики
    @FXML private Button settingsButton;// Кнопка настроек
    @FXML private Button achievementsButton;// Кнопка достижений
    @FXML private Button notesButton;// Кнопка заметок
    @FXML private Button plansButton;// Кнопка планов
    @FXML private Button runButton;// Кнопка запуска мониторинга
    @FXML private Button monitoringSettingsButton;// Кнопка настроек мониторинга
    @FXML private Button timerButton;// Кнопка установки таймера мониторинга

    // TextArea
    @FXML private TextArea consoleTextArea;// Консоль

    // Stage
    private Stage authStage = null;
    private Stage monitoringSettingsStage = null;
    private Stage timerStage = null;

    // Нажатие кнопок навигационной области
    @FXML
    private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor"); // Замена текущей сцены на главную сцену
    }
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        // Проверяем валидность токена и активность пользователя
        if (isAccessTokenValid() && getUser().isUserActive) {
            replaceMainScene("/fxml/profileView.fxml","Profile");// Замена текущей сцены на сцену профиля
        } else {
            // Пробуем обновить токен, если access-токен невалиден
            if (refreshAccessToken()) {
                updateUser(); // Обновляем данные пользователя
                replaceMainScene("/fxml/profileView.fxml","Profile");// Замена текущей сцены на сцену профиля
            } else {
                // Если refresh тоже не сработал - показываем окно авторизации
                if(authStage!=null||authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage=new Stage();
                createScene("/fxml/authView.fxml","Authentification",authStage,false);
            }
        }
    }
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
        replaceMainScene("/fxml/notesView.fxml","Notes"); // Замена текущей сцены на сцену заметок
    }
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml","Plans");// Замена текущей сцены на сцену планов
    }

    // Класс для взаимодействия с мониторингом
    private FocusMode focusMode;

    // Отключение и включение элементов
    public void setDisableAllButtons(boolean val){
        profileButton.setDisable(val);
        settingsButton.setDisable(val);
        statisticsButton.setDisable(val);
        achievementsButton.setDisable(val);
        notesButton.setDisable(val);
        plansButton.setDisable(val);
        monitoringSettingsButton.setDisable(val);
        timerButton.setDisable(val);
    }

    // Закрытие всех побочных окон
    private void closeSideStages(){
        if(monitoringSettingsStage!=null) {
            monitoringSettingsStage.close();
            monitoringSettingsStage = null;
        }

        if(authStage!=null) {
            authStage.close();
            authStage = null;
        }

        if(timerStage!=null) {
            timerStage.close();
            timerStage = null;
        }
    }



    // Метод для установки картинки Play для кнопки запуска
    public void setRunImageView() {
        runImageView.setImage(runImg);
    }

    // Запуск потока монитора
    @FXML
    private void handleRunButton(ActionEvent event) {
        System.out.println("Кнопка Run нажата!");

        if (!isMonitoringActive) {
            if(minutes>0){
                startMonitoringTimer(); // Включение динамического таймера
            }
            if(currentMode.equals("Pomodoro")){
                startMonitoringTimerPomodoro(); // Включение динамического таймера режима Pomodoro
            }

            setDisableAllButtons(true); // Отключение элементов
            closeSideStages();
            runImageView.setImage(pauseImg);
            focusMode.startMonitoring();
        } else {
            stopMonitoringTimer(); // Отключение динамических таймеров

            setDisableAllButtons(false); // Включение элементов
            runImageView.setImage(runImg);
            focusMode.stopMonitoring();
        }
    }

    // Таймеры для работы динамических таймеров мониторинга в главном экране
    Timer monitoringTimer;
    Timer monitoringTimerPomodoro;
    private int pomodoroTimerSeconds=0;
    private boolean workPhase=true; // Флаг для обозначения фазы работы у режима Pomodoro
    private final int WORK_PHASE_DURATION = 25*60; // 25 минут в секундах
    private final int BREAK_PHASE_DURATION = 5*60; // 5 минут в секундах

    private int timerSeconds=0;

    // Запустить таймер мониторинга
    private void startMonitoringTimer(){
        timerLabel.setVisible(true);
        timerSeconds=minutes*60;

        if (monitoringTimer == null) { // Пересоздаём, если таймер был отменён
            monitoringTimer = new Timer();
        }

        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateMonitoringTimer());
            }
        },0,1000);

    }

    private void updateMonitoringTimer(){
        if(timerSeconds>0) {
            timerSeconds--;

            // Обновляем текст с оставшимся временем в формате MM:SS
            int minutes = timerSeconds / 60;
            int seconds = timerSeconds % 60;
            String timeText = String.format("%02d:%02d", minutes, seconds);

            timerLabel.setText(timeText);
        }else{
            stopMonitoringTimer();
        }
    }

    // Остановить все таймеры мониторинга
    public void stopMonitoringTimer(){
        timerLabel.setVisible(false);
        pomodoroTimerLabel.setVisible(false);

        if (monitoringTimer != null) {
            monitoringTimer.cancel();
            monitoringTimer = null; // Обнуляем для пересоздания
        }
        if (monitoringTimerPomodoro != null) {
            monitoringTimerPomodoro.cancel();
            monitoringTimerPomodoro = null; // Обнуляем для пересоздания
        }

        pomodoroTimerSeconds=0;
        timerSeconds=0;
        timerLabel.setText("");
        pomodoroTimerLabel.setText("");
    }

    // Запустить таймер режима Pomodoro
    private void startMonitoringTimerPomodoro(){
        pomodoroTimerLabel.setVisible(true);
        workPhase=true;
        pomodoroTimerSeconds=WORK_PHASE_DURATION;

        if (monitoringTimerPomodoro == null) { // Пересоздаём, если таймер был отменён
            monitoringTimerPomodoro = new Timer();
        }

        monitoringTimerPomodoro.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateMonitoringTimerPomodoro());
            }
        },0,1000);
    }

    private  void updateMonitoringTimerPomodoro(){
        if (pomodoroTimerSeconds > 0) {
            pomodoroTimerSeconds--;

            // Обновляем текст с оставшимся временем в формате MM:SS
            int minutes = pomodoroTimerSeconds / 60;
            int seconds = pomodoroTimerSeconds % 60;
            String timeText = String.format("%02d:%02d", minutes, seconds);

            if (workPhase) {
                pomodoroTimerLabel.setText(timeText);
                pomodoroTimerLabel.setStyle("-fx-text-fill: red;"); // Красный для работы
            } else {
                pomodoroTimerLabel.setText(timeText);
                pomodoroTimerLabel.setStyle("-fx-text-fill: green;"); // Зеленый для отдыха
            }
        } else {
            // Переключаем фазы, когда время истекло
            workPhase = !workPhase;

            if (workPhase) {
                // Начинаем рабочую фазу (25 минут)
                pomodoroTimerSeconds = WORK_PHASE_DURATION;
                pomodoroTimerLabel.setStyle("-fx-text-fill: red;");
            } else {
                // Начинаем фазу отдыха (5 минут)
                pomodoroTimerSeconds = BREAK_PHASE_DURATION;
                pomodoroTimerLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }
    // Окно для настройки запуска


    @FXML
    private void handleMonitoringSettingsButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Settings нажата!");

        if(monitoringSettingsStage!=null||monitoringSettingsStage.isShowing()){
            monitoringSettingsStage.toFront();
            return;
        }
        monitoringSettingsStage=new Stage();
        createScene("/fxml/monitoringSettingsView.fxml","Process Settings",monitoringSettingsStage,false);
    }

    // Окно для настройки таймера

    @FXML
    private void handleTimerButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Timer нажата!");

        if(timerStage!=null&&timerStage.isShowing()){
            timerStage.toFront();
            return;
        }
        timerStage=new Stage();
        createScene("/fxml/timerView.fxml","Timer",monitoringSettingsStage,false);
    }


    // Функция для обновления времени
    private void updateTime(Label label) {
        Date now = new Date();
        // Форматируем время в нужный формат
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.LL.yyyy");

        String currentTime = sdf.format(now);

        label.setText(currentTime);
    }

    @FXML
    public void initialize(){
        runImageView.setImage(runImg);
        settingsImageView.setImage(settingsImg);
        timerImageView.setImage(timerImg);
        mainImageView.setImage(iconImg);

        focusMode = new FocusMode(consoleTextArea,this);
        focusMode.setFullLockdownMode();

        // Часы в главном меню
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateTime(clockLabel));
            }
        },0,1000);

        // Чтения и сохранение мотивирующих сообщений
        readMotivationMessages();

        // Чтение и сохранение текстов для Sailor's Knot
        readSailorsKnotText();

        // Обновляем access-токен и, в случае успеха, загружает данные пользователя
        if(refreshAccessToken()){
            updateUser();
        }
    }
}
