package be.verthosa.ticker.bitcointicker.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import org.json.JSONObject;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.Models.NewsFact;
import be.verthosa.ticker.bitcointicker.RestService.RestService;

public class NewsCryptoControlService extends Service {
    private PowerManager.WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bitcointicker:fornews");
        mWakeLock.acquire();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() == null) {
            stopSelf();

            return;
        }

        new NewsTask().execute();
    }


    public class NewsTask extends AsyncTask<Void, Void, NewsFact> {
        @Override
        protected NewsFact doInBackground(Void... Param) {
            try {
                Helpers.updateTimings(getApplicationContext(), "NEWS");

                RestService restService = new RestService("https://cryptocontrol.io/api/v1/public/news?key=" + Constants.CRYPTOCONTROLKEY + "&latest=true");
                JSONObject restObject = restService.getCryptoControlNewsObject();

                String id = restObject.getString("_id");
                String title = restObject.getString("title");
                String description = restObject.getString("description");
                String url = restObject.getString("url");

                NewsFact fact = new NewsFact(id, title, description, url);

                return fact;
            }catch(Exception ex){
                Log.e("bitcointicker", "Error getting news from crypto control.");
                Log.e("bitcointicker", ex.getMessage());
                return null;
            }
        }


        @Override
        protected void onPostExecute(NewsFact result) {
            Context context = getApplicationContext();

            SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);

            if(prefs.contains("lastnewsid")){
                String lastid = prefs.getString("lastnewsid", null);

                if(!lastid.equals(result.GetId())){
                    Helpers.showNewsNotification(getApplicationContext(), result.GetTitle(), result.GetDescription(), result.GetUrl());

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastnewsid", result.GetId());
                    editor.commit();
                }
            }else{
                Helpers.showNewsNotification(getApplicationContext(), result.GetTitle(), result.GetDescription(), result.GetUrl());

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastnewsid", result.GetId());
                editor.commit();
            }

            stopSelf();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }
}