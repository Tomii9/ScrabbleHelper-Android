package tomii.scrabblehelper;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Tomii on 2017.04.22..
 */

public class RequestController {

    private String request = new String();
    private boolean response = false;
    private String operation = new String();
    private List<String> paramNames = new ArrayList<String>();
    private List<String> params = new ArrayList<String>();
    HighScoreDTO[] highScores;
    HighScoreDTO ownHighScore;
    WordDTO bestWord;

    public HighScoreDTO[] getHighScores() {

        try {
            new Top3HttpRequestTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for (int i=0; i<highScores.length; i++) {
            System.out.println(highScores[i].getUser());
        }

        return highScores;
    }

    public HighScoreDTO getOwnHighScore() {
        try {
            new HighScoreHttpRequestTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return ownHighScore;
    }

    public boolean setHighScore(int score) {

        operation = "sethighscore";
        paramNames.add("score");
        params.add(String.valueOf(score));
        try {
            new BooleanHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    public WordDTO getBestWord(String hand) {

        setHand(hand);
        try {
            new BestWordHttpRequestTask().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(bestWord.getWord());
        bestWord.setDown(!bestWord.isDown());
        return bestWord;
    }

    public boolean endGame() {

        operation = "resetboard";
        try {
            new BooleanHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    public boolean refreshCache() {
        operation = "refreshcache";
        try {
            new BooleanHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    public boolean checkLegitimacy(String word) {
        operation = "checklegitimacy";
        paramNames.add("word");
        params.add(word);
        try {
            new BooleanHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    public boolean placeWord(WordDTO word) {
        if (checkLegitimacy(word.getWord())) {
            operation = "placeword";
            paramNames.add("word");
            paramNames.add("x");
            paramNames.add("y");
            paramNames.add("down");
            params.add(word.getWord());
            params.add(String.valueOf(word.getX()));
            params.add(String.valueOf(word.getY()));
            params.add(String.valueOf(!word.isDown()));
            try {
                new BooleanHttpRequestTask(paramNames, params).execute().get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    public boolean setHand(String hand) {

        operation = "sethand";
        paramNames.add("hand");
        params.add(hand);
        try {
            new BooleanHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean tempResponse = response;
        reset();
        return tempResponse;
    }

    private void reset() {
        request = new String();
        response = false;
        operation = new String();
        paramNames = new ArrayList<String>();
        params = new ArrayList<String>();
    }


    private class BooleanHttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private BooleanHttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://midori:8080/" + operation;

            for (int i=0; i<params.size(); i++) {
                if (i==0) {
                    url = url+"?"+paramNames.get(i)+"="+params.get(i);
                } else {
                    url = url+"&"+paramNames.get(i)+"="+params.get(i);
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String exists = restTemplate.getForObject(url, String.class);
                response = exists.equals("true");
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }

    private class BestWordHttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private BestWordHttpRequestTask(){

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                url = "http://midori:8080/getbestword";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                bestWord = restTemplate.getForObject(url, WordDTO.class);
                return true;
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }

    private class Top3HttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private Top3HttpRequestTask(){

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                url = "http://midori:8080/gettopscores";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                highScores = restTemplate.getForObject(url, HighScoreDTO[].class);
                return true;
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }

    private class HighScoreHttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private HighScoreHttpRequestTask(){

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                url = "http://midori:8080/getownhighscore";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                ownHighScore = restTemplate.getForObject(url, HighScoreDTO.class);
                return true;
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }
}

