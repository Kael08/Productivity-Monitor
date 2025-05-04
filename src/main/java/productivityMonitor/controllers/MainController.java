package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import productivityMonitor.services.MonitoringManager;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.TimerUtils;
import java.io.IOException;

import static productivityMonitor.services.StageService.*;
import static productivityMonitor.utils.SharedData.*;
import static productivityMonitor.services.TokenManager.*;
import static productivityMonitor.models.User.getUser;

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
    @FXML private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor"); // Замена текущей сцены на главную сцену
    }// Нажатие иконки приложения
    @FXML private void handleProfileButton(ActionEvent event) throws IOException {
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
                if(authStage!=null&&authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage=new Stage();
                createScene("/fxml/authView.fxml","Authentification",authStage,false);
            }
        }
    }// Нажатие кнопки профиля
    @FXML private void handleStatisticsButton(ActionEvent action){

    }// Нажатие кнопки статистики
    @FXML private void handleSettingsButton(ActionEvent action){

    }// Нажатие кнопки настроек
    @FXML private void handleAchievementsButton(ActionEvent event){

    }// Нажатие кнопки достижений
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml","Notes"); // Замена текущей сцены на сцену заметок
    }// Нажатие кнопки заметок
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml","Plans");// Замена текущей сцены на сцену планов
    }// Нажатие кнопки планов
    @FXML private void handleMonitoringSettingsButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Settings нажата!");

        if(monitoringSettingsStage!=null&&monitoringSettingsStage.isShowing()){
            monitoringSettingsStage.toFront();
            return;
        }

        monitoringSettingsStage=new Stage();
        MonitoringSettingsController controller=createSceneAndGetController("/fxml/monitoringSettingsView.fxml","Process Settings",monitoringSettingsStage,false);
        controller.setFocusMode(monitoringManager);
    }// Нажатие кнопки настройки мониторинга
    @FXML private void handleTimerButton(ActionEvent event) throws IOException {
        System.out.println("Кнопка Timer нажата!");

        if(timerStage!=null&&timerStage.isShowing()){
            timerStage.toFront();
            return;
        }
        timerStage=new Stage();
        createScene("/fxml/timerView.fxml","Timer",timerStage,false);
    }// Нажатие кнопки настройки таймера
    @FXML private void handleRunButton(ActionEvent event) {
        System.out.println("Кнопка Run нажата!");

        if (!isMonitoringActive) {
            timerUtils.activateMonitoringTimer();// Запуск мониторинг-таймера

            setDisableAllButtons(true); // Отключение элементов
            closeSideStages();
            runImageView.setImage(pauseImg);
            monitoringManager.startMonitoring();
        } else {
            timerUtils.deactivateMonitoringTimer(); // остановка мониторинг-таймера

            setDisableAllButtons(false); // Включение элементов
            runImageView.setImage(runImg);
            monitoringManager.stopMonitoring();
        }
    }// Нажатие кнопки запуска мониторинга

    // Класс для взаимодействия с мониторингом
    private MonitoringManager monitoringManager;
    // Логгер для записи текста в основную консоль
    ConsoleLogger logger;
    // Класс для работы с таймерами
    public TimerUtils timerUtils=new TimerUtils();
    // Отключение и включение элементов(кроме кнопки запуска мониторинга)
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

    @FXML public void initialize(){
        runImageView.setImage(runImg);
        settingsImageView.setImage(settingsImg);
        timerImageView.setImage(timerImg);
        mainImageView.setImage(iconImg);

        logger=new ConsoleLogger(consoleTextArea);
        monitoringManager = new MonitoringManager(logger,this);
        monitoringManager.setMode("FullLockdown");

        timerUtils.setTimerLabel(timerLabel);
        timerUtils.setPomodoroTimerLabel(pomodoroTimerLabel);
        timerUtils.setClockLabel(clockLabel);

        // Часы в главном меню
        timerUtils.activateClock();
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