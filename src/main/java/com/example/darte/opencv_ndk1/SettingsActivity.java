package com.example.darte.opencv_ndk1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;

/**
 * Created by marcin on 08.04.18.
 */

public class SettingsActivity extends Activity {

    private Button button;
    private int sensity;
    private int sensityMax;
    private int alarmLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SeekBar sensitySeekBar = findViewById(R.id.sensitySeekBar);
        sensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensity = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar timeSeekBar = findViewById(R.id.timeSeekBar);
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                alarmLength = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        button = findViewById(R.id.button);
        button.setOnClickListener((v) -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.putExtra("sensity", sensity);
            intent.putExtra("sensityMax", sensitySeekBar.getMax());
            intent.putExtra("alarmLength", alarmLength);
            startActivity(intent);
        });

    }
}
