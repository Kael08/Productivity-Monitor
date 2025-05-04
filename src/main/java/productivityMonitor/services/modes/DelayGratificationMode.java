package productivityMonitor.services.modes;

import javafx.application.Platform;
import javafx.stage.Stage;
import productivityMonitor.interfaces.MonitoringMode;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.ProcessUtils;

import java.io.IOException;

import static productivityMonitor.services.MonitoringManager.isDelayOver;
import static productivityMonitor.services.MonitoringManager.isDelayRunning;
import static productivityMonitor.services.StageService.createModeAlertWindow;
import static productivityMonitor.utils.SharedData.isMonitoringActive;
import static productivityMonitor.utils.SharedData.processList;
import static productivityMonitor.utils.TimerUtils.minutes;

public class DelayGratificationMode implements MonitoringMode {
    private final ProcessUtils processUtils;
    private final ConsoleLogger logger;

    public DelayGratificationMode(ProcessUtils processUtils, ConsoleLogger logger) {
        this.processUtils = processUtils;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "Delay Gratification";
    }

    @Override
    public void start(long durationMillis) {
        logger.log("Мониторинг запущен в режиме Delay Gratification!\n");
        long endTime = durationMillis == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;

        try {
            if (minutes == 0) {
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        if (!isDelayOver) {
                            if (processUtils.isProcessesActive(processList)) {
                                processUtils.closeProcesses(processList);
                                if (!isDelayRunning) {
                                    isDelayRunning=true;
                                    Platform.runLater(() -> {
                                        try {
                                            Stage stage=new Stage();
                                            stage.setOnHidden(event->{
                                                isDelayRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/delayGratificationWindowView.fxml","Delay Timer",stage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        }
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
                        if (!isDelayOver) {
                            if (processUtils.isProcessesActive(processList)) {
                                processUtils.closeProcesses(processList);
                                if (!isDelayRunning) {
                                    isDelayRunning=true;
                                    Platform.runLater(() -> {
                                        try {
                                            Stage stage=new Stage();
                                            stage.setOnHidden(event->{
                                                isDelayRunning=false;
                                            });
                                            createModeAlertWindow("/fxml/delayGratificationWindowView.fxml","Delay Timer",stage,false);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    });
                                }
                            }
                        }
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
            isDelayRunning=false;
        }
    }

    @Override
    public void stop() {
        isMonitoringActive=false;
        isDelayRunning=false;
        isDelayOver=false;
    }
}