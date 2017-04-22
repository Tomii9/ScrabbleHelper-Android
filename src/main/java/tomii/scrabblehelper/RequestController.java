package tomii.scrabblehelper;

import android.os.AsyncTask;
import android.util.Log;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomii on 2017.04.22..
 */

public class RequestController {

    String request = new String();
    private List<String> paramNames = new ArrayList<String>();
    private List<String> params = new ArrayList<String>();

    public boolean checkLegitimacy(String word) {
        paramNames.add("?word=");
        params.add(word);
        new BooleanHttpRequestTask(paramNames, params).execute();
        return false;
    }

    private void reset() {
        request = new String();
        paramNames = new ArrayList<String>();
        params = new ArrayList<String>();
    }


    private class BooleanHttpRequestTask extends AsyncTask<Void, Void, Boolean> {

        String url = new String();

        private BooleanHttpRequestTask(List<String> paramNames, List<String> params){
            url = "http://192.168.43.123:8080/";

            for (int i=0; i<params.size(); i++) {
                url = url+paramNames.get(i)+params.get(i);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                String exists = restTemplate.getForObject(url, String.class);
                return exists.equals("true");
            } catch (Exception e) {
                Log.e("RequestController", e.getMessage(), e);
            }
            return null;
        }

    }

}

