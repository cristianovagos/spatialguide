package com.paydayme.spatialguide.core;

import android.app.Application;
import android.os.StrictMode;
import android.os.SystemClock;

/**
 * Created by cvagos on 13-03-2018.
 */

public class InitApp extends Application {

    /* InitApp

       Class to just boot up the app, will run first
     */
    @Override
    public void onCreate() {
        super.onCreate();
        SystemClock.sleep(2000);
    }
}
