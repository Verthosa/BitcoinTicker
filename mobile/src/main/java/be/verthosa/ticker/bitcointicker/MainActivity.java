package be.verthosa.ticker.bitcointicker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import be.verthosa.ticker.bitcointicker.Helpers.Constants;
import be.verthosa.ticker.bitcointicker.Helpers.Helpers;
import be.verthosa.ticker.bitcointicker.Services.NewsTickerService;
import be.verthosa.ticker.bitcointicker.Services.PriceTickerService;

public class MainActivity extends AppCompatActivity {

    private Context _context;
    private TextView txtInterval;

    public void setAlarms(){
        int interval = Helpers.getInterval(_context);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent priceTickerIntent = new Intent(this, PriceTickerService.class);
        Intent newsTickerIntent = new Intent(this, NewsTickerService.class);

        PendingIntent priceTickerPendingIntent = PendingIntent.getService(this, 0, priceTickerIntent, 0);
        PendingIntent newsTickerPendingIntent = PendingIntent.getService(this, 0, newsTickerIntent, 0);

        alarmManager.cancel(priceTickerPendingIntent);
        alarmManager.cancel(newsTickerPendingIntent);

        // by my own convention, minutes <= 0 means notifications are disabled
        if (interval > 0) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval * 60 * 1000, interval * 60 * 1000, priceTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Constants.DEFAULT_NEWS_INTERVAL * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent);

            Helpers.updateTimings(_context, "ALL");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_outline_show_chart_24px);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        _context = getApplicationContext();

        setSpinners();
        addListenerOnButton();

        setAlarms();
        setTimings();
    }


    private static boolean isSet(String param) {
        // doesn't ignore spaces, but does save an object creation.
        return param != null && param.length() != 0;
    }

    public void setTimings(){
        String nextPricePolling = Helpers.getSharedPreference(_context, "nextpricepolling");
        String nextNewsPolling = Helpers.getSharedPreference(_context,"nextnewspolling");

        if(isSet(nextPricePolling)){
            TextView lblPricePolling = findViewById(R.id.lblPricePolling);
            lblPricePolling.setText(nextPricePolling);
        }

        if(isSet(nextNewsPolling)) {
            TextView lblNewsPolling = findViewById(R.id.lblNewsPolling);
            lblNewsPolling.setText(nextNewsPolling);
        }
    }
    public void onResume() {
        super.onResume();

        setTimings();
    }

    private Spinner cryptos, fiats;
    private Button btnSubmit;

    public void setSpinners(){
        // set a change listener on the SeekBar
        SeekBar seekBar = findViewById(R.id.intervalSeekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        String crypto = Helpers.getSharedPreference(_context,"crypto");
        String fiat = Helpers.getSharedPreference(_context,"fiat");
        String interval = Helpers.getSharedPreference(_context,"interval");
        String percentage = Helpers.getSharedPreference(_context,"percentage");

        if(crypto != null && fiat != null){
            String[] cryptoArray = getResources().getStringArray(R.array.crypto_array);
            String[] fiatArray = getResources().getStringArray(R.array.fiat_array);

            cryptos = findViewById(R.id.cryptoSpinner);
            fiats = findViewById(R.id.fiatSpinner);
            txtInterval = findViewById(R.id.txtInterval);
            EditText inputPercentage = findViewById(R.id.inputPercentage);

            cryptos.setSelection(Arrays.asList(cryptoArray).indexOf(crypto));
            fiats.setSelection(Arrays.asList(fiatArray).indexOf(fiat));

            inputPercentage.setText(percentage);

            seekBar.setProgress(Integer.parseInt(interval));
            txtInterval.setText(interval);
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            txtInterval.setText(seekBar.getProgress() + "");
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    // get the selected dropdown list value
    public void addListenerOnButton() {

        cryptos = findViewById(R.id.cryptoSpinner);
        fiats = findViewById(R.id.fiatSpinner);
        btnSubmit = findViewById(R.id.saveSettings);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Take action.
                Context context = getApplicationContext();

                SharedPreferences prefs = context.getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                String interval = ((TextView)findViewById(R.id.txtInterval)).getText().toString();
                String percentage = ((EditText)findViewById(R.id.inputPercentage)).getText().toString();

                editor.putString("crypto", cryptos.getSelectedItem().toString());
                editor.putString("fiat", fiats.getSelectedItem().toString());
                editor.putString("percentage", percentage);
                editor.putString("interval", interval);

                // remove last price.
                if(prefs.contains("lastprice")){
                    editor.remove("lastprice");
                }

                editor.commit();

                CharSequence text = "Saved...";
                int duration = Toast.LENGTH_SHORT;

                setAlarms();
                setTimings();

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }
}

