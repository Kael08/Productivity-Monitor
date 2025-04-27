package productivityMonitor.utils;

public class User {
    private static User user;

    private String username;

    private User(){}

    public boolean isUserActive=false;

    public static User getUser(){
        if(user==null){
            user=new User();
        }
        return user;
    }

    public String getUsername(){
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public void activateUser(){
        isUserActive=true;
    }

    public void deactivateUser(){
        this.username=null;
        isUserActive=false;
    }
}
