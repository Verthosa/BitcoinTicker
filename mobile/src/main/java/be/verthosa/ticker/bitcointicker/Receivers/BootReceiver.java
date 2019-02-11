package be.verthosa.ticker.bitcointicker.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.Services.NewsCryptoCompareService;
import be.verthosa.ticker.bitcointicker.Services.NewsCryptoControlService;
import be.verthosa.ticker.bitcointicker.Services.PriceTickerService;

/**
 * Created by gverthe on 8/03/2018.
 */

public class BootReceiver extends BroadcastReceiver {
    private Context _context;

    private String getSharedPreference(String name){
        SharedPreferences prefs = _context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);

        return prefs.getString(name, null);
    }

    public void onReceive(Context context, Intent intent) {
        _context = context;

        String savedInterval = getSharedPreference("interval");

        int interval = 1;

        if(savedInterval != null && !savedInterval.equals("")){
            interval = Integer.parseInt(savedInterval);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent priceTickerIntent = new Intent(context, PriceTickerService.class);
        Intent newsTickerIntent = new Intent(context, NewsCryptoControlService.class);
        Intent newsTickerIntent2 = new Intent(context, NewsCryptoCompareService.class);

        PendingIntent priceTickerPendingIntent = PendingIntent.getService(context, 0, priceTickerIntent, 0);
        PendingIntent newsTickerPendingIntent = PendingIntent.getService(context, 0, newsTickerIntent, 0);
        PendingIntent newsTickerPendingIntent2 = PendingIntent.getService(context, 0, newsTickerIntent2, 0);

        alarmManager.cancel(priceTickerPendingIntent);
        alarmManager.cancel(newsTickerPendingIntent);
        alarmManager.cancel(newsTickerPendingIntent2);

        // by my own convention, minutes <= 0 means notifications are disabled
        if (interval > 0) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval * 60 * 1000, interval * 60 * 1000, priceTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Constants.CRYPTOCONTROL_NEWS_INTERVAL * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Constants.CRYPTOCOMPARE_NEWS_INTERVAL * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent2);

            Helpers.updateTimings(_context, "ALL");
        }
    }
}