package productivityMonitor.utils;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static productivityMonitor.utils.SharedData.currentMode;

public class TimerUtils {
    public static int minutes;// Время работы таймера мониторинга

    private Label timerLabel;// Таймер
    private Label pomodoroTimerLabel;// Таймер Pomodoro
    private Label clockLabel;// Часы

    public void setTimerLabel(Label timerLabel){
        this.timerLabel=timerLabel;
    }
    public void setPomodoroTimerLabel(Label pomodoroTimerLabel){
        this.pomodoroTimerLabel=pomodoroTimerLabel;
    }
    public void setClockLabel(Label clockLabel){
        this.clockLabel=clockLabel;
    }

    private Timer monitoringTimer;// Таймер мониторинга
    private Timer monitoringTimerPomodoro;// Таймер режима Pomodoro

    private int pomodoroTimerSeconds=0;// Секунды Pomodoro
    private int timerSeconds=0;// Секунды таймера

    private final int WORK_PHASE_DURATION = 25*60; // Длительной рабочей фазы(25 минут в секундах)
    private final int BREAK_PHASE_DURATION = 5*60; // Длительность фазы отдыха(5 минут в секундах)

    private boolean workPhase=true; // Флаг для обозначения фазы Pomodoro

    // Запуск таймера мониторинга
    public void activateMonitoringTimer(){
        if(minutes!=0) {
            timerLabel.setVisible(true);
            timerSeconds = minutes * 60;// Переводим в секунды

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

        // Активация таймера pomodoro
        if(currentMode.equals("Pomodoro")){
            pomodoroTimerLabel.setVisible(true);
            workPhase=true;// Рабочая фаза
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
    }
    // Остановка таймера мониторинга
    public void deactivateMonitoringTimer(){
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
    // Обновление времени мониторинг-таймера
    private void updateMonitoringTimer(){
        if(timerSeconds>0) {
            timerSeconds--;

            // Обновляем текст с оставшимся временем в формате MM:SS
            int minutes = timerSeconds / 60;
            int seconds = timerSeconds % 60;
            String timeText = String.format("%02d:%02d", minutes, seconds);

            timerLabel.setText(timeText);
        }else{
            deactivateMonitoringTimer();
        }
    }
    // Обновление времени мониторинг-таймера Pomodoro
    private void updateMonitoringTimerPomodoro(){
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

    // Запуск часов в главном окне
    public void activateClock(){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateClockTime(clockLabel));
            }
        },0,1000);
    }
    // Функция для обновления часов
    private void updateClockTime(Label label) {
        Date now = new Date();
        // Форматируем время в нужный формат
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.LL.yyyy");

        String currentTime = sdf.format(now);

        label.setText(currentTime);
    }
}
