package com.paydayme.spatialguide.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.paydayme.spatialguide.utils.Utils;

/**
 * Created by cvagos on 13-03-2018.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Utils.isServicesOK(this, TAG)) {
            // Start main activity
//            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            startActivity(new Intent(SplashActivity.this, RouteActivity.class));
        } else {
            Log.d(TAG, "onCreate: Google Play Services not upgraded / installed");
            // TODO - show Google Play Services error with dialog to notify user
        }

        // Close splash activity
        finish();
    }
}
