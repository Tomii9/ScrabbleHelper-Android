package tomii.scrabblehelper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.InputType.TYPE_CLASS_TEXT;

public class MainActivity extends AppCompatActivity {

    private List<ImageView> handimages;
    private List<Character> hand;
    private ImageView[][] boardImages;
    private DisplayMetrics displayMetrics;
    private char[][] board;
    private boolean firstTurn;
    private boolean firstSelection;
    private int[] coord;
    private char EMPTY = '\u0000';
    private String httpParam;
    private int score;
    private HighScoreDTO [] topScores;
    private RequestController requestController;
    private boolean isAdmin;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        String token = ((ScrabbleHelperApp) MainActivity.this.getApplication()).getToken();
        String server = ((ScrabbleHelperApp) MainActivity.this.getApplication()).getServer();
        requestController = new RequestController(token, server);
        firstSelection = true;
        coord = new int[2];
        board = new char[15][15];
        boardImages = new ImageView[15][15];
        hand = new Vector<Character>();
        score = 0;
        isAdmin = ((ScrabbleHelperApp) MainActivity.this.getApplication()).isAdmin();
        userName = ((ScrabbleHelperApp) MainActivity.this.getApplication()).getUserName();
        setScore(score);
        firstTurn = board[7][7] == EMPTY;
        handimages = new ArrayList<>();
        for (int i=0; i<7; i++) {
            ImageView img = (ImageView) findViewById(getResources().getIdentifier("hand_" + i, "id", getPackageName()));
            handimages.add(img);
        }

        setupBoard();

        //Define Buttons
        Button logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog("Are you sure you want to log out?", "logout");
            }
        });

        Button searchButton = (Button) findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hand.size()==0) {
                    showMessageDialog("Your hand is empty!");
                } else if (hand.size()<7) {
                    showConfirmationDialog("Your hand isn't full. Are you unable to draw letters?", "nonFullHand");
                } else {
                    getBestWord();
                }

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

        final Button adminButton = (Button) findViewById(R.id.admin);
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdminDialog();
            }
        });
        if (!isAdmin) {
            adminButton.setVisibility(View.INVISIBLE);
        }

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
                topScores = requestController.getHighScores();
                showTopScoreDialog(topScores);
            }
        });

        Button endButton = (Button) findViewById(R.id.endgame);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog("Are you sure you want to end the game?", "endGame");
            }
        });

    }

    //define methods of buttons
    private void logout() {
        requestController.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    private void getBestWord() {
        String handString = new String();
        for (Character character: hand) {
            handString = handString.concat(character.toString());
        }
        WordDTO wordDTO =requestController.getBestWord(handString);
        if (wordDTO.getWord() == null) {
            showMessageDialog("Cannot find any word, pass the play and shuffle your hand!");
        } else {
            placeWord(wordDTO.getWord(), wordDTO.getX(), wordDTO.getY(), wordDTO.isDown(), true);
            highlightRecentlyPlacedWord(wordDTO);
            setScore(score + wordDTO.getValue());
        }
    }

    private void endGame() {
        resetBoard();
        clearSelectionsOnBoard();
        if (requestController.getOwnHighScore().getHighscore() < score) {
            requestController.setHighScore(score);
            showMessageDialog("New High Score: " + score);
        } else {
            showMessageDialog("Final Score: " + score);
        }
        hand.clear();
        refreshHand();
        setScore(0);
        requestController.endGame();
    }

    private void highlightRecentlyPlacedWord(WordDTO wordDTO) {
        selectTile(wordDTO.getX(), wordDTO.getY(), false);
        if (wordDTO.isDown()) {
            selectTile(wordDTO.getX(), wordDTO.getY()+wordDTO.getWord().length()-1, false);
        } else {
            selectTile(wordDTO.getX()+wordDTO.getWord().length()-1, wordDTO.getY(), false);
        }
    }

    private void placeWord(String word, int x, int y, boolean across, boolean ownPlacement) {

        char[] boardSave = new char[word.length()];
        if (!across) {
            for (int i = 0; i < word.length(); i++) {
                if (isOverWrite(x + i, y, word.charAt(i))) {
                    for (int k = 0; k < i; k++) {
                        if (boardSave[k] == EMPTY) {
                            boardImages[x + k][y].setImageResource(android.R.color.transparent);
                            board[x + k][y] = EMPTY;
                        } else {
                            boardImages[x + k][y].setImageResource(getResources().getIdentifier(Character.toString(boardSave[k]), "drawable", getPackageName()));
                            board[x + k][y] = boardSave[k];
                        }
                    }
                    showMessageDialog("Illegal move: OverWriting letter already on board!");
                    break;
                }
                if (ownPlacement) {
                    if (!hand.contains(new Character(word.charAt(i)))) {
                        hand.remove(new Character('.'));
                    } else {
                        hand.remove(new Character(word.charAt(i)));
                    }
                }
                boardSave[i] = board[x + i][y];
                board[x + i][y] = word.charAt(i);
                boardImages[x + i][y].setImageResource(getResources().getIdentifier(Character.toString(word.charAt(i)), "drawable", getPackageName()));
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                if (isOverWrite(x, y + i, word.charAt(i))) {
                    for (int k = 0; k < i; k++) {
                        if (boardSave[k] == EMPTY) {
                            boardImages[x][y + k].setImageResource(android.R.color.transparent);
                            board[x][y + k] = EMPTY;
                        } else {
                            boardImages[x][y + k].setImageResource(getResources().getIdentifier(Character.toString(boardSave[k]), "drawable", getPackageName()));
                            board[x][y + k] = boardSave[k];
                        }
                    }
                    showMessageDialog("Illegal move: OverWriting letter already on board!");
                    break;
                }
                if (ownPlacement) {
                    if (!hand.contains(new Character(word.charAt(i)))) {
                        hand.remove(new Character('.'));
                    } else {
                        hand.remove(new Character(word.charAt(i)));
                    }
                }
                boardSave[i] = board[x][y + i];
                board[x][y + i] = word.charAt(i);
                boardImages[x][y + i].setImageResource(getResources().getIdentifier(Character.toString(word.charAt(i)), "drawable", getPackageName()));
            }
        }
        firstTurn = false;
        refreshHand();
    }

    private boolean isOverWrite(int x, int y, char c) {
        char ch = board[x][y];
        return ch != EMPTY && ch != c;
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

                        selectTile(x, y, true);

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

    //first selection: red, second selection: yellow
    private void selectTile(int x, int y, boolean showDialog) {
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
                int lettercount = 0;
                for (int i = y; i <= coord[1]; i++) {
                    boardImages[x][i].setBackgroundResource(R.drawable.box_selected);
                    if ((board[x][i] != EMPTY || checkAdjacentTiles(x, i)) && !firstTurn) {
                        legalMove = true;
                    }
                    if (board[x][i] == EMPTY) {
                        lettercount++;
                    }
                }
                if (lettercount>8) {
                    legalMove = false;
                }
                if (legalMove && showDialog) {
                    showInputDialog(Math.abs(coord[1] - y) + 1, x, y, true);
                } else if (lettercount>7) {
                    showMessageDialog("Impossible placement! Too many letters used!");
                    clearSelectionsOnBoard();
                } else if (firstTurn && showDialog){
                    showMessageDialog("It is the first turn, you have to use the middle tile!");
                    clearSelectionsOnBoard();
                } else if (showDialog){
                    showMessageDialog("Illegal move: Doesn't connect to any letter on the board!");
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
                int lettercount = 0;
                for (int i = x; i <= coord[0]; i++) {
                    boardImages[i][y].setBackgroundResource(R.drawable.box_selected);
                    if ((board[i][y] != EMPTY || checkAdjacentTiles(i, y)) && !firstTurn) {
                        legalMove = true;
                    }
                    if (board[i][y] == EMPTY) {
                        lettercount++;
                    }
                }
                if (lettercount>7) {
                    legalMove = false;
                }
                if (legalMove && showDialog) {
                    showInputDialog(Math.abs(coord[0] - x) + 1, x, y, false);
                } else if (lettercount>7) {
                    showMessageDialog("Impossible placement! Too many letters used!");
                    clearSelectionsOnBoard();
                } else if (firstTurn && showDialog){
                    showMessageDialog("It is the first turn, you have to use the middle tile!");
                    clearSelectionsOnBoard();
                } else if (showDialog){
                    showMessageDialog("Illegal move: Doesn't connect to any letter on the board!");
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
            hand.add(handString.charAt(i));
            if (handString.charAt(i) == '.') {
                handimages.get(i).setImageResource(getResources().getIdentifier("joker", "drawable", getPackageName()));
            } else {
                handimages.get(i).setImageResource(getResources().getIdentifier(String.valueOf(handString.charAt(i)), "drawable", getPackageName()));
            }
        }
    }

    private void refreshHand() {
        for (int i = 0; i < 7; i++) {
            if (i < hand.size()) {
                if (hand.get(i) == '.') {
                    handimages.get(i).setImageResource(getResources().getIdentifier("joker", "drawable", getPackageName()));
                } else {
                    handimages.get(i).setImageResource(getResources().getIdentifier(hand.get(i).toString(), "drawable", getPackageName()));
                }

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

    private void setScore(int score) {
        TextView scoreText = (TextView) findViewById(getResources().getIdentifier("score", "id", getPackageName()));
        scoreText.setText("points: " + score);
        this.score = score;
    }

    //define dialogs
    private void showTopScoreDialog(HighScoreDTO[] highScores) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("High Scores");
        String message = new String();
        for (int i=0; i<highScores.length && i<3; i++) {
            if (highScores[i].getUser().equals(userName)) {
                message = message.concat("=> ");
            }
            message = message.concat(i + 1 + ". " + highScores[i].getUser() + " - " + highScores[i].getHighscore() + " - " + highScores[i].getDate() + "\n");
        }
        if (highScores.length>3) {
            message = message.concat("Your score: " + highScores[3].getHighscore() + " - " + highScores[3].getDate());
        }
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
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
                    showMessageDialog("This word contains illegal characters!");
                    dialog.cancel();
                } else if (word.getText().length() == 0) {
                    showMessageDialog("You didn't enter any word!");
                    dialog.cancel();
                } else {
                    httpParam = word.getText().toString();
                    if (requestController.checkLegitimacy(httpParam)) {
                        showMessageDialog(word.getText().toString() + " is a legit word");
                    } else {
                        showMessageDialog("Cannot find " + word.getText().toString() +" in the dictionary");
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDrawDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("What did you draw?");
        final EditText drawnLetters = new EditText(MainActivity.this);
        drawnLetters.setInputType(TYPE_CLASS_TEXT);
        drawnLetters.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7 - hand.size())});
        builder.setView(drawnLetters);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pattern pattern = Pattern.compile("[a-zA-Z_.]*");
                Matcher matcher = pattern.matcher(drawnLetters.getText().toString());
                if (!matcher.matches()) {
                    showMessageDialog("This contains illegal characters!");
                    dialog.cancel();
                } else if ((drawnLetters.getText().toString().length() - drawnLetters.getText().toString().replaceAll("\\.", "").length()) > 2) {
                    showMessageDialog("It is impossible to draw more than 2 jokers!");
                }  else {
                    parseHand(drawnLetters.getText().toString().toLowerCase());
                    refreshHand();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showInputDialog(final int length, final int x, final int y, final boolean across) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Word to place on board (" + length + "):");

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
                    showMessageDialog("This word is too short!");
                    dialog.cancel();
                } else if (!matcher.matches()) {
                    showMessageDialog("This word contains illegal characters!");
                } else {
                    if (requestController.checkLegitimacy(input.getText().toString().toLowerCase())) {
                        placeWord(input.getText().toString().toLowerCase(), x, y, across, false);
                        requestController.placeWord(new WordDTO(input.getText().toString().toLowerCase(), x, y, across, 0));
                    } else {
                        showIlLegitPlacementDialog(input.getText().toString().toLowerCase(), x, y, across);
                    }
                }

            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void showIlLegitPlacementDialog(final String word, final int x, final int y, final boolean across) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(word + " is not in the database. Will you still accept it?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                placeWord(word, x, y, across, false);
                requestController.placeWord(new WordDTO(word, x, y, across, 0));
            }
        });
        builder.setNegativeButton("NO", null);
        builder.show();
    }

    private void showConfirmationDialog(String message, final String action) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(message);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (action) {
                    case "endGame":
                        endGame();
                        break;
                    case "nonFullHand":
                        getBestWord();
                        break;
                    case "logout":
                        logout();
                        break;
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    private void showAdminDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Admin panel");
        builder.setNegativeButton("Back", null);
        Context context = MainActivity.this;
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 20, 10, 20);

        Button addWordButton = new Button(MainActivity.this);
        addWordButton.setText("Add new Word");
        addWordButton.setPadding(10, 20, 10, 20);
        addWordButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showAdminInputDialog("addWord");
            }
        });
        layout.addView(addWordButton);

        Button deleteWordButton = new Button(MainActivity.this);
        deleteWordButton.setText("Delete Word");
        deleteWordButton.setPadding(10, 20, 10, 20);
        deleteWordButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showAdminInputDialog("deleteWord");
            }
        });
        layout.addView(deleteWordButton);

        Button resetHighScoreButton = new Button(MainActivity.this);
        resetHighScoreButton.setText("Reset HighScore");
        resetHighScoreButton.setPadding(10, 20, 10, 20);
        resetHighScoreButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showAdminInputDialog("resetHighScore");
            }
        });
        layout.addView(resetHighScoreButton);

        Button banUserButton = new Button(MainActivity.this);
        banUserButton.setText("Ban user");
        banUserButton.setPadding(10, 20, 10, 20);
        banUserButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                showAdminInputDialog("banUser");
            }
        });
        layout.addView(banUserButton);

        builder.setView(layout);
        builder.show();
    }

    private void showAdminInputDialog(final String operation) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        switch (operation) {
            case "addWord":
                builder.setTitle("Word to add:");
                break;
            case "deleteWord":
                builder.setTitle("Word to Delete:");
                break;
            case "banUser":
                builder.setTitle("User to ban:");
                break;
            case "resetHighScore":
                builder.setTitle("User to punish:");
                break;
        }
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (input.getText().length() == 0) {
                    showMessageDialog("Input field is empty!");
                } else {
                    switch (operation) {
                        case "addWord":
                            if (requestController.addWord(input.getText().toString())) {
                                showMessageDialog("Success");
                            } else {
                                showMessageDialog("Database error! The word probably already exists.");
                            }
                            break;
                        case "deleteWord":
                            if (requestController.deleteWord(input.getText().toString())) {
                                showMessageDialog("Success");
                            } else {
                                showMessageDialog("Database Error!");
                            }
                            break;
                        case "banUser":
                            if (requestController.banUser(input.getText().toString())) {
                                showMessageDialog("Success");
                            } else {
                                showMessageDialog("Database Error!");
                            }
                            break;
                        case "resetHighScore":
                            if (requestController.resetHighScore(input.getText().toString())) {
                                showMessageDialog("Success");
                            } else {
                                showMessageDialog("Database Error! The user probably doesn't exist.");
                            }
                            break;
                    }
                }
            }

        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    //Generic message dialog, use this to show any message in a popup on the screen
    private void showMessageDialog(String errorMessage) {
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

    @Override
    public void onResume() {
        super.onResume();
        refreshHand();
    }

    private int dpToPixel(float dp){
        return (int)(dp * ((float)displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void drawBonusFields() {
        //these are used as R.drawable.$
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
    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Quitting ScrabbleHelper")
                .setMessage("Are you sure you want to exit ScrabbleHelper?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null);
        builder.show();
    }
}
