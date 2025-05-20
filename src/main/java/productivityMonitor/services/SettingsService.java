package productivityMonitor.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import static productivityMonitor.utils.DataLoader.saveColorToFile;

public class SettingsService {
    public static ObservableList<String> langList = FXCollections.observableArrayList("English","Русский");
    public static ObservableList<String> colorList = FXCollections.observableArrayList("Purple", "Green","Red","Black","Blue","White");

    public static String localization="en";

    // Пути до стилей
    public static String profileStylePath = "/styles/purple/purple_profile.css";
    public static String statisticsStylePath = "/styles/purple/purple_statistics.css";
    public static String authStylePath = "/styles/purple/purple_auth.css";
    public static String regStylePath = "/styles/purple/purple_reg.css";
    public static String delayGratificationWindowStylePath = "/styles/purple/purple_delayGratificationWindow.css";
    public static String mindfulnessWindowStylePath = "/styles/purple/purple_mindfulnessWindow.css";
    public static String sailorsKnotWindowStylePath = "/styles/purple/purple_sailorsKnotWindow.css";
    public static String mainStylePath = "/styles/purple/purple_main.css";
    public static String monitoringSettingsStylePath = "/styles/purple/purple_monitoringSettings.css";
    public static String plansStylePath = "/styles/purple/purple_plans.css";
    public static String notesStylePath = "/styles/purple/purple_notes.css";
    public static String settingsStylePath = "/styles/purple/purple_settings.css";
    public static String timerStylePath = "/styles/purple/purple_timer.css";


    // Установка оформления
    public static void setUIColor(String color){
        profileStylePath="/styles/"+color+"/"+color+"_profile.css";
        statisticsStylePath="/styles/"+color+"/"+color+"_statistics.css";
        authStylePath="/styles/"+color+"/"+color+"_auth.css";
        delayGratificationWindowStylePath="/styles/"+color+"/"+color+"_delayGratificationWindow.css";
        mindfulnessWindowStylePath="/styles/"+color+"/"+color+"_mindfulnessWindow.css";
        sailorsKnotWindowStylePath="/styles/"+color+"/"+color+"_sailorsKnotWindow.css";
        regStylePath="/styles/"+color+"/"+color+"_reg.css";
        mainStylePath="/styles/"+color+"/"+color+"_main.css";
        monitoringSettingsStylePath="/styles/"+color+"/"+color+"_monitoringSettings.css";
        plansStylePath="/styles/"+color+"/"+color+"_plans.css";
        notesStylePath="/styles/"+color+"/"+color+"_notes.css";
        settingsStylePath="/styles/"+color+"/"+color+"_settings.css";
        timerStylePath="/styles/"+color+"/"+color+"_timer.css";
        UIColor=color;
        saveColorToFile(color);
    }

    // Цвет приложения
    public static String UIColor = "purple";
}
