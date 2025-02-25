package productivityMonitor.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

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
    private Label clockLabel; // Часы

    @FXML
    private TextField inputTextField; // Поле ввода в консоли

    List<String> processList = new ArrayList<>(); // Хранит имена блокируемых процессов

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
        deleteProcess(inputTextField.getText());
    }

    @FXML
    private void handleAddButton(ActionEvent event) {
        System.out.println("Кнопка Add нажата!");
        addProcess(inputTextField.getText());
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
                Thread.currentThread().sleep(5000);
                System.out.println("Ещё один цикл");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Устанавливаем флаг прерывания
                System.out.println("Мониторинг был прерван.");
                return; // Прерываем выполнение потока
            }
        }
    };
    // Добавление процесса в список блокируемых процессов
    private void addProcess(String processName){
        if(processName.isEmpty()) {
            consoleTextArea.appendText("Ошибка: Название процесса не может быть пустым!\n");
            return;
        } else if(processName.equals(".exe")) {
            consoleTextArea.appendText("Ошибка: Введите название процесса!\n");
            return;
        }

        if(!processName.endsWith(".exe")){
            processName+=".exe";
        }

        consoleTextArea.appendText(processName+"\n");
        processList.add(processName);
    }

    // Удаление процесса из списка блокируемых процессов
    private void deleteProcess(String processName){
        if(processName.isEmpty()) {
            consoleTextArea.appendText("Ошибка: Название процесса не может быть пустым!\n");
            return;
        }

        if(!processName.endsWith(".exe")){
            processName+=".exe";
        } else if(processName.equals(".exe")) {
            consoleTextArea.appendText("Ошибка: Введите название процесса!\n");
            return;
        }

        final String copyProcessName = processName;

        boolean isRemoved =  processList.removeIf(process->process.equals(copyProcessName));

        if(isRemoved){
            consoleTextArea.appendText("Процесс успешно удален!\n");
        } else {
            consoleTextArea.appendText("Ошибка: Процесс с именем '" + processName + "' не найден!\n");
        }
    }



}