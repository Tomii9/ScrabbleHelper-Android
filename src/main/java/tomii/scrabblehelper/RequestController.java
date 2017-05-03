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

    private String token = new String();
    private boolean response = false;
    private String operation = new String();
    private List<String> paramNames = new ArrayList<String>();
    private List<String> params = new ArrayList<String>();
    private HighScoreDTO[] highScores;
    private HighScoreDTO ownHighScore;
    private WordDTO bestWord;
    private SessionDTO sessionDTO;
    private String server;

    public RequestController(String token, String server) {
        this.token = token;
        this.server = server;
    }

    public HighScoreDTO[] getHighScores() {
        operation = "gettopscores";
        paramNames.add("token");
        params.add(token);
        try {
            new Top3HttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        for (int i=0; i<highScores.length; i++) {
            System.out.println(highScores[i].getUser());
        }
        reset();
        return highScores;
    }

    public HighScoreDTO getOwnHighScore() {
        operation = "getownhighscore";
        paramNames.add("token");
        params.add(token);
        try {
            new HighScoreHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        reset();
        return ownHighScore;
    }

    public boolean setHighScore(int score) {

        operation = "sethighscore";
        paramNames.add("score");
        paramNames.add("token");
        params.add(String.valueOf(score));
        params.add(token);
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
        operation = "getbestword";
        paramNames.add("token");
        params.add(token);
        try {
            new BestWordHttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println(bestWord.getWord());
        bestWord.setDown(!bestWord.isDown());
        reset();
        return bestWord;
    }

    public boolean endGame() {

        operation = "resetboard";
        paramNames.add("token");
        params.add(token);
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
        paramNames.add("token");
        params.add(token);
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
        paramNames.add("token");
        params.add(word);
        params.add(token);
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
            paramNames.add("token");
            params.add(token);
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
        paramNames.add("token");
        params.add(hand);
        params.add(token);
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

    public SessionDTO login(String userName, String password) {
        operation = "login";
        paramNames.add("user");
        paramNames.add("password");
        params.add(userName);
        params.add(password);
        try {
            new AuthenticationHTttpRequestTask(paramNames, params).execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        reset();
        return sessionDTO;
    }

    public boolean logout() {
        operation = "logout";
        paramNames.add("token");
        params.add(token);
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

    public boolean register(String username, String password) {
        operation = "register";
        paramNames.add("user");
        paramNames.add("password");
        params.add(username);
        params.add(password);
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

    public boolean deleteWord(String word) {
        operation = "admin/deleteword";
        paramNames.add("word");
        paramNames.add("token");
        params.add(word);
        params.add(token);
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

    public boolean addWord(String word) {
        operation = "admin/banuser";
        paramNames.add("word");
        paramNames.add("token");
        params.add(word);
        params.add(token);
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

    public boolean resetHighScore(String user) {
        operation = "admin/resethighscore";
        paramNames.add("user");
        paramNames.add("token");
        params.add(user);
        params.add(token);
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

    public boolean banUser(String user) {
        operation = "admin/banuser";
        paramNames.add("user");
        paramNames.add("token");
        params.add(user);
        params.add(token);
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
        response = false;
        operation = new String();
        paramNames = new ArrayList<String>();
        params = new ArrayList<String>();
    }


    private class BooleanHttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private BooleanHttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://" + server + ":8080/" + operation;

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

        private BestWordHttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://" + server + ":8080/" + operation;

            for (int i=0; i<params.size(); i++) {
                if (i==0) {
                    url = url+"?"+paramNames.get(i)+"="+params.get(i);
                } else {
                    url = url+"&"+paramNames.get(i)+"="+params.get(i);
                }
            }
            System.out.println(url);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
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

        private Top3HttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://" + server + ":8080/" + operation;

            for (int i=0; i<params.size(); i++) {
                if (i==0) {
                    url = url+"?"+paramNames.get(i)+"="+params.get(i);
                } else {
                    url = url+"&"+paramNames.get(i)+"="+params.get(i);
                }
            }
            System.out.println(url);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
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

        private HighScoreHttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://" + server + ":8080/" + operation;

            for (int i=0; i<params.size(); i++) {
                if (i==0) {
                    url = url+"?"+paramNames.get(i)+"="+params.get(i);
                } else {
                    url = url+"&"+paramNames.get(i)+"="+params.get(i);
                }
            }
            System.out.println(url);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
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

    private class AuthenticationHTttpRequestTask extends AsyncTask<Void, Void, Boolean> {
        String url = new String();

        private AuthenticationHTttpRequestTask(List<String> paramNames, List<String> params) {
            url = "http://" + server + ":8080/" + operation;

            for (int i=0; i<params.size(); i++) {
                if (i==0) {
                    url = url+"?"+paramNames.get(i)+"="+params.get(i);
                } else {
                    url = url+"&"+paramNames.get(i)+"="+params.get(i);
                }
            }
            System.out.println(url);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                sessionDTO = restTemplate.getForObject(url, SessionDTO.class);
                return true;
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }
}

