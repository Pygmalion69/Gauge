package de.nitri.gaugedemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.nitri.gauge.Gauge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Gauge gauge = (Gauge) findViewById(R.id.gauge);
        gauge.moveToValue(800);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               gauge.moveToValue(300);
            }
        }, 2800);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gauge.moveToValue(550);
            }
        }, 5600);
    }
}
