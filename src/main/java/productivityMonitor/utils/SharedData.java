package productivityMonitor.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SharedData {
    // Список запрещенных процессов
    public static ObservableList<String> processList = FXCollections.observableArrayList();

    // Установленное время работы мониторы(в минутах)
    public static int minutes = 0;

    // Список запрещенных доменов
    public static ObservableList<String> urlList = FXCollections.observableArrayList();

    // Флаг для запуска веб-сервера
    public static boolean isWebSocketServerActive = true;

    // Флаг для обозначения запуска мониторинга
    public static volatile boolean isMonitoringActive = false;
}
