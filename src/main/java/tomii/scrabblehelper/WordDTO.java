package tomii.scrabblehelper;

/**
 * Created by Tomii on 2017.04.23..
 */

public class WordDTO {
    private String word;
    private int x;
    private int y;
    private boolean down;
    private int value;

    public WordDTO () {
    }

    public WordDTO (String word, int x, int y, boolean down, int value) {
        this.word = word;
        this.x = x;
        this.y = y;
        this.down = down;
        this.value = value;
    }

    public WordDTO (String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isDown() {
        return down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
