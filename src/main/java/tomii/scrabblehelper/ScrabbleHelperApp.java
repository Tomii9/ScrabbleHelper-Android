package tomii.scrabblehelper;

import android.app.Application;
import java.util.List;
import java.util.Vector;

public class ScrabbleHelperApp extends Application {
    private List<Character> hand = new Vector<Character>();
    private String server;
    private String token = new String();
    private boolean isAdmin;
    private char[][] board;
    private int score;
    private String userName;

    @Override
    public void onCreate() {
        super.onCreate();
        board = new char[15][15];
    }

    public List<Character> getHand() {
        return hand;
    }

    public void setHand(List<Character> hand) {
        this.hand = hand;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public char[][] getBoard() {
        return this.board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
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
