package be.verthosa.ticker.bitcointicker.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.RemoteInput;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Services.PriceTickerService;

public class MyMessageReplyReceiver extends BroadcastReceiver {

    private static final String TAG = PriceTickerService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.REPLY_ACTION.equals(intent.getAction())) {
            int conversationId = intent.getIntExtra(Constants.CONVERSATION_ID, -1);
            CharSequence reply = getMessageText(intent);
            if (conversationId != -1) {
                Log.d(TAG, "Got reply (" + reply + ") for ConversationId " + conversationId);
            }
        }
    }

    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(Constants.EXTRA_VOICE_REPLY);
        }
        return null;
    }
}
