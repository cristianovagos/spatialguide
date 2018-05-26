package com.paydayme.spatialguide.core;

import android.app.Application;
import android.os.SystemClock;

import com.pusher.pushnotifications.PushNotifications;

import static com.paydayme.spatialguide.core.Constant.PUSHER_INSTANCE_ID;

/**
 * Created by cvagos on 13-03-2018.
 */

public class InitApp extends Application {

    private static final String TAG = "InitApp";
    /* InitApp

       Class to just boot up the app, will run first
     */
    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(1000);

        PushNotifications.start(getApplicationContext(), PUSHER_INSTANCE_ID);
        PushNotifications.subscribe("spatialguide");
    }
}
