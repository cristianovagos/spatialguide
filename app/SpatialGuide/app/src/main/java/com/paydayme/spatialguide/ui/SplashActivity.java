package com.paydayme.spatialguide.ui;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by cvagos on 13-03-2018.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start main activity
//        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        startActivity(new Intent(SplashActivity.this, RouteActivity.class));

        // Close splash activity
        finish();
    }
}