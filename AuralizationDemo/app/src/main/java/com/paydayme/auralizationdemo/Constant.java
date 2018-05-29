package com.paydayme.auralizationdemo;

import android.net.ConnectivityManager;

/**
 * Created by cvagos on 20-03-2018.
 */

public final class Constant {

    public static final String SHARED_PREFS_AURALIZATION = "com.paydayme.auralizationdemo.prefs.auralization_engine";
    public static final String SHARED_PREFS_EXTERNAL_IMU = "com.paydayme.auralizationdemo.prefs.external_imu_device";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}
