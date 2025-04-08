package productivityMonitor;

import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.WebSocket;
import com.google.gson.Gson;

import java.net.InetSocketAddress;
import java.util.*;

import static productivityMonitor.utils.SharedData.urlList;

public class FocusWebSocketServer extends WebSocketServer {

    private final Gson gson = new Gson();

    public FocusWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Расширение подключилось");
        sendBlacklist(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Получено сообщение: " + message);
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

    /*public static void main(String[] args) throws Exception {
        FocusWebSocketServer server = new FocusWebSocketServer(8081);
        server.start();
        System.out.println("WebSocket-сервер запущен на ws://localhost:8081");

        // Через 10 секунд добавим сайт в blacklist
        *//*new Timer().schedule(new TimerTask() {
            public void run() {
            }
        }, 10000);*//*
    }*/

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

