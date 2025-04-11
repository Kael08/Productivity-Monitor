package productivityMonitor;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static productivityMonitor.utils.SharedData.*;

public class FocusMode {
    private TextArea consoleTextArea;
    private Thread monitorThread;
    private final AtomicReference<Runnable> currentTask = new AtomicReference<>();

    // Сервер для контроля браузера
    private FocusWebSocketServer webSocketServer;

    public FocusMode(TextArea consoleTextArea) {
        this.consoleTextArea = consoleTextArea;
    }

    private void appendToConsole(String text) {
        if (consoleTextArea != null) {
            Platform.runLater(() -> consoleTextArea.appendText(text));
        }
    }

    public void setFullLockdownMode() {
        setMonitoringTask(fullLockdown);
    }

    private Runnable fullLockdown = () -> {
        appendToConsole("Монитор запущен!\n");

        // Запуск WebSocket-сервера
        if (isWebSocketServerActive && webSocketServer == null) {
            webSocketServer = new FocusWebSocketServer(8081);
            webSocketServer.start();
            System.out.println("WebSocket-сервер запущен");
        }

        try {
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
        } finally {
            if (isWebSocketServerActive && webSocketServer != null) {
                boolean wasInterrupted = Thread.interrupted(); // Сбросить флаг
                try {
                    webSocketServer.stop();
                } catch (Exception e) {
                    System.err.println("Ошибка при остановке WebSocket: " + e.getMessage());
                } finally {
                    if (wasInterrupted) {
                        Thread.currentThread().interrupt(); // Восстановить флаг
                    }
                    webSocketServer = null;
                    System.out.println("WebSocket-сервер остановлен");
                }
            }
        }

        appendToConsole("Монитор завершил работу.\n");
    };


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

    public void stopMonitoring() {
        isMonitoringActive = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

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
                    //consoleTextArea.appendText("Процесс " + pn + " был завершен\n");
                    appendToConsole("Процесс " + pn + " был завершен\n");
                } else {
                    // Это также вызывается, когда процесса не было или он не найден, и из-за этого мусорится консоль
                    //consoleTextArea.appendText("Ошибка при завершении процесса " + pn + "\n");
                }
            } catch (Exception e) {
                //consoleTextArea.appendText("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
                appendToConsole("Не удалось завершить процесс " + pn + ": " + e.getMessage() + "\n");
            }
        }
    }

    // Режим, при котором для запуска приложения или перехода
    // на определенный домен требуется выполнить трудное задание
    public void sailorsKnot(){

    }

    // Режим, который требует подождать несколько минут для
    // запуска нежелательного приложения или домена
    public void delayGratification(){

    }

    // Режим, который пытается отговорить пользователя
    // от запуска нежелательного приложения или домена
    public void mindfulness(){

    }

    // Режим, суть которого заключается в перемене
    // 5 минут каждые 25 минут работы(параметры времени можно настроить)
    public void pomodoro(){

    }
}
