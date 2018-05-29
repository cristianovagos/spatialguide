package com.paydayme.auralizationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.paydayme.auralizationdemo.Constant.SHARED_PREFS_AURALIZATION;
import static com.paydayme.auralizationdemo.Constant.SHARED_PREFS_EXTERNAL_IMU;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AuralizationEngine auralizationEngine;

    @BindView(R.id.btn1) ImageButton button1;
    @BindView(R.id.btn2) ImageButton button2;
    @BindView(R.id.btn3) ImageButton button3;
    @BindView(R.id.btn4) ImageButton button4;
    @BindView(R.id.btn5) ImageButton button5;
    @BindView(R.id.btn6) ImageButton button6;
    @BindView(R.id.btn7) ImageButton button7;
    @BindView(R.id.btn8) ImageButton button8;

    @BindView(R.id.playBtn) Button playBtn;
    @BindView(R.id.changeSoundBtn) Button changeSoundBtn;

    private String[] listSounds = {
            "Kipling",
            "Djembe",
            "Drums",
            "Vocals",
            "Dowland"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        auralizationEngine = new AuralizationEngine(this, "kipling.wav");

        initClickListeners();
    }

    private void initClickListeners() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!auralizationEngine.isPlaying()) {
                    auralizationEngine.play();
                    playBtn.setText("Pause");
                    Toast.makeText(MainActivity.this, "Playing sound...", Toast.LENGTH_SHORT).show();
                } else {
                    auralizationEngine.pause();
                    playBtn.setText("Play");
                    Toast.makeText(MainActivity.this, "Pausing sound...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        changeSoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View view = inflater.inflate(R.layout.dialog_change_sound, null);

                ListView soundList = (ListView) view.findViewById(R.id.soundsList);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, listSounds);

                soundList.setAdapter(adapter);
                soundList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String value = (String) parent.getItemAtPosition(position);
                        value = value.toLowerCase() + ".wav";
                        auralizationEngine.stopAndUnload();
                        auralizationEngine = new AuralizationEngine(MainActivity.this, value);
                        playBtn.setText("Play");
                        Toast.makeText(MainActivity.this, "Sound \"" + value + "\" selected", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Change sound")
                        .setView(view);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(-1, 0, -1);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(0, 1, 0);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(1, 0, -1);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(-1, 0, 0);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(1, 0, 0);
            }
        });

        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(-1, 0, 1);
            }
        });

        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(0, -1, 0);
            }
        });

        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auralizationEngine.setPosition(1, 0, 1);
            }
        });
    }


}
