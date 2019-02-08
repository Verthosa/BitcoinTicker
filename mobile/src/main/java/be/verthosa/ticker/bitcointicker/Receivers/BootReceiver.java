package be.verthosa.ticker.bitcointicker.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import be.verthosa.ticker.bitcointicker.Services.NewsTickerService;
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
        // in our case intent will always be BOOT_COMPLETED, so we can just set // the alarm //
// Note that a BroadcastReceiver is *NOT* a Context. Thus, we can't use // "this" whenever we need to pass a reference to the current context.
// Thankfully, Android will supply a valid Context as the first parameter
        _context = context;

        String savedInterval = getSharedPreference("interval");

        int interval = 1;

        if(savedInterval != null && !savedInterval.equals("")){
            interval = Integer.parseInt(savedInterval);
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent priceTickerIntent = new Intent(context, PriceTickerService.class);
        Intent newsTickerIntent = new Intent(context, NewsTickerService.class);

        PendingIntent priceTickerPendingIntent = PendingIntent.getService(context, 0, priceTickerIntent, 0);
        PendingIntent newsTickerPendingIntent = PendingIntent.getService(context, 0, newsTickerIntent, 0);

        alarmManager.cancel(priceTickerPendingIntent);
        alarmManager.cancel(newsTickerPendingIntent);

        // by my own convention, minutes <= 0 means notifications are disabled
        if (interval > 0) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval * 60 * 1000, interval * 60 * 1000, priceTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 30 * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent);

        }
    }
}