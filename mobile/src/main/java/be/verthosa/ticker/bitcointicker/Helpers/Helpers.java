package be.verthosa.ticker.bitcointicker.Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import java.util.Date;
import java.util.Random;

import be.verthosa.ticker.bitcointicker.R;

public class Helpers {
    public static String getSharedPreference(Context context, String name){
        SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
        return prefs.getString(name, null);
    }

    public static int getInterval(Context context){
        String savedInterval = getSharedPreference(context,"interval");

        int interval = Constants.DEFAULT_PRICE_INTERVAL;

        if(savedInterval != null && !savedInterval.equals("")){
            interval = Integer.parseInt(savedInterval);
        }

        return interval;
    }

    private static Intent getMessageReadIntent(int id) {
        return new Intent().setAction(Constants.READ_ACTION)
                .putExtra(Constants.CONVERSATION_ID, id);
    }

    // Creates an Intent that will be triggered when a voice reply is received.
    private static Intent getMessageReplyIntent(int conversationId) {
        return new Intent().setAction(Constants.REPLY_ACTION)
                .putExtra(Constants.CONVERSATION_ID, conversationId);
    }

    public static void showNewsNotification(Context context, String title, String message, String url){
        Random random = new Random();

        showCarNotification(context, title, message, url, random.nextInt(), BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ic_outline_notification_important_24px), R.drawable.ic_outline_notification_important_24px);
    }

    public static void showPriceNotification(Context context, String title, String message, Boolean isAscending){
        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), isAscending == null ? R.drawable.ic_outline_grade_24px : isAscending ? R.drawable.ic_outline_trending_up_24px : R.drawable.ic_outline_trending_down_24px);
        int iconId = isAscending == null ? R.drawable.ic_outline_grade_24px : isAscending ? R.drawable.ic_outline_trending_up_24px : R.drawable.ic_outline_trending_down_24px;

        showCarNotification(context, title, message, "", 9876, icon, iconId);
    }

    private static void showCarNotification(Context context, String Title, String message, String url, int uid, Bitmap icon, int iconId){
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(context,
                uid,
                getMessageReadIntent(uid),
                PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteInput remoteInput = new RemoteInput.Builder(Constants.EXTRA_VOICE_REPLY)
                .setLabel("carnotification")
                .build();

        PendingIntent replyIntent = PendingIntent.getBroadcast(context,
                uid,
                getMessageReplyIntent(uid),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(Title)
                        .setLatestTimestamp(Constants.timestamp)
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        unreadConversationBuilder.addMessage(message);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Car Notifier Notifications Channel")
                .setSmallIcon(iconId)
                .setLargeIcon(icon)
                .setContentText(message)
                .setWhen(Constants.timestamp)
                .setContentTitle(Title)
                .setContentIntent(readPendingIntent)
                .setAutoCancel(true)
                /// Extend the notification with CarExtender.
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConversationBuilder.build()));

        if(!url.equals("")){
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(url));
            PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            builder.setContentIntent(pi);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        NotificationManagerCompat.from(context)
                .notify(uid, builder.build());

    }

    public static void updateTimings(Context context, String which){
        int interval = Helpers.getInterval(context);

        Date nextPricePolling =  new Date( System.currentTimeMillis() + interval * 60 * 1000);
        Date nextNewsPolling = new Date( System.currentTimeMillis() + Constants.DEFAULT_NEWS_INTERVAL * 60 * 1000);

        SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        switch (which.toUpperCase()) {
            case "ALL":
                editor.putString("nextpricepolling", nextPricePolling.toString());
                editor.putString("nextnewspolling", nextNewsPolling.toString());
                break;
            case "PRICE":
                editor.putString("nextpricepolling", nextPricePolling.toString());
                break;
            case "NEWS":
                editor.putString("nextnewspolling", nextNewsPolling.toString());
                break;
        }

        editor.commit();
    }
}
