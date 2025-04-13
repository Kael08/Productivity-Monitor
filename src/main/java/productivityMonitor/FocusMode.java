package productivityMonitor;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import productivityMonitor.controllers.MainController;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static productivityMonitor.utils.SharedData.*;

public class FocusMode {
    // Основная сцена
    private final MainController mainController;

    // Основная консоль
    private TextArea consoleTextArea;

    // Поток мониторинга
    private Thread monitorThread;

    // Задачи мониторинга
    private final AtomicReference<Runnable> currentTask = new AtomicReference<>();

    // Сервер для браузера
    private FocusWebSocketServer webSocketServer;

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
                appendToConsole("Монитор запущен без таймера!\n");
                while (isMonitoringActive) {
                    try {
                        closeProcess(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Монитор остановлен по прерыванию!\n");
                        break;
                    }
                }
            } else {
                long endTime = System.currentTimeMillis()+minutes*60*1000;
                appendToConsole("Монитор запущен с таймером на "+minutes+" минут!\n");
                while (isMonitoringActive&&System.currentTimeMillis()<endTime) {
                    try {
                        closeProcess(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        appendToConsole("Монитор остановлен по прерыванию!\n");
                        break;
                    }
                }
                stopMonitoring();

                appendToConsole("Время вышло. Монитор завершил работу!\n");
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

        appendToConsole("Монитор завершил работу\n");
    };

    // Режим, при котором для запуска приложения или перехода
    // на определенный домен требуется выполнить трудное задание
    public void setSailorsKnot(){
        System.out.println("sailorsknot");
    }

    // Режим, который требует подождать несколько минут для
    // запуска нежелательного приложения или домена
    public void setDelayGratification(){
        System.out.println("delaygratification");
    }

    // Режим, который пытается отговорить пользователя
    // от запуска нежелательного приложения или домена
    public void setMindfulness(){
        System.out.println("mindfulness");
    }

    private Runnable mindfulness = () ->{

    };

    // Режим, суть которого заключается в перемене
    // 5 минут каждые 25 минут работы(параметры времени можно настроить)
    public void setPomodoro(){
        System.out.println("pomodoro");
    }

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
                    // Это также вызывается, когда процесса не было или он не найден, и из-за этого мусорится консоль
                    //consoleTextArea.appendText("Ошибка при завершении процесса " + pn + "\n");
                }
            } catch (Exception e) {
                appendToConsole("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
            }
        }
    }
}
