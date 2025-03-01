package productivityMonitor.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class SharedData {
    // Список запрещенных процессов
    public static ObservableList<String> processList = FXCollections.observableArrayList();
}
