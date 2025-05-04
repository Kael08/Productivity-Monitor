package productivityMonitor.utils;

import com.google.gson.Gson;
import productivityMonitor.models.CustomMode;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {
    public static void saveCustomModeToFile(CustomMode customMode, String filePath) {
        Gson gson = new Gson();
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            gson.toJson(customMode, fileWriter);
        } catch (Exception e){
            System.out.println("ОШИБКА В JsonUtils: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static CustomMode loadCustomModeFromFile(String filePath) throws IOException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, CustomMode.class);
        }
    }
}
