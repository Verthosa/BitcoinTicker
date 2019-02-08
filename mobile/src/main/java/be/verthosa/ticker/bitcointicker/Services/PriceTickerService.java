package be.verthosa.ticker.bitcointicker.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;

import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.R;
import be.verthosa.ticker.bitcointicker.RestService.RestService;

public class PriceTickerService extends Service {
    private static final String TAG = PriceTickerService.class.getSimpleName();

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

    /**
     * The Action that indicates that a new message notification
     * should be sent by this Service.
     */
    public static final String READ_ACTION =
            "be.verthosa.ticker.bitcointicker.MY_ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION =
            "be.verthosa.ticker.bitcointicker.MY_ACTION_MESSAGE_REPLY";

    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    public static final long timestamp = System.currentTimeMillis();

    private Intent getMessageReadIntent(int id) {
        return new Intent().setAction(READ_ACTION)
                .putExtra(CONVERSATION_ID, id);
    }

    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(REPLY_ACTION)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void showCarNotification(String Title, String message, Boolean isAscending){
        Log.d(TAG, "Preparing to show  " + message);

        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                13,
                getMessageReadIntent(13),
                PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("hopla")
                .build();

        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                13,
                getMessageReplyIntent(13),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(Title)
                        .setLatestTimestamp(timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        unreadConversationBuilder.addMessage(message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(isAscending == null ? R.drawable.ic_outline_show_chart_24px : isAscending ? R.drawable.ic_outline_trending_up_24px : R.drawable.ic_outline_trending_down_24px)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), isAscending == null ? R.drawable.ic_outline_show_chart_24px : isAscending ? R.drawable.ic_outline_trending_up_24px : R.drawable.ic_outline_trending_down_24px))
                .setContentText(message)
                .setWhen(timestamp)
                .setContentTitle(Title)
                .setContentIntent(readPendingIntent)
                /// Extend the notification with CarExtender.
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConversationBuilder.build()));

        NotificationManagerCompat.from(this)
                .notify(13, builder.build());

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

                // MODIFY TO https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest?CMC_PRO_API_KEY=ccf26a87-6f88-4239-92d1-037b48dc8d15&symbol=BTC&convert=EUR
                String restUrl = "https://api.coinmarketcap.com/v1/ticker/bitcoin/?convert=EUR";
                String propertyToRead = "price_usd";

                if (crypto != null && fiat != null) {
                    restUrl = "https://api.coinmarketcap.com/v1/ticker/" + crypto + "/?convert=" + fiat;
                    propertyToRead = "price_" + fiat.toLowerCase();
                }

                RestService restService = new RestService(restUrl);
                JSONObject restObject = restService.getJSONObject();

                return restObject.getString(propertyToRead);
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
                    String notificationTitle = crypto + ": " + percent + "% :: " + fiat + price;
                    String notificationText = crypto + " is now worth " + price + " " + fiat;

                    showCarNotification(notificationTitle, notificationText, percent > 0);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastprice", price + "");
                    editor.commit();
                }
            }else{
                String notificationTitle = crypto + ": " + fiat + price;
                String notificationText = crypto + " is now worth " + price + " " + fiat;

                showCarNotification(notificationTitle, notificationText, null);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastprice", price + "");
                editor.commit();
            }

            stopSelf();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);
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