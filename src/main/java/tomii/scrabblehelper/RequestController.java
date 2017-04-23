package tomii.scrabblehelper;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.StringHttpMessageConverter;
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

            System.out.println(url);
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

}

