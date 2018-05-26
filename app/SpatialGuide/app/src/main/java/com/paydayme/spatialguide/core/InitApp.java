package com.paydayme.spatialguide.core;

import android.app.Application;
import android.os.SystemClock;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.core.CrashlyticsCore;
import com.paydayme.spatialguide.BuildConfig;
import com.pusher.pushnotifications.PushNotifications;

import io.fabric.sdk.android.Fabric;

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

        Fabric.with(this, new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder()
                    .disabled(BuildConfig.DEBUG)
                    .build())
            .build(), new Answers());
    }
}
