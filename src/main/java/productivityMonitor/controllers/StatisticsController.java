package productivityMonitor.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import productivityMonitor.models.DailyStats;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import static productivityMonitor.controllers.SettingsController.getLang;
import static productivityMonitor.models.User.getUser;
import static productivityMonitor.services.SettingsService.iconImg;
import static productivityMonitor.services.SettingsService.statisticsStylePath;
import static productivityMonitor.services.StageService.createScene;
import static productivityMonitor.services.StageService.replaceMainScene;
import static productivityMonitor.services.TokenManager.*;

public class StatisticsController {
    // Pane
    @FXML private BorderPane rootPane;

    // ImageView
    @FXML private ImageView mainImageView;// Иконка приложения

    // Button
    @FXML private Button profileButton;// Кнопка профиля
    @FXML private Button statisticsButton;// Кнопка статистики
    @FXML private Button settingsButton;// Кнопка настроек
    @FXML private Button notesButton;// Кнопка заметок
    @FXML private Button plansButton;// Кнопка планов

    // Axis
    @FXML private CategoryAxis monitoringDateLabel;
    @FXML private NumberAxis monitoringMinutesLabel;
    @FXML private CategoryAxis processDateLabel;
    @FXML private NumberAxis processCountLabel;
    @FXML private CategoryAxis domainsDateLabel;
    @FXML private NumberAxis domainsCountLabel;

    // Нажатие кнопок навигационной области
    @FXML private void handleMainImageClick(MouseEvent event) throws IOException {
        replaceMainScene("/fxml/mainView.fxml","Productivity Monitor"); // Замена текущей сцены на главную сцену
    }// Нажатие иконки приложения
    @FXML private void handleProfileButton(ActionEvent event) throws IOException {
        // Проверяем валидность токена и активность пользователя
        if (isAccessTokenValid() && getUser().isUserActive) {
            replaceMainScene("/fxml/profileView.fxml",bundle.getString("profile"));// Замена текущей сцены на сцену профиля
        } else {
            // Пробуем обновить токен, если access-токен невалиден
            if (refreshAccessToken()) {
                updateUser(); // Обновляем данные пользователя
                replaceMainScene("/fxml/profileView.fxml",bundle.getString("profile"));// Замена текущей сцены на сцену профиля
            } else {
                // Если refresh тоже не сработал - показываем окно авторизации
                if(authStage!=null&&authStage.isShowing()) {
                    authStage.toFront();
                    return;
                }
                authStage=new Stage();
                createScene("/fxml/authView.fxml",bundle.getString("auth.title"),authStage,false);
            }
        }
    }// Нажатие кнопки профиля
    @FXML private void handleStatisticsButton(ActionEvent action){
        // Кнопка статистики заблокирована
    }// Нажатие кнопки статистики
    @FXML private void handleSettingsButton(ActionEvent action) throws IOException {
        replaceMainScene("/fxml/settingsView.fxml",bundle.getString("settings"));
    }// Нажатие кнопки настроек
    @FXML private void handleNotesButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/notesView.fxml",bundle.getString("notes")); // Замена текущей сцены на сцену заметок
    }// Нажатие кнопки заметок
    @FXML private void handlePlansButton(ActionEvent event) throws IOException {
        replaceMainScene("/fxml/plansView.fxml",bundle.getString("plans"));// Замена текущей сцены на сцену планов
    }// Нажатие кнопки планов

    // Label
    @FXML private Label monitoringLabel;
    @FXML private Label processesLabel;
    @FXML private Label domainsLabel;

    // LineChart
    @FXML LineChart<String,Number> monitoringChart;

    // BarChart
    @FXML BarChart<String,Number> processesChart;
    @FXML BarChart<String,Number> domainsChart;

    // Stage
    private Stage authStage = null;

    // ResourceBundle для локализации
    private ResourceBundle bundle;

    // Применение локализации
    private void applyLocalization() {
        profileButton.setText(bundle.getString("profile"));
        statisticsButton.setText(bundle.getString("statistics"));
        settingsButton.setText(bundle.getString("settings"));
        notesButton.setText(bundle.getString("notes"));
        plansButton.setText(bundle.getString("plans"));
        monitoringLabel.setText(bundle.getString("statistics.monitoring"));
        processesLabel.setText(bundle.getString("statistics.processes"));
        domainsLabel.setText(bundle.getString("statistics.domains"));
        monitoringDateLabel.setLabel(bundle.getString("statistics.date"));
        monitoringMinutesLabel.setLabel(bundle.getString("statistics.minutes"));
        processDateLabel.setLabel(bundle.getString("statistics.date"));
        processCountLabel.setLabel(bundle.getString("statistics.count"));
        domainsDateLabel.setLabel(bundle.getString("statistics.date"));
        domainsCountLabel.setLabel(bundle.getString("statistics.count"));

    }

    // Установка локализации
    private void setLocalization(String lang) {
        Locale locale = new Locale(lang);
        bundle = ResourceBundle.getBundle("lang.messages", locale);
        applyLocalization();
    }

    // Обновление всех графиков
    private void updateStatistics() {
        // Очищаем старые данные из графиков
        monitoringChart.getData().clear();
        processesChart.getData().clear();
        domainsChart.getData().clear();

        // Создаем серии данных для каждого графика
        XYChart.Series<String, Number> monitoringSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> processesSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> domainsSeries = new XYChart.Series<>();

        // Устанавливаем названия серий (опционально, для легенды)
        monitoringSeries.setName("Monitoring Time");
        processesSeries.setName("Blocked Processes");
        domainsSeries.setName("Blocked Domains");

        // Заполняем серии данными из списка DailyStats
        for (DailyStats stats :dailyStatsList) {
            String date = stats.getDate();
            double monitoringMinutes = stats.getMonitoringTimeInMinutes();
            int blockedProcesses = stats.getBlockedProcesses();
            int blockedDomains = stats.getBlockedDomains();

            // Добавляем данные в серии
            monitoringSeries.getData().add(new XYChart.Data<>(date, monitoringMinutes));
            processesSeries.getData().add(new XYChart.Data<>(date, blockedProcesses));
            domainsSeries.getData().add(new XYChart.Data<>(date, blockedDomains));
        }

        // Привязываем серии к соответствующим графикам
        monitoringChart.getData().add(monitoringSeries);
        processesChart.getData().add(processesSeries);
        domainsChart.getData().add(domainsSeries);
    }

    @FXML public void initialize(){
        // Установка языка
        setLocalization(getLang());

        mainImageView.setImage(iconImg);

        // Загрузка и обновление статистики
        if(refreshAccessToken()) {
            loadDailyStatistics();
            updateStatistics();
        }

        rootPane.getStylesheets().add(getClass().getResource(statisticsStylePath).toExternalForm());
    }
}
