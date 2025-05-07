package productivityMonitor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SettingsService {
    public static ObservableList<String> langList = FXCollections.observableArrayList("English","Русский");
    public static ObservableList<String> colorList = FXCollections.observableArrayList("Purple", "Green");

    public static String localization="en";
}
