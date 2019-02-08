package be.verthosa.ticker.bitcointicker.Receivers;/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import be.verthosa.ticker.bitcointicker.Services.PriceTickerService;

/**
 * A receiver that gets called when a reply is sent to a given conversationId
 */
public class MyMessageReplyReceiver extends BroadcastReceiver {

    private static final String TAG = PriceTickerService.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (PriceTickerService.REPLY_ACTION.equals(intent.getAction())) {
            int conversationId = intent.getIntExtra(PriceTickerService.CONVERSATION_ID, -1);
            CharSequence reply = getMessageText(intent);
            if (conversationId != -1) {
                Log.d(TAG, "Got reply (" + reply + ") for ConversationId " + conversationId);
            }
        }
    }

    /**
     * Get the message text from the intent.
     * Note that you should call {@code RemoteInput#getResultsFromIntent(intent)} to process
     * the RemoteInput.
     */
    private CharSequence getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getCharSequence(PriceTickerService.EXTRA_VOICE_REPLY);
        }
        return null;
    }
}