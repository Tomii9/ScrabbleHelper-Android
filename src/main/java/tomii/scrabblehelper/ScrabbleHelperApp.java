package tomii.scrabblehelper;

import android.app.Application;
import java.util.List;
import java.util.Vector;

public class ScrabbleHelperApp extends Application {
    private String server;
    private String token = new String();
    private boolean isAdmin;
    private String userName;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
