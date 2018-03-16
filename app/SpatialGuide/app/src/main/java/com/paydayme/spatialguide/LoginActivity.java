package com.paydayme.spatialguide;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cvagos on 13-03-2018.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    // Reference the views
    @BindView(R.id.input_email) EditText emailText;
    @BindView(R.id.input_password) EditText passwordText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.link_signup) TextView signupLink;
    @BindView(R.id.tilEmail) TextInputLayout tilEmail;
    @BindView(R.id.tilPassword) TextInputLayout tilPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Binding the views defined above
        ButterKnife.bind(this);

        // Initializing click listeners and fonts
        initClickListeners();

        // Changing the font
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        tilEmail.setTypeface(tf);
        tilPassword.setTypeface(tf);
    }

    private void initClickListeners() {
        // Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to login
                login();
            }
        });

        // Signup link
        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the signup activity
                Intent i = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(i, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        Log.d(TAG, "login");

        // Check if data inserted is valid
        if(!isLoginValid()) {
            // data not valid
            onLoginFailure();
            return;
        }

        // disable login button
        loginButton.setEnabled(false);

        // progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // TODO: Implement authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SIGNUP) {
            if(resultCode == RESULT_OK) {
                // TODO: Implement successful signup logic here

                // By default just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable go back to main activity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        startActivity(new Intent(LoginActivity.this, MapActivity.class));
        finish();
    }

    public void onLoginFailure() {
        // Present Toast with error
        Toast.makeText(getBaseContext(), getString(R.string.error_login), Toast.LENGTH_LONG);
        loginButton.setEnabled(true);
    }

    public boolean isLoginValid() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // Check email
        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.error_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        // Check password
        if(password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError(getString(R.string.error_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
