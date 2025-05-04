package productivityMonitor.services;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import productivityMonitor.controllers.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static productivityMonitor.services.StageService.createModeAlertWindow;
import static productivityMonitor.utils.SharedData.*;
import static productivityMonitor.utils.TimerUtils.minutes;

public class FocusMode {
    // Минуты для работы в Помодоро
    private static volatile int pomodoroWorkTime=1500;
    // Минуты для перемены в Помодоро
    private static volatile int pomodoroChillTime=300;

    // Основная сцена
    private MainController mainController;

    // Основная консоль
    private TextArea consoleTextArea;

    // Поток мониторинга
    private Thread monitorThread;

    // Флаг для режима Sailors`s Knot, который проверяет, что задание выполнено
    public static volatile boolean isTaskCompleted=false;

    // Флаг для режима Sailor`s Knot, который говорит, что задача запущена
    public static volatile  boolean isTaskRunning=false;

    // Флаг для режима Delay Gratification, который проверяет, что задержка закончилась
    public static volatile boolean isDelayOver=false;

    // Флаг для режима Delay Gratification, который говорит, что таймер задержки запущен
    public static volatile boolean isDelayRunning=false;

    // Задачи мониторинга
    private final AtomicReference<Runnable> currentTask = new AtomicReference<>();

    // Сервер для браузера
    private FocusWebSocketServer webSocketServer;

    // Блокировка доменов через брэндмауэр Windows
    private WindowsFirewallDomainBlocker windowsFirewallDomainBlocker=new WindowsFirewallDomainBlocker();

    // Пауза
    //public static Object pauseLock = new Object();

    // Флаг для паузы
    public static volatile boolean isPaused=false;

    private Stage modeStage=null;

    public FocusMode(TextArea consoleTextArea, MainController mainController) {
        this.consoleTextArea = consoleTextArea;
        this.mainController=mainController;
    }

    // Метод для записи текста в основную консоль
    private void appendToConsole(String text) {
        if (consoleTextArea != null) {
            // Специальный метод в javaFX для выполнения кода в потоке javaFX Application Thread
            Platform.runLater(() -> consoleTextArea.appendText(text));
        }
    }

    // Установка режима полной блокировки
    public void setFullLockdownMode() {
        setMonitoringTask(fullLockdown);
    }

    // Задача Полной блокировки
    private Runnable fullLockdown = () -> {
        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081,consoleTextArea,mainController);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }
        // Запуск блокировщика доменов
        /*if(isDomainBlockerActive){
            startBlocker();
            appendToConsole("Блокировщик доменов запущен!\n");
        }*/


        try {
            if(minutes==0) {
                appendToConsole("Мониторинг запущен в режиме FullLockdown!\n");
                while (isMonitoringActive) {
                    try {
                        closeProcess(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг остановлен!\n");
                        break;
                    }
                }
            } else {
                long endTime = System.currentTimeMillis()+(minutes*60*1000);
                appendToConsole("Мониторинг запущен на "+minutes+" минут в режиме FullLockdown!\n");
                while (isMonitoringActive&&System.currentTimeMillis()<endTime) {
                    try {
                        closeProcess(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг прерван!\n");
                        break;
                    }
                }
                stopMonitoring();

                appendToConsole("Время вышло!\n");
            }
        } finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage()+"\n");
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    appendToConsole("WebSocket-сервер остановлен\n");
                }
            }
            /*if(isDomainBlockerActive){
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try{
                    stopBlocker();
                } catch (Exception e){
                    appendToConsole("Ошибка при остановке блокировщика доменов\n"+e.getMessage()+"\n");
                } finally {
                    if(wasInterrupted){
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    appendToConsole("Блокировщик доменов остановлен!\n");
                }
            }*/
        }

        appendToConsole("Мониторинг завершен\n");
    };

    // Закрывает процессы по имени
    private void closeProcess(List<String> list) {
        for (String pn : list) {
            try {
                ProcessBuilder builder = new ProcessBuilder("taskkill", "/IM", pn, "/F");
                Process process = builder.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    appendToConsole("Процесс " + pn + " был завершен\n");
                } else {
                    // Это также вызывается, когда процесса не было или он не найден, и из-за этого "мусорится" консоль
                    //consoleTextArea.appendText("Ошибка при завершении процесса " + pn + "\n");
                }
            } catch (Exception e) {
                appendToConsole("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
            }
        }
    }

    // Режим, при котором для запуска приложения или перехода
    // на определенный домен требуется выполнить трудное задание
    public void setSailorsKnot(){
        setMonitoringTask(sailorsKnot);
    }

    private Runnable sailorsKnot=()->{
        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081,consoleTextArea,mainController);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }

        try {
            if (minutes == 0) {
                appendToConsole("Мониторинг запущен в режиме Sailor`s Knot!\n");
                while (isMonitoringActive) {
                    try {
                        if (!isTaskCompleted) {
                            if (isProcessesActive(processList)) {
                                closeProcess(processList);
                                if (!isTaskRunning) {
                                    isTaskRunning = true;
                                    Platform.runLater(() -> {
                                        try {
                                            modeStage=new Stage();
                                            modeStage.setOnHidden(event->{
                                                isTaskRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/sailorsKnotWindowView.fxml","Sailor's Knot Task",modeStage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        } else {
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг остановлен!\n");
                        break;
                    }
                }
            } else {
                appendToConsole("Мониторинг запущен на " + minutes + " минут в режиме Sailor`s Knot!\n");
                long endTime = System.currentTimeMillis() + (minutes * 60 * 1000);
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        if (!isTaskCompleted) {
                            if (isProcessesActive(processList)) {
                                closeProcess(processList);
                                if (!isTaskRunning) {
                                    isTaskRunning = true;
                                    Platform.runLater(() -> {
                                        try {
                                            modeStage=new Stage();
                                            modeStage.setOnHidden(event->{
                                                isTaskRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/sailorsKnotWindowView.fxml","Sailor's Knot Task",modeStage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        } else {
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг прерван!\n");
                        break;
                    }
                }
                stopMonitoring();

                appendToConsole("Время вышло!\n");
            }
        }finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    appendToConsole("WebSocket-сервер остановлен\n");
                }
            }
        }
    };

    // Режим, который требует подождать несколько минут для
    // запуска нежелательного приложения или домена
    public void setDelayGratification(){
        setMonitoringTask(delayGratification);
    }

    private Runnable delayGratification=()->{
        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081,consoleTextArea,mainController);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }
        try{
            if (minutes == 0) {
                appendToConsole("Мониторинг запущен в режиме Delay Gratification!\n");
                while (isMonitoringActive) {
                    try {
                        if (!isDelayOver) {
                            if (isProcessesActive(processList)) {
                                closeProcess(processList);
                                if (!isDelayRunning) {
                                    isDelayRunning = true;
                                    Platform.runLater(() -> {
                                        try {
                                            modeStage=new Stage();
                                            modeStage.setOnHidden(event->{
                                                isDelayRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/delayGratificationWindowView.fxml","Delay Timer",modeStage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        } else{
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг остановлен!\n");
                        break;
                    }
                }
            } else {
                appendToConsole("Мониторинг запущен на " + minutes + " минут в режиме Delay Gratification!\n");
                long endTime = System.currentTimeMillis() + (60 * 1000 * minutes);
                while (System.currentTimeMillis() < endTime) {
                    try {
                        if (!isDelayOver) {
                            if (isProcessesActive(processList)) {
                                closeProcess(processList);
                                if (!isDelayRunning) {
                                    isDelayRunning = true;
                                    Platform.runLater(() -> {
                                        try {
                                            modeStage=new Stage();
                                            modeStage.setOnHidden(event->{
                                                isDelayRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/delayGratificationWindowView.fxml","Delay Timer",modeStage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        } else{
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг прерван!\n");
                        break;
                    }
                }
                stopMonitoring();
                appendToConsole("Время вышло!");
            }
        }finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    appendToConsole("WebSocket-сервер остановлен\n");
                }
            }
        }
    };

    public static int maxAlertWindow = 5;
    public static int countAlertWindow = 0;

    // Режим, который пытается отговорить пользователя
    // от запуска нежелательного приложения или домена
    public void setMindfulness(){
        setMonitoringTask(mindfulness);
    }



    private Runnable mindfulness = () ->{
        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081,consoleTextArea,mainController);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }
        try{
            if (minutes == 0) {
                appendToConsole("Мониторинг запущен в режиме Mindfulness!\n");
                while (isMonitoringActive) {
                    try {
                        if (countAlertWindow < maxAlertWindow && isProcessesActive(processList)) {
                            closeProcess(processList);
                            if (!isPaused) {
                                isPaused = true;
                                Platform.runLater(() -> {
                                    try {
                                        modeStage=new Stage();
                                        modeStage.setOnHidden(event->{
                                            countAlertWindow++;
                                            isPaused=false;
                                        });
                                        createModeAlertWindow("/fxml/mindfulnessWindowView.fxml","Warning!",modeStage,false);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        // Если число закрытий окон превзошло лимит, то нужно преждевременно выключить WebSocketServer
                        } else if(countAlertWindow==maxAlertWindow){
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг остановлен!\n");
                        break;
                    }
                }
            } else {
                appendToConsole("Мониторинг запущен на " + minutes + " минут в режиме Mindfulness!\n");
                long endTime = System.currentTimeMillis() + (minutes * 60 * 1000);
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        if (countAlertWindow < maxAlertWindow && isProcessesActive(processList)) {
                            closeProcess(processList);
                            if (!isPaused) {
                                isPaused = true;
                                Platform.runLater(() -> {
                                    try {
                                        modeStage=new Stage();
                                        modeStage.setOnHidden(event->{
                                            countAlertWindow++;
                                            isPaused=false;
                                        });
                                        createModeAlertWindow("/fxml/mindfulnessWindowView.fxml","Warning!",modeStage,false);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                            // Если число закрытий окон превзошло лимит, то нужно преждевременно выключить WebSocketServer
                        } else if(countAlertWindow==maxAlertWindow){
                            if (isWebSocketServerActive && webSocketServer != null) {
                                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                                try {
                                    webSocketServer.stop();
                                } catch (Exception e) {
                                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                                } finally {
                                    if (wasInterrupted) {
                                        Thread.currentThread().interrupt(); // Восстановить флаг
                                    }
                                    webSocketServer = null;
                                    appendToConsole("WebSocket-сервер остановлен\n");
                                }
                            }
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг прерван!\n");
                        break;
                    }
                }
                stopMonitoring();
                appendToConsole("Время вышло!\n");
            }
        }finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    appendToConsole("WebSocket-сервер остановлен\n");
                }
            }
        }
        appendToConsole("Мониторинг окончен!\n");
        countAlertWindow=0;
    };

    // Поиск активных процессов среди нежелательных процессов
    private boolean isProcessesActive(List<String> requiredProcessesList){
        return getProcessList().stream().anyMatch(requiredProcessesList::contains);
    }

    // Получить список всех процессов
    private List<String> getProcessList(){
        List<String> processList = new ArrayList<>();
        ProcessBuilder builder = new ProcessBuilder("tasklist");
        builder.redirectErrorStream(true);

        try{
            Process process = builder.start();
            BufferedReader reader=new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    String processName = line.split("\\s+")[0];
                    processList.add(processName);
                }
            }

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return processList;
    }

    // Режим, суть которого заключается в перемене
    // 5 минут каждые 25 минут работы(параметры времени можно настроить)
    public void setPomodoro(){
        setMonitoringTask(pomodoro);
    }

    private Runnable pomodoro = () -> {
        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081,consoleTextArea,mainController);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }
        try{
            if (minutes == 0) {
                appendToConsole("Мониторинг запущен в режиме Pomodoro!\n");
                while (isMonitoringActive) {
                    try {
                        // Рабочая фаза (25 минут)
                        appendToConsole("Рабочая фаза (25 минут)\n");
                        long workEndTime = System.currentTimeMillis() + pomodoroWorkTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < workEndTime) {
                            closeProcess(processList); // Закрываем процессы каждую секунду
                            Thread.sleep(1000);
                            long remaining = (workEndTime - System.currentTimeMillis()) / 1000;
                            //appendToConsole("Осталось работать: " + calcTime((int) remaining) + "\r");
                        }

                        if (!isMonitoringActive) break;

                        // Фаза отдыха (5 минут)
                        appendToConsole("\nФаза отдыха (5 минут)\n");
                        long chillEndTime = System.currentTimeMillis() + pomodoroChillTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < chillEndTime) {
                            Thread.sleep(1000);
                            long remaining = (chillEndTime - System.currentTimeMillis()) / 1000;
                            //appendToConsole("Осталось отдыхать: " + calcTime((int) remaining) + "\r");
                        }

                        if (!isMonitoringActive) break;
                        appendToConsole("\nЦикл завершен, начинаем новый...\n");

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Мониторинг прерван!\n");
                        break;
                    }
                }
            } else {
                appendToConsole("Мониторинг запущен на " + minutes + " минут в режиме Pomodoro!\n");
                long totalEndTime = System.currentTimeMillis() + minutes * 60 * 1000;

                try {
                    while (isMonitoringActive && System.currentTimeMillis() < totalEndTime) {
                        // Рабочая фаза (25 минут или оставшееся время)
                        int workTime = Math.min(pomodoroWorkTime, (int) ((totalEndTime - System.currentTimeMillis()) / 1000));
                        if (workTime <= 0) break;

                        appendToConsole("Рабочая фаза (" + (workTime / 60) + " минут)\n");
                        long workEndTime = System.currentTimeMillis() + workTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < workEndTime) {
                            closeProcess(processList); // Закрываем процессы каждую секунду
                            Thread.sleep(1000);
                            long remaining = (workEndTime - System.currentTimeMillis()) / 1000;
                            //appendToConsole("Осталось работать: " + calcTime((int) remaining) + "\r");
                        }

                        if (!isMonitoringActive) break;

                        // Фаза отдыха (5 минут или оставшееся время)
                        int chillTime = Math.min(pomodoroChillTime,
                                (int) ((totalEndTime - System.currentTimeMillis()) / 1000));
                        if (chillTime <= 0) break;

                        appendToConsole("\nФаза отдыха (" + (chillTime / 60) + " минут)\n");
                        long chillEndTime = System.currentTimeMillis() + chillTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < chillEndTime) {
                            Thread.sleep(1000);
                            long remaining = (chillEndTime - System.currentTimeMillis()) / 1000;
                            //appendToConsole("Осталось отдыхать: " + calcTime((int) remaining) + "\r");
                        }

                        if (!isMonitoringActive) break;
                        appendToConsole("\n");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    appendToConsole("Мониторинг прерван!\n");
                }

                stopMonitoring();
                appendToConsole("Время вышло!\n");
            }
        }finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    appendToConsole("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    appendToConsole("WebSocket-сервер остановлен\n");
                }
            }
        }
        appendToConsole("Мониторинг окончен!\n");
    };

    // Начать мониторинг
    public void startMonitoring() {
        if (!isMonitoringActive) {
            isMonitoringActive = true;
            monitorThread = new Thread(() -> {
                Runnable task = currentTask.get();
                if (task != null) {
                    task.run();
                }
            });
            monitorThread.start();
        }
    }

    // Остановить мониторинг
    public void stopMonitoring() {
        isMonitoringActive = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }

        Platform.runLater(()->{
            if(mainController!=null)
                mainController.setDisableAllButtons(false); // Включение элементов
                mainController.setRunImageView();
                mainController.timerUtils.deactivateMonitoringTimer();
        });
    }

    // Установка задачи для мониторинга
    private void setMonitoringTask(Runnable task) {
        currentTask.set(task);
    }


}
