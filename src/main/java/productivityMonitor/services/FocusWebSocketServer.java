package productivityMonitor.services;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import com.google.gson.Gson;
import productivityMonitor.utils.ConsoleLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static productivityMonitor.controllers.MonitoringSettingsController.urlList;
import static productivityMonitor.services.MonitoringManager.*;
import static productivityMonitor.services.StageService.createModeAlertWindow;

public class FocusWebSocketServer extends WebSocketServer {
    private final Gson gson = new Gson();
    private final ConsoleLogger logger;

    public FocusWebSocketServer(int port, ConsoleLogger logger) {
        super(new InetSocketAddress(port));
        this.logger=logger;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Расширение подключилось");
        sendBlacklist(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Получено сообщение: " + message);
        if(message.startsWith("CT:")){
            handleTabClosed(message.substring(3));
        }
    }

    // Обработка закрытия вкладки для разных режимов
    public void handleTabClosed(String url) {
        switch (currentMode.getName()) {
            case "Mindfulness":
                handleMindfulnessTabClosed(url);
                break;
            case "Sailor's Knot":
                handleSailorsKnotTabClosed(url);
                break;
            case "Delay Gratification":
                handleDelayGratificationTabClosed(url);
                break;
            case "Pomodoro":
                logger.log("Pomodoro: заблокирована вкладка "+url+"\n");
                break;
            case "FullLockdown":
                logger.log("FullLockdown: заблокирована вкладка "+url+"\n");
                break;
        }
    }

    private void handleMindfulnessTabClosed(String url) {
        if (countAlertWindow < maxAlertWindow && !isPaused) {
            isPaused = true;
            Platform.runLater(() -> {
                try {
                    Stage stage=new Stage();
                    stage.setOnHidden(event->{
                        countAlertWindow++;
                        isPaused=false;
                    });
                    createModeAlertWindow("/fxml/mindfulnessWindowView.fxml","Warning!",stage,false);
                } catch (IOException e) {
                    logger.log("Ошибка при создании окна Mindfulness: " + e.getMessage() + "\n");
                }
            });
        }
        logger.log("Mindfulness: заблокирована вкладка " + url + "\n");
    }

    private void handleSailorsKnotTabClosed(String url) {
        if (!isTaskCompleted && !isTaskRunning) {
            isTaskRunning = true;
            Platform.runLater(() -> {
                try {
                    Stage stage=new Stage();
                    stage.setOnHidden(event->{
                        isTaskRunning=false;
                    });
                    createModeAlertWindow("/fxml/sailorsKnotWindowView.fxml","Sailor's Knot Task",stage,false);
                } catch (IOException e) {
                    logger.log("Ошибка при создании окна Sailor's Knot: " + e.getMessage() + "\n");
                }
            });
        }
        logger.log("Sailor's Knot: заблокирована вкладка " + url + "\n");
    }

    private void handleDelayGratificationTabClosed(String url) {
        if (!isDelayOver && !isDelayRunning) {
            isDelayRunning = true;
            Platform.runLater(() -> {
                try {
                    Stage stage=new Stage();
                    stage.setOnHidden(event->{
                        isDelayRunning=false;
                    });
                    createModeAlertWindow("/fxml/delayGratificationWindowView.fxml","Delay Timer",stage,false);
                } catch (IOException e) {
                    logger.log("Ошибка при создании окна Delay Gratification: " + e.getMessage() + "\n");
                }
            });
        }
        logger.log("Delay Gratification: заблокирована вкладка " + url + "\n");
    }

    public void sendBlacklist(WebSocket conn) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "update_blacklist");
        msg.put("urls", urlList);

        conn.send(gson.toJson(msg));
    }

    public void broadcastBlacklist() {
        for (WebSocket conn : getConnections()) {
            sendBlacklist(conn);
        }
    }

    public void addToBlacklist(String domain) {
        urlList.add(domain);
        broadcastBlacklist();
        System.out.println("Добавлен в blacklist: " + domain);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Расширение отключилось: " + conn.getRemoteSocketAddress() + " по причине: " + reason);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Ошибка WebSocket-соединения: " + ex.getMessage());
        if (conn != null) {
            // Можно логировать или обрабатывать конкретное соединение
        }
    }

    @Override
    public void onStart() {
        System.out.println("Сервер WebSocket запущен успешно!");
    }
}
