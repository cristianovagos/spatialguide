package com.example.red_d.imu_test;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class IMUService extends Service implements SensorEventListener {
    private static final String TAG = "IMUService";

    private SensorManager sensorManager = null;
    private Sensor magneticSensor = null;
    private Sensor accelSensor = null;

    private PowerManager.WakeLock wakeLock = null;

    @Override
    public void onCreate() {
        super.onCreate();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        PowerManager manager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

        registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        unregisterListeners();
        wakeLock.release();
        stopForeground(true);
    }

    private void registerListeners() {
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterListeners() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Notification notification = null;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("com.paydayme.spatialguide.imu",
                    "Channel One", NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);

            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("IMU Service")
                    .setContentText("Obtaining values...")
                    .setChannelId("com.paydayme.spatialguide.imu").build();

        } else {
            notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("IMU Service")
                    .setContentText("Obtaining values...").build();
        }

        startForeground(Process.myPid(), notification);
        registerListeners();
        wakeLock.acquire();

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Intent intent = new Intent("imu-service");
        intent.putExtra("values", event.values);
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                intent.putExtra("magnetic-values", event.values);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                intent.putExtra("accel-values", event.values);
                break;
        }

        getApplicationContext().sendBroadcast(intent);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive: " + intent);

            if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                return;

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "run: runnable running");
                    unregisterListeners();
                    registerListeners();
                }
            };

            new Handler().postDelayed(runnable, 500);
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
