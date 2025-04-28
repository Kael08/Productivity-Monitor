package productivityMonitor.utils;

import com.google.gson.Gson;

import java.io.FileWriter;

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
}
