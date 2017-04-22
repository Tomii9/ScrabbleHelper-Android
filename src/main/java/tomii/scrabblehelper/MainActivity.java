package tomii.scrabblehelper;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.InputType.TYPE_CLASS_TEXT;

public class MainActivity extends AppCompatActivity {

    private List<ImageView> handimages;
    private List<Character> hand;
    private ImageView[][] boardImages;
    private DisplayMetrics displayMetrics;
    char[][] board;
    boolean firstTurn;
    boolean firstSelection;
    int[] coord;
    private char EMPTY = '\u0000';
    String httpParam;
    String httpURL;
    RequestController requestController = new RequestController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);

        firstSelection = true;
        coord = new int[2];
        board = ((ScrabbleHelperApp) MainActivity.this.getApplication()).getBoard();
        boardImages = new ImageView[15][15];
        hand = ((ScrabbleHelperApp) MainActivity.this.getApplication()).getHand();
        firstTurn = board[7][7] == EMPTY;
        handimages = new ArrayList<>();
        for (int i=0; i<7; i++) {
            ImageView img = (ImageView) findViewById(getResources().getIdentifier("hand_" + i, "id", getPackageName()));
            handimages.add(img);
        }

        setupBoard();

        Button searchButton = (Button) findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button drawButton = (Button) findViewById(R.id.draw);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hand.size()<7) {
                    showDrawDialog();
                } else {
                    hand.clear();
                    refreshHand();
                    showDrawDialog();
                }
            }
        });

        final Button resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndConfirmationDialog("Are you sure you want to reset the board?", false);
            }
        });

        Button checkButton = (Button) findViewById(R.id.check);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCheckDialog();
            }
        });

        Button scoresButton = (Button) findViewById(R.id.topscores);
        scoresButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button endButton = (Button) findViewById(R.id.endgame);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndConfirmationDialog("Are you sure you want to end the game?", true);
            }
        });

    }

    private void showCheckDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("What word would you like to check?");

        final EditText word = new EditText(MainActivity.this);
        word.setInputType(TYPE_CLASS_TEXT);
        word.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        builder.setView(word);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pattern pattern = Pattern.compile("[a-zA-Z]*");
                Matcher matcher = pattern.matcher(word.getText().toString());
                if (!matcher.matches()) {
                    showErrorDialog("This contains illegal characters!");
                    dialog.cancel();
                } else {
                    httpParam = word.getText().toString();
                    new HttpRequestTask().execute();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDrawDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("What did you draw?");

        final EditText drawnletters = new EditText(MainActivity.this);
        drawnletters.setInputType(TYPE_CLASS_TEXT);
        drawnletters.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7 - hand.size())});
        builder.setView(drawnletters);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pattern pattern = Pattern.compile("[a-zA-Z]*");
                Matcher matcher = pattern.matcher(drawnletters.getText().toString());
                if (!matcher.matches()) {
                    showErrorDialog("This contains illegal characters!");
                    dialog.cancel();
                } else {
                    parseHand(drawnletters.getText().toString());
                    refreshHand();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showInputDialog(final int length, final int x, final int y, final boolean across) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Word to place on board:");

        final EditText input = new EditText(MainActivity.this);
        input.setInputType(TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(length)});
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pattern pattern = Pattern.compile("[a-zA-Z]*");
                Matcher matcher = pattern.matcher(input.getText().toString());
                if (input.length()!=length) {
                    showErrorDialog("This word is too short");
                    dialog.cancel();
                } else if (!matcher.matches()) {
                    showErrorDialog("This word contains illegal characters!");
                } else {
                    placeWord(input.getText().toString(), x, y, across);
                    firstTurn = false;
                }

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void showEndConfirmationDialog(String message, final boolean endgame) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetBoard();
                clearSelectionsOnBoard();
                if (endgame) {
                    // TODO
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void showErrorDialog(String errorMessage) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(errorMessage);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void placeWord(String word, int x, int y, boolean across) {
        char[]boardSave = new char[word.length()];
        if (!across) {
            for (int i=0; i<word.length();i++) {
                if (isOverWrite(x+i, y, word.charAt(i))) {
                    for (int k=0; k<i; k++) {
                        if (boardSave[k] == EMPTY) {
                            boardImages[x+k][y].setImageResource(android.R.color.transparent);
                            board[x+k][y]=EMPTY;
                        } else {
                            boardImages[x + k][y].setImageResource(getResources().getIdentifier(Character.toString(boardSave[k]), "drawable", getPackageName()));
                            board[x+k][y]=boardSave[k];
                        }
                    }
                    showErrorDialog("Illegal move: OverWriting letter already on board!");
                    break;
                }
                boardSave[i]=board[x+i][y];
                board[x+i][y]=word.charAt(i);
                boardImages[x+i][y].setImageResource(getResources().getIdentifier(Character.toString(word.charAt(i)), "drawable", getPackageName()));
            }
        } else {
            for (int i=0; i<word.length(); i++) {
                if (isOverWrite(x, y+i, word.charAt(i))) {
                    for (int k=0; k<i; k++) {
                        if (boardSave[k] == EMPTY) {
                            boardImages[x][y+k].setImageResource(android.R.color.transparent);
                            board[x][y+k]=EMPTY;
                        } else {
                            boardImages[x][y+k].setImageResource(getResources().getIdentifier(Character.toString(boardSave[k]), "drawable", getPackageName()));
                            board[x][y+k] = boardSave[k];
                        }
                    }
                    showErrorDialog("Illegal move: OverWriting letter already on board!");
                    break;
                }
                boardSave[i]=board[x][y+i];
                board[x][y+i]=word.charAt(i);
                boardImages[x][y+i].setImageResource(getResources().getIdentifier(Character.toString(word.charAt(i)), "drawable", getPackageName()));
            }
        }
    }

    private boolean isOverWrite(int x, int y, char c) {
        char ch = board[x][y];
        return ch != EMPTY && ch != c;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHand();
    }

    private int dpToPixel(float dp){
        return (int)(dp * ((float)displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void setupBoard(){
        boardImages = new ImageView[15][15];
        android.widget.GridLayout boardLayout = (android.widget.GridLayout) findViewById(R.id.board);
        int tilePadding = dpToPixel(3);
        int tileSize = dpToPixel(20);
        for (int i=0; i<15; i++) {
            for (int j = 0; j < 15; j++) {
                ImageView tile = new ImageView(this);
                tile.setLayoutParams(new GridLayout.LayoutParams(new ViewGroup.LayoutParams(tileSize, tileSize)));
                tile.setAdjustViewBounds(true);
                tile.setScaleType(ImageView.ScaleType.FIT_XY);
                tile.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);;
                final int x = i;
                final int y = j;
                tile.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        selectTile(x, y);

                        return false;
                    }
                });
                if (board[i][j] != EMPTY) {
                    tile.setImageResource(getResources().getIdentifier(Character.toString(board[i][j]), "drawable", getPackageName()));
                } else {
                    tile.setImageResource(android.R.color.transparent);
                }
                boardImages[i][j] = tile;
                boardLayout.addView(tile);
            }
        }
        drawBonusFields();
    }

    private void selectTile(int x, int y) {
        boolean legalMove = false;
        if (firstSelection) {
            clearSelectionsOnBoard();
            coord[0]=x;
            coord[1]=y;
            firstSelection=false;
            boardImages[x][y].setBackgroundResource(R.drawable.box_preselected);
        } else {
            if (coord[0]==x && coord[1]!=y) {
                firstSelection=true;
                if (coord[1] < y) {
                    int temp = y;
                    y = coord[1];
                    coord[1] = temp;
                }
                if (firstTurn && coord[1]>=7 && y <=7 && x==7) {
                    legalMove = true;
                }
                for (int i = y; i <= coord[1]; i++) {
                    boardImages[x][i].setBackgroundResource(R.drawable.box_selected);
                    if ((board[x][i] != EMPTY || checkAdjacentTiles(x, i)) && !firstTurn) {
                        legalMove = true;
                    }
                }
                if (legalMove) {
                    showInputDialog(Math.abs(coord[1] - y) + 1, x, y, true);
                }else if (firstTurn){
                    showErrorDialog("It is the first turn, you have to use the middle tile!");
                    clearSelectionsOnBoard();
                } else{
                    showErrorDialog("Illegal move: Doesn't connect to any letter on the board!");
                    clearSelectionsOnBoard();
                }
            }
            if (coord[1]==y && coord[0]!=x) {
                firstSelection=true;
                if (coord[0] < x) {
                    int temp = x;
                    x = coord[0];
                    coord[0] = temp;
                }
                if (firstTurn && coord[0]>=7 && x <=7  && y==7) {
                    legalMove = true;
                }
                for (int i = x; i <= coord[0]; i++) {
                    boardImages[i][y].setBackgroundResource(R.drawable.box_selected);
                    if ((board[i][y] != EMPTY || checkAdjacentTiles(i, y)) && !firstTurn) {
                        legalMove = true;
                    }
                }
                if (legalMove) {
                    showInputDialog(Math.abs(coord[0] - x) + 1, x, y, false);
                } else if (firstTurn){
                    showErrorDialog("It is the first turn, you have to use the middle tile!");
                    clearSelectionsOnBoard();
                } else {
                    showErrorDialog("Illegal move: Doesn't connect to any letter on the board!");
                    clearSelectionsOnBoard();
                }
            }
        }
    }

    private boolean checkAdjacentTiles(int x, int y) {
        return (x != 0 && board[x-1][y] != EMPTY) || (x != 14 && board[x+1][y] != EMPTY)
                || (y != 0 && board[x][y-1] != EMPTY) || (y != 14 && board[x][y+1] != EMPTY);
    }

    private void clearSelectionsOnBoard() {
        for (int i=0; i<15; i++) {
            for (int j=0; j<15; j++) {
                boardImages[i][j].setBackgroundResource(R.drawable.box);
            }
        }
        drawBonusFields();
    }

    private void parseHand(String handString) {

        for (int i=0; i<handString.length(); i++) {
            String letter = String.valueOf(handString.charAt(i));
            hand.add(letter.charAt(0));
            handimages.get(i).setImageResource(getResources().getIdentifier(letter, "drawable", getPackageName()));
        }
    }

    private void refreshHand() {
        for (int i = 0; i < 7; i++) {
            if (i < hand.size()) {
                handimages.get(i).setImageResource(getResources().getIdentifier(hand.get(i).toString(), "drawable", getPackageName()));
            } else {
                handimages.get(i).setImageResource(android.R.color.transparent);
            }
        }
    }

    private void resetBoard() {
        for (int i=0; i<15; i++) {
            for (int j=0; j<15; j++) {
                board[i][j]=EMPTY;
                boardImages[i][j].setImageResource(android.R.color.transparent);
            }
        }
        firstTurn=true;
    }

    private void drawBonusFields() {
        String[][] bonuses = new String[][]{{"tripleword", "box", "box", "doubleletter", "box", "box", "box", "tripleword", "box", "box", "box", "doubleletter", "box", "box", "tripleword"},
                                            {"box", "doubleword", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "doubleword", "box"},
                                            {"box", "box", "doubleword", "box", "box", "box", "doubleletter", "box", "doubleletter", "box", "box", "box", "doubleword", "box", "box"},
                                            {"doubleletter", "box", "box", "doubleword", "box", "box", "box", "doubleletter", "box", "box", "box", "doubleword", "box", "box", "doubleletter"},
                                            {"box", "box", "box", "box", "doubleword", "box", "box", "box", "box", "box", "doubleword", "box", "box", "box", "box"},
                                            {"box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box"},
                                            {"box", "box", "doubleletter", "box", "box", "box", "doubleletter", "box", "doubleletter", "box", "box", "box", "doubleletter", "box", "box"},
                                            {"tripleword", "box", "box", "doubleletter", "box", "box", "box", "start", "box", "box", "box", "doubleletter", "box", "box", "tripleword"},
                                            {"box", "box", "doubleletter", "box", "box", "box", "doubleletter", "box", "doubleletter", "box", "box", "box", "doubleletter", "box", "box"},
                                            {"box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box"},
                                            {"box", "box", "box", "box", "doubleword", "box", "box", "box", "box", "box", "doubleword", "box", "box", "box", "box"},
                                            {"doubleletter", "box", "box", "doubleword", "box", "box", "box", "doubleletter", "box", "box", "box", "doubleword", "box", "box", "doubleletter"},
                                            {"box", "box", "doubleword", "box", "box", "box", "doubleletter", "box", "doubleletter", "box", "box", "box", "doubleword", "box", "box"},
                                            {"box", "doubleword", "box", "box", "box", "tripleletter", "box", "box", "box", "tripleletter", "box", "box", "box", "doubleword", "box"},
                                            {"tripleword", "box", "box", "doubleletter", "box", "box", "box", "tripleword", "box", "box", "box", "doubleletter", "box", "box", "tripleword"}};
        for (int i=0; i<15; i++) {
            for (int j=0; j<15; j++) {
                boardImages[i][j].setBackgroundResource(getResources().getIdentifier(bonuses[i][j], "drawable", getPackageName()));
            }
        }
    }

    private class HttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final String url = "http://192.168.43.123:8080/checklegitimacy?word=" + httpParam;
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String exists = restTemplate.getForObject(url, String.class);
                return exists.equals("true");
            } catch (Exception e) {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean exists) {
            if (exists) {
                showErrorDialog("YES");
            } else {
                showErrorDialog("NO");
            }
        }

    }

}
