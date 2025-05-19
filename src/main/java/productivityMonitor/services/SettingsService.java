package productivityMonitor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static productivityMonitor.utils.DataLoader.saveColorToFile;

public class SettingsService {
    public static ObservableList<String> langList = FXCollections.observableArrayList("English","Русский");
    public static ObservableList<String> colorList = FXCollections.observableArrayList("Purple", "Green","Red","Black","Blue","White");

    public static String localization="en";

    // Пути до стилей
    public static String profileStylePath = "/styles/purple/purple_profile.css";

    // Установка оформления
    public static void setUIColor(String color){
        profileStylePath="/styles/"+color+"/"+color+"_profile.css";
        UIColor=color;
        saveColorToFile(color);
    }

    // Цвет приложения
    public static String UIColor = "purple";
}
