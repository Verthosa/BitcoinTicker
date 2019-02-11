package be.verthosa.ticker.bitcointicker.RestService;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class RestService {
    private String _url;

    public RestService(String url){
        _url = url;
    }

    public JSONObject getPriceObject(){
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            try {
                return new JSONObject(buffer.toString());
            } catch (JSONException e) {
                // return e.getMessage();
            }

            return null;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public JSONObject getNewsObject(){
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            try {
                JSONArray jsonArray = new JSONArray(buffer.toString());

                return jsonArray.getJSONObject(0);
            } catch (JSONException e) {
                // return e.getMessage();
            }

            return null;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public JSONObject getNews2Object(){
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            try {
                JSONObject jsonArray = new JSONObject(buffer.toString());

                return jsonArray.getJSONArray("Data").getJSONObject(0);
            } catch (JSONException e) {
                // return e.getMessage();
            }

            return null;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
