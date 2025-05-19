package productivityMonitor.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import productivityMonitor.models.CustomMode;
import productivityMonitor.models.DailyStats;
import productivityMonitor.utils.CryptoUtils;

import java.io.FileWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static productivityMonitor.controllers.MonitoringSettingsController.customModeList;
import static productivityMonitor.controllers.MonitoringSettingsController.customModeListOb;
import static productivityMonitor.models.User.getUser;

public class TokenManager {
    private static String accessToken;
    private static String refreshToken;
    private static long accessTokenExpirationTime; // Когда истекает access-токен

    private static final HttpClient client = HttpClient.newHttpClient();

    public static List<DailyStats> dailyStatsList=new ArrayList<>();

    // Проверка что access-токен валиден
    public static boolean isAccessTokenValid(){
        return accessToken!=null&&System.currentTimeMillis()<accessTokenExpirationTime;
    }

    // Обновление access-токена
    public static boolean refreshAccessToken(){
        if(!loadRefreshToken()){
            return false;
        }

        try{
            String json=String.format("{\"refreshToken\":\"%s\"}",refreshToken);

            HttpRequest request=HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/auth/refresh"))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            if(response.statusCode()==200){
                Gson gson=new Gson();
                JsonObject jsonObject = gson.fromJson(response.body(),JsonObject.class);

                accessToken=jsonObject.get("accessToken").getAsString();
                accessTokenExpirationTime=System.currentTimeMillis()+1000*55*60;
                return true;
            } else{
                return false;
            }
        } catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    public static void setTokens(String access,String refresh){
        try(FileWriter fileWriter=new FileWriter("src/main/resources/data/REFRESH_TOKEN.json")){
            accessToken=access;
            refreshToken=refresh;
            accessTokenExpirationTime=System.currentTimeMillis()+1000*55*60;

            String encryptedRefresh = CryptoUtils.encrypt(refresh);
            fileWriter.write("\"" + encryptedRefresh + "\"");
        } catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getAccessToken(){
        return accessToken;
    }

    public static boolean loadRefreshToken(){
        try{
            refreshToken = Files.readString(java.nio.file.Path.of("src/main/resources/data/REFRESH_TOKEN.json")).trim().replace("\"", "");
            refreshToken = CryptoUtils.decrypt(refreshToken);
            if(refreshToken.isEmpty())
                return false;
            return true;
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    public static void updateUser(){
        try{
            String username;
            HttpRequest request=HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/users/me"))
                    .header("Authorization","Bearer "+accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.body(),JsonObject.class);

            username=jsonObject.get("username").getAsString();

            getUser().setUsername(username);
            getUser().activateUser();
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadCustomModes(){
        try{
            HttpRequest request=HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/customModes/"))
                    .header("Authorization","Bearer "+accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response=client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            System.out.println(response.body());

            Gson gson = new Gson();
            JsonObject[] jsonObject = gson.fromJson(response.body(),JsonObject[].class);

            for(JsonObject obj:jsonObject){
                CustomMode mode = new CustomMode(
                        obj.get("name").getAsString(),
                        obj.get("mode_name").getAsString(),
                        gson.fromJson(obj.get("process_list"), new TypeToken<List<String>>(){}.getType()),
                        gson.fromJson(obj.get("url_list"), new TypeToken<List<String>>(){}.getType()),
                        obj.get("is_domain_blocker_active").getAsBoolean()
                );
                customModeList.add(mode.name);
                customModeListOb.put(mode.name,mode);
            }
        }catch (Exception e){
            System.out.println("ОШИБКА ПРИ ЗАГРУЗКЕ КАСТОМНЫХ РЕЖИМОВ: "+e.getMessage());
            e.printStackTrace();
        }
    }

    // Загрузка дневной статистики с сервера
    public static void loadDailyStatistics() {
        try {
            // Получаем сегодняшнюю дату
            LocalDate today = LocalDate.now(); // 2025-05-19
            LocalDate oneMonthAgo = today.minusMonths(1); // 2025-04-19

            // Форматируем дату
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String endDate = today.format(formatter); // 2025-05-19
            String startDate = oneMonthAgo.format(formatter); // 2025-04-19

            // Формируем URL с параметрами в строке запроса
            String query = String.format("start_date=%s&end_date=%s",
                    URLEncoder.encode(startDate, StandardCharsets.UTF_8),
                    URLEncoder.encode(endDate, StandardCharsets.UTF_8));
            String url = "http://localhost:3000/statistics/?" + query;

            //System.out.println("Query: " + query);

            // Создаем GET-запрос
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            Gson gson = new Gson();
            JsonObject[] jsonObjects = gson.fromJson(response.body(), JsonObject[].class);

            dailyStatsList.clear();

            for (JsonObject obj : jsonObjects) {
                // Извлекаем данные
                String date = obj.get("date").getAsString().split("T")[0]; // Берем только дату (2025-05-17)
                JsonObject monitoringTime = obj.getAsJsonObject("monitoring_time");

                // Проверяем наличие полей и используем значения по умолчанию, если они отсутствуют
                int hours = monitoringTime.has("hours") ? monitoringTime.get("hours").getAsInt() : 0;
                int minutes = monitoringTime.has("minutes") ? monitoringTime.get("minutes").getAsInt() : 0;
                int seconds = monitoringTime.has("seconds") ? monitoringTime.get("seconds").getAsInt() : 0;
                int blockedProcesses = obj.get("blocked_processes").getAsInt();
                int blockedDomains = obj.get("blocked_domains").getAsInt();

                // Создаем объект DailyStats
                DailyStats ds = new DailyStats(date, hours, minutes, seconds, blockedProcesses, blockedDomains);

                // Выводим данные
                /*System.out.println("Date: " + ds.getDate());
                System.out.println("Monitoring Time: " + ds.getMonitoringTime());
                System.out.println("Blocked Processes: " + ds.getBlockedProcesses());
                System.out.println("Blocked Domains: " + ds.getBlockedDomains());
                System.out.println();*/

                dailyStatsList.add(ds);
            }



        } catch (Exception e) {
            System.out.println("ОШИБКА ПРИ ЗАГРУЗКЕ СТАТИСТИКИ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Отправка статистики мониторинга на сервер
    public static void saveDailyStatistics(int hours,int minutes,int seconds,int blockedProcesses,int blockedDomains){
        try{
            String time=hours+":"+minutes+":"+seconds;// Преобразуем в нужный формат

            LocalDate today = LocalDate.now();
            // Форматируем дату
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String date = today.format(formatter);

            String json=String.format("{\"date\":\"%s\",\"monitoring_time\":\"%s\",\"blocked_processes\":\"%s\",\"blocked_domains\":\"%s\"}",date,time,blockedProcesses,blockedDomains);

            HttpRequest request=HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:3000/statistics/add"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

            System.out.println("POST /statistics/add Response Code: " + response.statusCode());
            System.out.println("POST /statistics/add Response Body: " + response.body());
        }catch (Exception e){
            System.out.println("ОШИБКА ПРИ ОТПРАВКЕ СТАТИСТИКИ: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    public static void clearTokens() {
        accessToken = null;
        refreshToken = null;
        accessTokenExpirationTime = 0;

        try(FileWriter fileWriter = new FileWriter("src/main/resources/data/REFRESH_TOKEN.json")){
            fileWriter.write("");
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
