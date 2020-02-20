package be.verthosa.ticker.bitcointicker;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import be.verthosa.ticker.bitcointicker.Services.NewsCryptoCompareService;
import be.verthosa.ticker.bitcointicker.Services.NewsCryptoControlService;
import be.verthosa.ticker.bitcointicker.Services.PriceTickerService;

public class MainActivity extends AppCompatActivity {

    private Context _context;
    private TextView txtInterval;

    public void setAlarms(){
        int interval = Helpers.getInterval(_context);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent priceTickerIntent = new Intent(this, PriceTickerService.class);
        Intent newsTickerIntent = new Intent(this, NewsCryptoControlService.class);
        Intent newsTickerIntent2 = new Intent(this, NewsCryptoCompareService.class);

        PendingIntent priceTickerPendingIntent = PendingIntent.getService(this, 0, priceTickerIntent, 0);
        PendingIntent newsTickerPendingIntent = PendingIntent.getService(this, 0, newsTickerIntent, 0);
        PendingIntent newsTickerPendingIntent2 = PendingIntent.getService(this, 0, newsTickerIntent2, 0);

        if(alarmManager != null){
            alarmManager.cancel(priceTickerPendingIntent);
            alarmManager.cancel(newsTickerPendingIntent);
            alarmManager.cancel(newsTickerPendingIntent2);
        }

        // by my own convention, minutes <= 0 means notifications are disabled
        if (interval > 0 && alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval * 60 * 1000, interval * 60 * 1000, priceTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Constants.CRYPTOCONTROL_NEWS_INTERVAL * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + Constants.CRYPTOCOMPARE_NEWS_INTERVAL * 60 * 1000, interval * 60 * 1000, newsTickerPendingIntent2);

            Helpers.updateTimings(_context, "ALL");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setLogo(R.drawable.ic_outline_show_chart_24px);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        _context = getApplicationContext();

        setSpinners();
        addListenerOnButton();
        addListenersTestButtons();

        setAlarms();
        setTimings();
    }

    private void checkPermission(String Permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Permission);
        if (permissionCheck != 1) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Permission)) {
               showExplanation("Permission Needed", "Rationale", Permission, 4141);
            } else {
                requestPermission(Permission, 4141);
            }
        } else {
            Toast.makeText(MainActivity.this, "Permission (already) Granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (requestCode == 4141) {
            if (grantResults.length > 0 && grantResults[0] == 1) {
                Toast.makeText(MainActivity.this, "Permission OK", Toast.LENGTH_SHORT).show();
            }
        }
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

    final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            txtInterval = findViewById(R.id.txtInterval);
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

    public void addListenersTestButtons(){
        Button btnTestPrice = findViewById(R.id.btnTestPrice);
        Button btnTestNews = findViewById(R.id.btnTestNews);
        Button btnTestNews2 = findViewById(R.id.btnTestNews2);

        btnTestPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(getApplicationContext(), PriceTickerService.class);
                getApplicationContext().startService(serviceIntent);
            }
            });

        btnTestNews.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.remove("lastnewsid");
                    editor.apply();

                    Intent serviceIntent = new Intent(getApplicationContext(), NewsCryptoControlService.class);
                    getApplicationContext().startService(serviceIntent);
               }
                });

        btnTestNews2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("be.verthosa.ticker", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                editor.remove("lastcomparenewsid");
                editor.apply();


                Intent serviceIntent2 = new Intent(getApplicationContext(), NewsCryptoCompareService.class);
                getApplicationContext().startService(serviceIntent2);
            }
        });
    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        cryptos = findViewById(R.id.cryptoSpinner);
        fiats = findViewById(R.id.fiatSpinner);
        btnSubmit = findViewById(R.id.saveSettings);


        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // checkPermission(Manifest.permission.INTERNET);

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

                if(prefs.contains("lastnewsid")){
                    editor.remove("lastnewsid");
                }

                if(prefs.contains("lastcomparenewsid")){
                    editor.remove("lastcomparenewsid");
                }

                editor.apply();

                CharSequence text = "Settings saved...";
                int duration = Toast.LENGTH_SHORT;

                setAlarms();
                setTimings();

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });
    }
}

