package be.verthosa.ticker.bitcointicker.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

public class MyMessageReadReceiver extends BroadcastReceiver {
    private static final String TAG = MyMessageReadReceiver.class.getSimpleName();

    private static final String CONVERSATION_ID = "conversation_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive called");
        int conversationId = intent.getIntExtra(CONVERSATION_ID, -1);
        if (conversationId != -1) {
            Log.d(TAG, "Conversation " + conversationId + " was read");
            NotificationManagerCompat.from(context).cancel(conversationId);
        }
    }
}