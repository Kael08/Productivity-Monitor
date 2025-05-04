package productivityMonitor.services;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import productivityMonitor.controllers.MainController;
import productivityMonitor.interfaces.MonitoringMode;
import productivityMonitor.services.modes.*;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.ProcessUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static productivityMonitor.services.StageService.createModeAlertWindow;
import static productivityMonitor.utils.SharedData.*;
import static productivityMonitor.utils.TimerUtils.minutes;

public class MonitoringManager {
    public static volatile int pomodoroWorkTime = 1500; // Время работы Pomodoro в секундах
    public static volatile int pomodoroChillTime = 300; // Время отдыха Pomodoro в секундах

    // Флаги режимов
    public static volatile boolean isTaskCompleted = false; // Sailor's Knot: Задача выполнена
    public static volatile boolean isTaskRunning = false;  // Sailor's Knot: Задача запущена
    public static volatile boolean isDelayOver = false;    // Delay Gratification: Задержка завершена
    public static volatile boolean isDelayRunning = false; // Delay Gratification: Задержка запущена
    public static volatile boolean isPaused = false;       // Mindfulness: Пауза

    // Статические параметры
    public static final int maxAlertWindow = 5;
    public static volatile int countAlertWindow = 0;

    private final Map<String, MonitoringMode> modes = new HashMap<>(); // Регистрация режимов
    private MonitoringMode currentMode; // Текущий активный режим
    private Thread monitorThread; // Поток мониторинга
    private FocusWebSocketServer webSocketServer; // Сервер для браузера
    private final ConsoleLogger logger; // Логгер
    private final ProcessUtils processUtils; // Утилиты для работы с процессами
    private MainController mainController; // Контроллер UI
    private final ReentrantLock monitorLock = new ReentrantLock(); // Блокировка для синхронизации

    // Задача мониторинга
    private final AtomicReference<Runnable> currentTask = new AtomicReference<>();

    public MonitoringManager(ConsoleLogger logger, MainController mainController) {
        this.mainController = mainController;
        this.logger = logger;
        this.processUtils = new ProcessUtils(logger);
        initializeModes();
    }

    private void initializeModes() {
        modes.put("FullLockdown", new FullLockdownMode(processUtils, logger));
        modes.put("Sailor's Knot", new SailorsKnotMode(processUtils, logger));
        modes.put("Delay Gratification", new DelayGratificationMode(processUtils, logger));
        modes.put("Mindfulness", new MindfulnessMode(processUtils, logger));
        modes.put("Pomodoro", new PomodoroMode(processUtils, logger));
    }

    // Установка режима мониторинга
    public void setMode(String modeName) {
        monitorLock.lock();
        try {
            currentMode = modes.get(modeName);
            if (currentMode == null) {
                logger.log("Режим " + modeName + " не найден!\n");
                currentMode = modes.get("FullLockdown"); // Резервный режим
            }
            currentTask.set(null); // Сброс текущей задачи при смене режима
            logger.log("Установлен режим: " + modeName + "\n");
        } finally {
            monitorLock.unlock();
        }
    }

    // Запуск мониторинга
    public void startMonitoring() {
        if (!isMonitoringActive) {
            monitorLock.lock();
            try {
                isMonitoringActive = true;
                long durationMillis = minutes == 0 ? 0 : minutes * 60 * 1000L;

                if (isWebSocketServerActive && webSocketServer == null) {
                    webSocketServer = new FocusWebSocketServer(8081, logger);
                    webSocketServer.start();
                    logger.log("WebSocket-сервер запущен\n");
                }

                monitorThread = new Thread(() -> {
                    try {
                        if (currentMode != null) {
                            currentMode.start(durationMillis);
                        } else {
                            Runnable task = currentTask.get();
                            if (task != null) {
                                task.run();
                            }
                        }
                    } catch (Exception e) {
                        logger.log("Ошибка во время мониторинга: " + e.getMessage() + "\n");
                    } finally {
                        stopMonitoring();
                    }
                });
                monitorThread.setDaemon(true);
                monitorThread.start();
                logger.log("Мониторинг запущен\n");
            } finally {
                monitorLock.unlock();
            }
        }
    }

    // Остановка мониторинга
    public void stopMonitoring() {
        monitorLock.lock();
        try {
            if (isMonitoringActive) {
                isMonitoringActive = false;
                if (monitorThread != null && monitorThread.isAlive()) {
                    monitorThread.interrupt();
                }
                cleanupResources();
                logger.log("Мониторинг остановлен\n");
            }
        } finally {
            monitorLock.unlock();
        }

        Platform.runLater(() -> {
            if (mainController != null) {
                mainController.setDisableAllButtons(false);
                mainController.setRunImageView();
                mainController.timerUtils.deactivateMonitoringTimer();
            }
        });
    }

    // Очистка ресурсов
    private void cleanupResources() {
        if (isWebSocketServerActive && webSocketServer != null) {
            try {
                webSocketServer.stop();
                logger.log("WebSocket-сервер остановлен\n");
            } catch (Exception e) {
                logger.log("Ошибка при остановке WebSocket: " + e.getMessage() + "\n");
            }
            webSocketServer = null;
        }
        // Сброс флагов
        isTaskCompleted = false;
        isTaskRunning = false;
        isDelayOver = false;
        isDelayRunning = false;
        isPaused = false;
        countAlertWindow = 0;
    }
}