package productivityMonitor.controllers.modeWindowControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import productivityMonitor.interfaces.ModeWindow;
import java.util.Timer;
import java.util.TimerTask;

import static productivityMonitor.services.MonitoringManager.isDelayOver;
import static productivityMonitor.services.MonitoringManager.isDelayRunning;

public class DelayGratificationWindowController implements ModeWindow {
    // Label
    @FXML private Label infoLabel;
    @FXML private Label timerLabel;

    private int seconds = 300; // 5 минут
    private Stage currentStage;

    @Override
    public void setStage(Stage currentStage){
        this.currentStage=currentStage;
    }

    // Функция для обновления времени
    private void updateTimer(Label label){
        seconds--;
        if(seconds<1){
            isDelayOver=true;
            isDelayRunning=false;
            currentStage.close();
        } else {
            timerLabel.setText(calcTime(seconds));
        }
    }

    // Функция для перевода целочисленного значения в минуты и секунды и возвращении в виде строки
    private String calcTime(int val) {
        int minutes = val / 60;
        int seconds = val % 60;  // Более элегантный способ получить секунды

        // Форматируем с ведущим нулём для секунд
        return minutes + ":" + String.format("%02d", seconds);
    }

    @FXML private void initialize(){
        timerLabel.setText(calcTime(seconds));

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(()->updateTimer(timerLabel));
            }
        },0,1000);
    }
}
