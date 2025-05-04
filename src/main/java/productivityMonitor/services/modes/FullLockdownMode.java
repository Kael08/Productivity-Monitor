package productivityMonitor.services.modes;

import productivityMonitor.interfaces.MonitoringMode;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.ProcessUtils;

import static productivityMonitor.controllers.MonitoringSettingsController.processList;
import static productivityMonitor.services.MonitoringManager.isMonitoringActive;
import static productivityMonitor.utils.TimerUtils.minutes;

public class FullLockdownMode implements MonitoringMode {
    private final ProcessUtils processUtils;
    private final ConsoleLogger logger;

    public FullLockdownMode(ProcessUtils processUtils, ConsoleLogger logger) {
        this.processUtils = processUtils;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "FullLockdown";
    }

    @Override
    public void start(long durationMillis) {
        logger.log("Мониторинг запущен в режиме FullLockdown!\n");
        long endTime = durationMillis == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;

        try {
            if (minutes==0) {
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        processUtils.closeProcesses(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log("Мониторинг остановлен!\n");
                        break;
                    }
                }
            } else {
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        processUtils.closeProcesses(processList);
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log("Мониторинг прерван!\n");
                        break;
                    }
                }
                logger.log("Время вышло!\n");
            }
        } finally {
            logger.log("Мониторинг завершен\n");
        }
    }

    @Override
    public void stop() {
        isMonitoringActive=false;
    }
}