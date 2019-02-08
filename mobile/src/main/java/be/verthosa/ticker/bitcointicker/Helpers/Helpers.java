package be.verthosa.ticker.bitcointicker.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class Helpers {
    public static String getSharedPreference(Context context, String name){
        SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
        return prefs.getString(name, null);
    }

    public static int getInterval(Context context){
        String savedInterval = getSharedPreference(context,"interval");

        int interval = 1;

        if(savedInterval != null && !savedInterval.equals("")){
            interval = Integer.parseInt(savedInterval);
        }

        return interval;
    }

    public static void updateTimings(Context context, String which){
        int interval = Helpers.getInterval(context);

        Date nextPricePolling =  new Date( System.currentTimeMillis() + interval * 60 * 1000);
        Date nextNewsPolling = new Date( System.currentTimeMillis() + 30 * 60 * 1000);

        SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if(which.toUpperCase().equals("ALL")){
            editor.putString("nextpricepolling", nextPricePolling.toString());
            editor.putString("nextnewspolling", nextNewsPolling.toString());
        }else if(which.toUpperCase().equals("PRICE")){
            editor.putString("nextpricepolling", nextPricePolling.toString());
        }else if(which.toUpperCase().equals("NEWS")){
            editor.putString("nextnewspolling", nextNewsPolling.toString());
        }

        editor.commit();
    }
}
