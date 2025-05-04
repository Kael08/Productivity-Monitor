package productivityMonitor.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CustomMode {
    @SerializedName("name")
    public String name;
    @SerializedName("modeName")
    public String modeName;
    @SerializedName("processList")
    public List<String> processList;
    @SerializedName("urlList")
    public List<String> urlList;
    @SerializedName("isWebSocketServerActive")
    public boolean isWebSocketServerActive;

    public CustomMode(String name,String modeName,List<String> processList,
                      List<String> urlList,boolean isWebSocketServerActive){
        this.name=name;
        this.modeName=modeName;
        this.processList=processList;
        this.urlList=urlList;
        this.isWebSocketServerActive=isWebSocketServerActive;
    }

    public boolean isWebSocketServerActive() {
        return isWebSocketServerActive;
    }

    public List<String> getProcessList() {
        return processList;
    }

    public List<String> getUrlList(){
        return urlList;
    }

    public String getModeName() {
        return modeName;
    }

    public String getName() {
        return name;
    }
}
