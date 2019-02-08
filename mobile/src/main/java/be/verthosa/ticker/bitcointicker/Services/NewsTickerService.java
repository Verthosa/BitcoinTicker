package be.verthosa.ticker.bitcointicker.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import org.json.JSONObject;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.Models.NewsFact;
import be.verthosa.ticker.bitcointicker.R;
import be.verthosa.ticker.bitcointicker.RestService.RestService;

public class NewsTickerService extends Service {
    private static final String TAG = NewsTickerService.class.getSimpleName();

    private PowerManager.WakeLock mWakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleIntent(Intent intent) {
        // obtain the wake lock
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bitcointicker:fornews");
        mWakeLock.acquire();

        // check the global background data setting
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm.getActiveNetworkInfo() == null) {
            stopSelf();

            return;
        }


        // do the actual work, in a separate thread
        new NewsTask().execute();
    }

    public static final String READ_ACTION = "be.verthosa.ticker.bitcointicker.MY_ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION = "be.verthosa.ticker.bitcointicker.MY_ACTION_MESSAGE_REPLY";

    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_VOICE_REPLY = "extra_voice_reply";

    public static final long timestamp = System.currentTimeMillis();

    private Intent getMessageReadIntent(int id) {
        return new Intent().setAction(READ_ACTION)
                .putExtra(CONVERSATION_ID, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(REPLY_ACTION)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void showCarNotification(String Title, String message, String url){
        Log.d(TAG, "Preparing to show  " + message);

        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                14,
                getMessageReadIntent(14),
                PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("hopla")
                .build();

        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                14,
                getMessageReplyIntent(14),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(Title)
                        .setLatestTimestamp(timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        unreadConversationBuilder.addMessage(message);


        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(url));
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_error_outline_24px)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getApplicationContext().getResources(), R.drawable.ic_error_outline_24px))
                .setContentText(message)
                .setWhen(timestamp)
                .setContentTitle(Title)
                .setContentIntent(readPendingIntent)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                /// Extend the notification with CarExtender.
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConversationBuilder.build()));

        Log.d(TAG, "Sending notification 14 conversation: " + message);

        NotificationManagerCompat.from(this)
                .notify(14, builder.build());

    }

    public class NewsTask extends AsyncTask<Void, Void, NewsFact> {
        @Override
        protected NewsFact doInBackground(Void... Param) {
            try {
                Helpers.updateTimings(getApplicationContext(), "NEWS");

                RestService restService = new RestService("https://cryptocontrol.io/api/v1/public/news?key=" + Constants.CRYPTOCONTROLKEY + "&latest=true");
                JSONObject restObject = restService.getNewsObject();

                String id = restObject.getString("_id");
                String title = restObject.getString("title");
                String description = restObject.getString("description");
                String url = restObject.getString("url");

                NewsFact fact = new NewsFact(id, title, description, url);

                return fact;
            }catch(Exception ex){
                Log.e("bitcointicker", "Error getting news");
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
                    showCarNotification(result.GetTitle(), result.GetDescription(), result.GetUrl());

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("lastnewsid", result.GetId());
                    editor.commit();
                }
            }else{
                showCarNotification(result.GetTitle(), result.GetDescription(), result.GetUrl());

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("lastnewsid", result.GetId());
                editor.commit();
            }

            stopSelf();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleIntent(intent);

    }

    /**
     * This is called on 2.0+ (API level 5 or higher). Returning * START_NOT_STICKY tells the system to not restart the service if it is * killed because of poor resource (memory/cpu) conditions.
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_NOT_STICKY;
    }

    /**
     * In onDestroy() we release our wake lock. This ensures that whenever the * Service stops (killed for resources, stopSelf() called, etc.), the wake * lock will be released.
     */

    public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }
}