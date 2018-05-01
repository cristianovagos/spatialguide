package com.paydayme.spatialguide.ui.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.utils.Utils;

/**
 * Created by cvagos on 13-03-2018.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();
        spEditor.remove(Constant.SHARED_PREFERENCES_LAST_ROUTE);
        spEditor.apply();

        if(Utils.isServicesOK(this, TAG)) {
            // Start main activity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
//            startActivity(new Intent(SplashActivity.this, RouteActivity.class));
        } else {
            Log.d(TAG, "onCreate: Google Play Services not upgraded / installed, exiting...");
            // TODO - show Google Play Services error with dialog to notify user (no need)
        }

        // Close splash activity
        finish();
    }
}
