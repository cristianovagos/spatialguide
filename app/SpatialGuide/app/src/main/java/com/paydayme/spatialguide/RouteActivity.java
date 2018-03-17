package com.paydayme.spatialguide;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cvagos on 17-03-2018.
 */

public class RouteActivity extends AppCompatActivity {

    private static final String TAG = "RouteActivity";

    @BindView(R.id.btn_start) Button startRouteBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ButterKnife.bind(this);

        initClickListeners();
    }

    private void initClickListeners() {
        // Login Button
        startRouteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Go to login
            goToMap();
            }
        });
    }

    private void goToMap() {
        // disable login button
        startRouteBtn.setEnabled(false);

        // progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(RouteActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Setting the route...");
        progressDialog.show();

        new android.os.Handler().postDelayed(
                new Runnable() {
            public void run() {
                // On complete call either onLoginSuccess or onLoginFailed
                onRouteSelection();
                // onLoginFailed();
                progressDialog.dismiss();
            }
        }, 3000);
    }

    public void onRouteSelection() {
        startRouteBtn.setEnabled(true);
        startActivity(new Intent(RouteActivity.this, MapActivity.class));
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to quit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //Stop the activity
                            finish();
                        }

                    })
                    .setNegativeButton("No", null)
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
            textView.setTypeface(tf);
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }

    }
}
