package productivityMonitor.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import productivityMonitor.models.CustomMode;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import static productivityMonitor.services.MonitoringManager.sailorsKnotTextList;
import static productivityMonitor.services.MonitoringManager.motivationMessagesList;

public class DataLoader {
    // Функция для чтения файла с мотивирующими сообщениями
    public static void readMotivationMessages() {
        Type messageListType = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();

        try (FileReader reader = new FileReader("src/main/resources/data/motivation_messages.json")) {
            motivationMessagesList = gson.fromJson(reader, messageListType);
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Функция для чтения файла с текстом для Sailor's Knot
    public static void readSailorsKnotText(){
        Type sailorsKnotTextListType = new TypeToken<List<String>>() {}.getType();
        Gson gson = new Gson();

        try(FileReader reader = new FileReader("src/main/resources/data/sailorsKnotText.json")){
            sailorsKnotTextList=gson.fromJson(reader,sailorsKnotTextListType);
        } catch (Exception e){
            System.out.println("Ошибка: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void saveCustomModeToFile(CustomMode customMode, String filePath) {
        Gson gson = new Gson();
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            gson.toJson(customMode, fileWriter);
        } catch (Exception e){
            System.out.println("ОШИБКА В DataLoader: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static CustomMode loadCustomModeFromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, CustomMode.class);
        }
    }

    public static void saveLocalizationToFile(String localization){
        Gson gson = new Gson();
        try(FileWriter fileWriter = new FileWriter("src/main/resources/data/localization.json")){
            gson.toJson(localization,fileWriter);
        }catch (Exception e){
            System.out.println("ОШИБКА В DataLoader: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static String loadLocalizationFromFile(){
        Gson gson=new Gson();
        try(FileReader fileReader = new FileReader("src/main/resources/data/localization.json")){
            return gson.fromJson(fileReader,String.class);
        }catch (Exception e){
            System.out.println("ОШИБКА В DataLoader: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
