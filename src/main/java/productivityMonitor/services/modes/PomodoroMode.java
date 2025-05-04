package productivityMonitor.services.modes;

import productivityMonitor.interfaces.MonitoringMode;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.ProcessUtils;

import static productivityMonitor.controllers.MonitoringSettingsController.processList;
import static productivityMonitor.services.MonitoringManager.isMonitoringActive;
import static productivityMonitor.services.MonitoringManager.pomodoroChillTime;
import static productivityMonitor.services.MonitoringManager.pomodoroWorkTime;
import static productivityMonitor.utils.TimerUtils.minutes;

public class PomodoroMode implements MonitoringMode {
    private final ProcessUtils processUtils;
    private final ConsoleLogger logger;

    public PomodoroMode(ProcessUtils processUtils, ConsoleLogger logger) {
        this.processUtils = processUtils;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "Pomodoro";
    }

    @Override
    public void start(long durationMillis) {
        logger.log("Мониторинг запущен в режиме Pomodoro!\n");
        long totalEndTime = durationMillis == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;

        try {
            if (minutes == 0) {
                while (isMonitoringActive && System.currentTimeMillis() < totalEndTime) {
                    try {
                        // Рабочая фаза (25 минут)
                        logger.log("Рабочая фаза (" + (pomodoroWorkTime / 60) + " минут)\n");
                        long workEndTime = System.currentTimeMillis() + pomodoroWorkTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < workEndTime) {
                            processUtils.closeProcesses(processList);
                            Thread.sleep(1000);
                        }

                        if (!isMonitoringActive) break;

                        // Фаза отдыха (5 минут)
                        logger.log("\nФаза отдыха (" + (pomodoroChillTime / 60) + " минут)\n");
                        long chillEndTime = System.currentTimeMillis() + pomodoroChillTime * 1000;

                        while (isMonitoringActive && System.currentTimeMillis() < chillEndTime) {
                            Thread.sleep(1000);
                        }

                        if (!isMonitoringActive) break;
                        logger.log("\nЦикл завершен, начинаем новый...\n");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log("Мониторинг прерван!\n");
                        break;
                    }
                }
            } else {
                while (isMonitoringActive && System.currentTimeMillis() < totalEndTime) {
                    // Рабочая фаза
                    int workTime = Math.min(pomodoroWorkTime,
                            (int) ((totalEndTime - System.currentTimeMillis()) / 1000));
                    if (workTime <= 0) break;

                    logger.log("Рабочая фаза (" + (workTime / 60) + " минут)\n");
                    long workEndTime = System.currentTimeMillis() + workTime * 1000;

                    while (isMonitoringActive && System.currentTimeMillis() < workEndTime) {
                        processUtils.closeProcesses(processList);
                        Thread.sleep(1000);
                    }

                    if (!isMonitoringActive) break;

                    // Фаза отдыха
                    int chillTime = Math.min(pomodoroChillTime,
                            (int) ((totalEndTime - System.currentTimeMillis()) / 1000));
                    if (chillTime <= 0) break;

                    logger.log("\nФаза отдыха (" + (chillTime / 60) + " минут)\n");
                    long chillEndTime = System.currentTimeMillis() + chillTime * 1000;

                    while (isMonitoringActive && System.currentTimeMillis() < chillEndTime) {
                        Thread.sleep(1000);
                    }
                }
                logger.log("Время вышло!\n");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log("Мониторинг прерван!\n");
        } finally {
            logger.log("Мониторинг окончен!\n");
        }
    }

    @Override
    public void stop() {
        isMonitoringActive=false;
    }
}