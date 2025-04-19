package productivityMonitor;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import productivityMonitor.controllers.MainController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static productivityMonitor.controllers.MainController.countAlertWindow;
import static productivityMonitor.controllers.MainController.maxAlertWindow;
import static productivityMonitor.utils.SharedData.*;

public class FocusMode {
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

    // Пауза
    public static Object pauseLock = new Object();

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
            webSocketServer = new FocusWebSocketServer(8081);
            webSocketServer.start();
            appendToConsole("WebSocket-сервер запущен\n");
        }

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
                long endTime = System.currentTimeMillis()+minutes*60*1000;
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
        if(minutes==0){
            appendToConsole("Мониторинг запущен в режиме Sailor`s Knot!\n");
            while(isMonitoringActive){
                try{
                    if(!isTaskCompleted){
                        if(isProcessesActive(processList)){
                            closeProcess(processList);
                            if(!isTaskRunning){
                                isTaskRunning=true;
                                Platform.runLater(()->{
                                    try {
                                        mainController.createSailorsKnotWindow();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        }
                        Thread.sleep(2000);
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    appendToConsole("Мониторинг остановлен!\n");
                    break;
                }
            }
        }else{
            appendToConsole("Мониторинг запущен на "+minutes+" минут в режиме Sailor`s Knot!\n");
            long endTime=System.currentTimeMillis()*(minutes*60*1000);
            while(isMonitoringActive&&System.currentTimeMillis()<endTime){
                try{
                    if(!isTaskCompleted){
                        if(isProcessesActive(processList)){
                            closeProcess(processList);
                            if(!isTaskRunning){
                                isTaskRunning=true;
                                Platform.runLater(()->{
                                    try {
                                        mainController.createSailorsKnotWindow();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        }
                        Thread.sleep(2000);
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    appendToConsole("Мониторинг прерван!\n");
                    break;
                }
            }
            stopMonitoring();

            appendToConsole("Время вышло!\n");
        }
    };

    // Режим, который требует подождать несколько минут для
    // запуска нежелательного приложения или домена
    public void setDelayGratification(){
        setMonitoringTask(delayGratification);
    }

    private Runnable delayGratification=()->{
        if(minutes==0){
            appendToConsole("Мониторинг запущен в режиме Delay Gratification!\n");
            while(isMonitoringActive){
                try {
                    if(!isDelayOver){
                        if(isProcessesActive(processList)){
                            closeProcess(processList);
                            if(!isDelayRunning){
                                isDelayRunning=true;
                                Platform.runLater(()->{
                                    try {
                                        mainController.createDelayGratificationWindow();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        }
                        Thread.sleep(2000);
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    appendToConsole("Мониторинг остановлен!\n");
                    break;
                }
            }
        }else{
            appendToConsole("Мониторинг запущен на "+minutes+" минут в режиме Delay Gratification!\n");
            long endTime=System.currentTimeMillis()*(60*1000*minutes);
            while (System.currentTimeMillis()<endTime){
                try{
                    if(!isDelayOver){
                        if(isProcessesActive(processList)){
                            closeProcess(processList);
                            if(!isDelayRunning){
                                isDelayRunning=true;
                                Platform.runLater(()->{
                                    try {
                                        mainController.createDelayGratificationWindow();
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        }
                        Thread.sleep(2000);
                    }
                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    appendToConsole("Мониторинг прерван!\n");
                    break;
                }
            }
            stopMonitoring();
            appendToConsole("Время вышло!");
        }
    };

    // Режим, который пытается отговорить пользователя
    // от запуска нежелательного приложения или домена
    public void setMindfulness(){
        setMonitoringTask(mindfulness);
    }

    private Runnable mindfulness = () ->{
        if(minutes==0){
            appendToConsole("Мониторинг запущен в режиме Mindfulness!\n");
            while (isMonitoringActive) {
                try {
                    if (countAlertWindow<maxAlertWindow&&isProcessesActive(processList)) {
                        Platform.runLater(()-> {
                            mainController.createAlertWindow(motivationMessagesList);
                        });
                        isPaused=true;
                        synchronized (pauseLock){
                            while (isPaused){
                                try{
                                    pauseLock.wait();
                                } catch (InterruptedException e){
                                    Thread.currentThread().interrupt();
                                    System.out.println("Ошибка во время паузы");
                                    return;
                                }
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
            appendToConsole("Мониторинг запущен на "+minutes+" минут в режиме Mindfulness!\n");
            long endTime=System.currentTimeMillis()+(minutes*60*1000);
            while (isMonitoringActive&&System.currentTimeMillis()<endTime){
                try {
                    if (countAlertWindow<maxAlertWindow&&isProcessesActive(processList)) {
                        Platform.runLater(()-> {
                            mainController.createAlertWindow(motivationMessagesList);
                        });
                        isPaused=true;
                        synchronized (pauseLock){
                            while (isPaused){
                                try{
                                    pauseLock.wait();
                                } catch (InterruptedException e){
                                    Thread.currentThread().interrupt();
                                    System.out.println("Ошибка во время паузы");
                                    return;
                                }
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

    private Runnable pomodoro =()->{

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
                mainController.enableAllButtons();
                mainController.setRunImageView();
        });
    }

    // Установка задачи для мониторинга
    private void setMonitoringTask(Runnable task) {
        currentTask.set(task);
    }


}
