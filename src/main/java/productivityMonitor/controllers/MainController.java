package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import productivityMonitor.FocusMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static productivityMonitor.FocusMode.*;
import static productivityMonitor.utils.SharedData.*;
import static productivityMonitor.utils.TokenManager.*;
import static productivityMonitor.utils.User.getUser;

public class MainController {

    private Stage mainStage;

    public MainController(){}

    public void setMainStage(Stage mainStage){
        this.mainStage=mainStage;
    }

    // Профиль
    private Stage authStage = null;
    @FXML
    private Button profileButton;
    @FXML
    private void handleProfileButton(ActionEvent event) throws IOException {
        // Проверяем валидность токена и активность пользователя
        if (isAccessTokenValid() && getUser().isUserActive) {
            loadProfileStage(event);
        } else {
            // Пробуем обновить токен, если access-токен невалиден
            if (refreshAccessToken()) {
                updateUser(); // Обновляем данные пользователя
                loadProfileStage(event);
            } else {
                // Если refresh тоже не сработал - показываем окно авторизации
                loadAuthStage(event);
            }
        }
    }

    private void loadAuthStage(ActionEvent event) throws IOException {
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

    private void loadProfileStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/profileView.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // <-- вот ключ
        stage.setScene(new Scene(root));
        stage.setTitle("Profile");
        stage.show();
    }

    // Настройки
    @FXML
    private Button settingsButton;
    @FXML
    private void handleSettingsButton(ActionEvent action){

    }

    // Статистика
    @FXML
    private Button statisticsButton;
    @FXML
    private void handleStatisticsButton(ActionEvent action){

    }

    // Достижения
    @FXML
    private Button achievementsButton;
    @FXML
    private void handleAchievementsButton(ActionEvent event){

    }

    // Заметки
    @FXML
    private Button notesButton;
    @FXML
    private void handleNotesButton(ActionEvent action) throws IOException {
        loadNotesStage(action);
    }

    private void loadNotesStage(ActionEvent event) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/notesView.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Notes");
        stage.show();
    }

    // Планы
    @FXML
    private Button plansButton;

    private void loadPlansStage(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/plansView.fxml"));
        Parent root = fxmlLoader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow(); // <-- вот ключ
        stage.setScene(new Scene(root));
        stage.setTitle("Plans");
        stage.show();
    }

    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        loadPlansStage(event);
    }

    // Иконка приложения
    @FXML
    private ImageView mainImageView;

    // Основная консоль
    @FXML
    private TextArea consoleTextArea;

    // Часы
    @FXML
    private Label clockLabel;

    // Таймеры мониторинга
    @FXML
    private Label timerLabel;
    @FXML
    private Label pomodoroTimerLabel;


    // Иконки
    private final Image runImg = new Image(getClass().getResource("/images/run-ico.png").toExternalForm()),
            settingsImg = new Image(getClass().getResource("/images/settings-ico.png").toExternalForm()),
            timerImg = new Image(getClass().getResource("/images/clock-ico.png").toExternalForm()),
            pauseImg = new Image(getClass().getResource("/images/pause-ico.png").toExternalForm()),
            iconImg = new Image(getClass().getResource("/images/icon.png").toExternalForm());


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

    // Запуск мониторинга
    @FXML
    private Button runButton;
    @FXML
    private ImageView runImageView;
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

    public static int maxAlertWindow = 5;
    public static int countAlertWindow = 0;

    private Stage mindfulnessStage = null;

    // Создание окна-предупреждения для режима Mindfulness
    public void createMindfulnessWindow() throws IOException{
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/mindfulnessWindowView.fxml"));

            Parent root = fxmlLoader.load();

            MindfulnessWindowController mindfulnessWindowController=fxmlLoader.getController();

            mindfulnessStage=new Stage();

            mindfulnessWindowController.setThisStage(mindfulnessStage);

            mindfulnessStage.setTitle("Warning!");
            mindfulnessStage.setScene(new Scene(root));
            mindfulnessStage.initOwner(mainStage);
            mindfulnessStage.initModality(Modality.WINDOW_MODAL);
            mindfulnessStage.setMinHeight(200);
            mindfulnessStage.setMinWidth(400);

            mindfulnessStage.setOnHidden(event->{
                countAlertWindow++;
                isPaused=false;
            });

            mindfulnessStage.show();
        }catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть окно задачи");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private Stage sailorsKnotStage = null;
    // Создает окно с задачей для Sailor's Knot
    public void createSailorsKnotWindow() throws IOException {
        try {
            // Загружаем FXML
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/sailorsKnotWindowView.fxml"));

            Parent root = fxmlLoader.load();

            SailorsKnotWindowController sailorsKnotWindowController = fxmlLoader.getController();

            sailorsKnotStage = new Stage();

            sailorsKnotWindowController.setThisStage(sailorsKnotStage);

            // Настраиваем Stage
            sailorsKnotStage.setTitle("Sailor's Knot Task");
            sailorsKnotStage.setScene(new Scene(root));
            sailorsKnotStage.initOwner(mainStage);
            sailorsKnotStage.initModality(Modality.WINDOW_MODAL);
            sailorsKnotStage.setMinWidth(300);
            sailorsKnotStage.setMinHeight(400);

            sailorsKnotStage.setOnHidden(event -> {
                isTaskRunning = false;
            });

            sailorsKnotStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть окно задачи");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private Stage delayGratificationStage=null;
    // Создаёт окно с таймером, по истичении которого заблокированные приложения станут доступны
    public void createDelayGratificationWindow() throws IOException{
        try{
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/delayGratificationWindowView.fxml"));

            Parent root =fxmlLoader.load();

            DelayGratificationWindowController delayGratificationWindowController=fxmlLoader.getController();

            delayGratificationStage=new Stage();

            delayGratificationWindowController.setThisStage(delayGratificationStage);

            delayGratificationStage.setTitle("Delay Timer");
            delayGratificationStage.setScene(new Scene(root));
            delayGratificationStage.initOwner(mainStage);
            delayGratificationStage.initModality(Modality.WINDOW_MODAL);
            delayGratificationStage.setMinHeight(200);
            delayGratificationStage.setMinWidth(420);

            delayGratificationStage.setOnHidden(event->{
                isDelayRunning=false;
            });

            delayGratificationStage.show();
        }catch (IOException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть окно задачи");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    // Конфигурация мониторинга
    @FXML
    private Button monitoringSettingsButton;
    @FXML
    private ImageView settingsImageView;

    // Окно для настройки запуска
    private Stage monitoringSettingsStage = null;

    @FXML
    private void handleMonitoringSettingsButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Settings нажата!");

        if(monitoringSettingsStage!=null&&monitoringSettingsStage.isShowing()){
            monitoringSettingsStage.toFront();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/monitoringSettingsView.fxml"));
        Parent root = fxmlLoader.load();

        MonitoringSettingsController monitoringSettingsController = fxmlLoader.getController();
        monitoringSettingsController.setFocusMode(focusMode);

        monitoringSettingsStage = new Stage();
        monitoringSettingsStage.setTitle("Process Settings");
        monitoringSettingsStage.setScene(new Scene(root,400,600));
        monitoringSettingsStage.setMinWidth(280);
        monitoringSettingsStage.setMinHeight(320);
        monitoringSettingsStage.show();
    }

    // Таймер мониторинга
    @FXML
    private Button timerButton;
    @FXML
    private ImageView timerImageView;

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
