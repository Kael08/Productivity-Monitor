package productivityMonitor.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

import static productivityMonitor.utils.User.getUser;

public class TokenManager {
    private static String accessToken;
    private static String refreshToken;
    private static long accessTokenExpirationTime; // Когда истекает access-токен

    private static final HttpClient client = HttpClient.newHttpClient();

    public static boolean isAccessTokenValid(){
        return accessToken!=null&&System.currentTimeMillis()<accessTokenExpirationTime;
    }

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
                accessTokenExpirationTime=System.currentTimeMillis()*1000*55*60;
                return true;
            } else{
                return false;
            }

        } catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void setTokens(String access,String refresh){
        try(FileWriter fileWriter=new FileWriter("src/main/resources/json_files/REFRESH_TOKEN.json")){
            accessToken=access;
            refreshToken=refresh;
            accessTokenExpirationTime=System.currentTimeMillis()*1000*55*60;
            fileWriter.write("\""+refreshToken+"\"");
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
            refreshToken = Files.readString(java.nio.file.Path.of("src/main/resources/json_files/REFRESH_TOKEN.json"));
            refreshToken = refreshToken.trim().replace("\"", "");
            return true;
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
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

    public static void clearTokens() {
        accessToken = null;
        refreshToken = null;
        accessTokenExpirationTime = 0;

        try(FileWriter fileWriter = new FileWriter("src/main/resources/json_files/REFRESH_TOKEN.json")){
            fileWriter.write("");
        }catch (Exception e){
            System.out.println("ОШИБКА: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
