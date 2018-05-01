package com.example.darte.opencv_ndk1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by marcin on 08.04.18.
 */

public class SettingsActivity extends Activity {

    private Button button;
    private Spinner spinner;
    private int sensibility;
    private int alarmLength;

    private void CopyRAWtoSDCard(int id, String path) throws IOException {
        InputStream in = getResources().openRawResource(id);
        FileOutputStream out = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        final int[] cascades = new int[] { R.raw.haar_closed_eye, R.raw.lbpcascade_frontalface_improved, R.raw.haarcascade_eye_tree_eyeglasses, R.raw.haar_closed_eye_improved};
        final String[] cascadesString = new String[] { "haar_closed_eye.xml", "lbpcascade_frontalface_improved.xml", "haarcascade_eye_tree_eyeglasses.xml", "haar_closed_eye_improved.xml"};
        for (int i = 0; i < cascades.length; i++) {
            try {
                String path = Environment.getExternalStorageDirectory() + "/Cascades";
                File dir = new File(path);
                if (dir.mkdirs() || dir.isDirectory()) {
                    String strCascadeName = cascadesString[i];
                    CopyRAWtoSDCard(cascades[i], path + File.separator + strCascadeName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        TextView textSensibility = (TextView) findViewById(R.id.textSensity);
        TextView textLength = (TextView) findViewById(R.id.textLength);

        // init values
        sensibility = 20;
        alarmLength = 2;

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sound, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        SeekBar sensibilitySeekBar = (SeekBar) findViewById(R.id.seekBarSensibility);
        sensibilitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sensibility = i;
                textSensibility.setText("Sensibility: " + sensibility * 4 + " %");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar lengthSeekBar = (SeekBar) findViewById(R.id.seekBarLength);
        lengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                alarmLength = i + 1;
                textLength.setText("Alarm length: " + alarmLength + " s");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener((v) -> {
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.putExtra("sensibility", sensibility);
            intent.putExtra("sensibilityMax", sensibilitySeekBar.getMax());
            intent.putExtra("alarmLength", alarmLength);
            intent.putExtra("chosenAlarm", spinner.getSelectedItem().toString());
            startActivity(intent);
        });
    }
}
