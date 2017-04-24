package tomii.scrabblehelper;

import android.app.Application;
import java.util.List;
import java.util.Vector;

public class ScrabbleHelperApp extends Application {
    private List<Character> hand = new Vector<Character>();
    private String connectionURL;
    char[][] board;
    int score;

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

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
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
}
