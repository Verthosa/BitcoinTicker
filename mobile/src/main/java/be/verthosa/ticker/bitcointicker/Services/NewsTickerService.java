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

import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.Models.NewsFact;
import be.verthosa.ticker.bitcointicker.R;
import be.verthosa.ticker.bitcointicker.RestService.RestService;

public class NewsTickerService extends Service {
    private static final String TAG = NewsTickerService.class.getSimpleName();

    private PowerManager.WakeLock mWakeLock;
    /**
     * Simply return null, since our Service will not be communicating with * any other components. It just does its work silently.
     */

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * This is where we initialize. We call this when onStart/onStartCommand is * called by the system. We won't do anything with the intent here, and you * probably won't, either.
     */

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
        // new PollTask().execute();
        new NewsTask().execute();
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

    // Creates an Intent that will be triggered when a voice reply is received.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(REPLY_ACTION)
                .putExtra(CONVERSATION_ID, conversationId);
    }

    private void showCarNotification(String Title, String message, String url){
        Log.d(TAG, "Preparing to show  " + message);

        // A pending Intent for reads
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                14,
                getMessageReadIntent(14),
                PendingIntent.FLAG_UPDATE_CURRENT);

        /// Add the code to create the UnreadConversation

        // Build a RemoteInput for receiving voice input in a Car Notification
        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_VOICE_REPLY)
                .setLabel("hopla")
                .build();

        // Building a Pending Intent for the reply action to trigger
        PendingIntent replyIntent = PendingIntent.getBroadcast(getApplicationContext(),
                14,
                getMessageReplyIntent(14),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the UnreadConversation and populate it with the participant name,
        // read and reply intents.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(Title)
                        .setLatestTimestamp(timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        // Note: Add messages from oldest to newest to the UnreadConversation.Builder
        // Since we are sending a single message here we simply add the message.
        // In a real world application there could be multiple messages which should be ordered
        // and added from oldest to newest.
        unreadConversationBuilder.addMessage(message);

        /// End create UnreadConversation

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
        /// End

        Log.d(TAG, "Sending notification 14 conversation: " + message);

        NotificationManagerCompat.from(this)
                .notify(14, builder.build());

    }

    public class NewsTask extends AsyncTask<Void, Void, NewsFact> {
        /**
         * This is where YOU do YOUR work. There's nothing for me to write here * you have to fill this in. Make your HTTP request(s) or whatever it is * you have to do to get your updates in
         * here, because this is run in a * separate thread
         */
        @Override
        protected NewsFact doInBackground(Void... Param) {
            try {
                Helpers.updateTimings(getApplicationContext(), "NEWS");

                RestService restService = new RestService("https://cryptocontrol.io/api/v1/public/news?key=a1520f97fcd1c5b78318f2a2500bfb8d&latest=true");
                JSONObject restObject = restService.getJSONObject();

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
            // handle your data
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