package productivityMonitor.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
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
}
