package productivityMonitor.services.modes;

import javafx.application.Platform;
import javafx.stage.Stage;
import productivityMonitor.interfaces.MonitoringMode;
import productivityMonitor.utils.ConsoleLogger;
import productivityMonitor.utils.ProcessUtils;

import java.io.IOException;

import static productivityMonitor.services.MonitoringManager.*;
import static productivityMonitor.services.StageService.createModeAlertWindow;
import static productivityMonitor.utils.SharedData.isMonitoringActive;
import static productivityMonitor.utils.SharedData.processList;
import static productivityMonitor.utils.TimerUtils.minutes;

public class MindfulnessMode implements MonitoringMode {
    private final ProcessUtils processUtils;
    private final ConsoleLogger logger;

    public MindfulnessMode(ProcessUtils processUtils, ConsoleLogger logger) {
        this.processUtils = processUtils;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return "Mindfulness";
    }

    @Override
    public void start(long durationMillis) {
        logger.log("Мониторинг запущен в режиме Mindfulness!\n");
        long endTime = durationMillis == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;

        try {
            if (minutes == 0) {
                while (isMonitoringActive && System.currentTimeMillis() < endTime) {
                    try {
                        if (countAlertWindow < maxAlertWindow &&
                                processUtils.isProcessesActive(processList)) {
                            processUtils.closeProcesses(processList);
                            if (!isPaused) {
                                isPaused=true;
                                Platform.runLater(() -> {
                                    try {
                                        Stage stage=new Stage();
                                        stage.setOnHidden(event->{
                                            countAlertWindow++;
                                            isPaused=false;
                                        });
                                        createModeAlertWindow("/fxml/mindfulnessWindowView.fxml","Warning!",stage,false);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        } else if (countAlertWindow == maxAlertWindow) {
                            break;
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
                        if (countAlertWindow < maxAlertWindow &&
                                processUtils.isProcessesActive(processList)) {
                            processUtils.closeProcesses(processList);
                            if (!isPaused) {
                                isPaused=true;
                                Platform.runLater(() -> {
                                    try {
                                        Stage stage=new Stage();
                                        stage.setOnHidden(event->{
                                            countAlertWindow++;
                                            isPaused=false;
                                        });
                                        createModeAlertWindow("/fxml/mindfulnessWindowView.fxml","Warning!",stage,false);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                            }
                        } else if (countAlertWindow == maxAlertWindow) {
                            break;
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
            countAlertWindow=0;
            logger.log("Мониторинг окончен!\n");
        }
    }

    @Override
    public void stop() {
        isMonitoringActive=false;
        isPaused=false;
        countAlertWindow=0;
    }
}