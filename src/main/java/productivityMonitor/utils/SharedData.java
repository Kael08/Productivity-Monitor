package productivityMonitor.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedData {
    // Volatile - модификатор переменных, который обеспечивает видимость изменений переменной другими потоками

    // Список запрещенных процессов
    public static ObservableList<String> processList = FXCollections.observableArrayList();

    // Установленное время работы мониторы(в минутах)
    public static int minutes = 0;

    // Список запрещенных доменов
    public static ObservableList<String> urlList = FXCollections.observableArrayList();

    // Список режимов
    public static ObservableList<String> modeList = FXCollections.observableArrayList("FullLockdown","Mindfulless","Sailors's Knot","Delay Gratification", "Pomodoro");

    // Флаг для запуска веб-сервера
    public static boolean isWebSocketServerActive = true;

    // Флаг для обозначения запуска мониторинга
    public static volatile boolean isMonitoringActive = false;

    // Список с мотивирующими сообщениями для Mindfulness-режима мониторинга
    public static List<String> motivationMessagesList = new ArrayList<>();

    // Функция для чтения файла с мотивирующими сообщениями
    public static void readMotivationMessages() {
        Type messageListType = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();

        try (FileReader reader = new FileReader("src/main/resources/json_files/motivation_messages.json")) {
            motivationMessagesList = gson.fromJson(reader, messageListType);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
