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

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.RestService.RestService;

public class PriceTickerService extends Service {
    private PowerManager.WakeLock mWakeLock;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        // obtain the wake lock
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bitcointicker:forprice");
        mWakeLock.acquire();

        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() == null) {
            stopSelf();

            return;
        }

        // do the actual work, in a separate thread
        new PriceTask().execute();
    }

    private class PriceTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                Context context = getApplicationContext();

                Helpers.updateTimings(getApplicationContext(), "PRICE");

                SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);

                String crypto = prefs.getString("crypto", null);
                String fiat = prefs.getString("fiat", null);

                String restUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?CMC_PRO_API_KEY=" + Constants.CMCKEY + "&symbol=BTC&convert=EUR";

                if (crypto != null && fiat != null) {
                    restUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?CMC_PRO_API_KEY=ccf26a87-6f88-4239-92d1-037b48dc8d15&symbol=" + crypto.toUpperCase() + "&convert=" + fiat.toUpperCase();
                }

                 RestService restService = new RestService(restUrl);
                JSONObject priceObj = restService.getPriceObject();

                String price = priceObj.getJSONObject("data").getJSONObject(crypto.toUpperCase()).getJSONObject("quote").getJSONObject(fiat.toUpperCase()).getString("price");

                return price;
            }catch(Exception ex){
                Log.e("bitcointicker", "Error getting prices");
                Log.e("bitcointicker", ex.getMessage());
                return null;
            }
        }

        double round(double value, int places) {
            if (places < 0) throw new IllegalArgumentException();

            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }

        @Override
        protected void onPostExecute(String strPrice) {
            Context context = getApplicationContext();
            SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);

            String crypto = prefs.getString("crypto", null);
            String fiat = prefs.getString("fiat", null);
            String perc = prefs.getString("percentage", null);

            double limitPercentage = round(Double.parseDouble(perc), 2);
            double negativeLimitPercentage = limitPercentage *-1;

            double price = Double.parseDouble(strPrice);
            BigDecimal bd = new BigDecimal(price);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            price = bd.doubleValue();

            if(prefs.contains("lastprice")){
            String lastPrice = prefs.getString("lastprice", null);

            double lastPriceDouble = Double.parseDouble(lastPrice);
            BigDecimal bd1 = new BigDecimal(lastPriceDouble);
            bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
            lastPriceDouble = bd1.doubleValue();

                double change = ((price - lastPriceDouble) / lastPriceDouble);

                BigDecimal bd2 = new BigDecimal(change);
                bd2 = bd2.setScale(4, RoundingMode.HALF_UP);
                change = bd2.doubleValue();

                double percent = round(change*100, 2);

                if(percent > limitPercentage || percent < negativeLimitPercentage){
                    String notificationTitle = crypto + "/" + fiat + " " + percent + "% " + price;
                    String notificationText = crypto + " is now worth " + price + " " + fiat;

                    Helpers.showPriceNotification(context, notificationTitle, notificationText, percent > 0);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastprice", price + "");
                    editor.commit();
                }
            }else{
                String notificationTitle = crypto + "/" + fiat + " " + price;
                String notificationText = crypto + " is now worth " + price + " " + fiat;

                Helpers.showPriceNotification(context, notificationTitle, notificationText, null);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastprice", price + "");
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