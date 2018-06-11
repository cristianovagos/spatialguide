package com.paydayme.spatialguide.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SignUpEvent;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.utils.NetworkUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;
import static com.paydayme.spatialguide.core.Constant.CONNECTIVITY_ACTION;

/**
 * Created by cvagos on 13-03-2018.
 */

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    // API Stuff
    private SGApiClient sgApiClient;

    private IntentFilter intentFilter;
    private AlertDialog connectionDialog;

    // Reference the views
    @BindView(R.id.input_username) AppCompatEditText usernameText;
    @BindView(R.id.input_firstname) AppCompatEditText firstnameText;
    @BindView(R.id.input_lastname) AppCompatEditText lastnameText;
    @BindView(R.id.input_email) AppCompatEditText emailText;
    @BindView(R.id.input_reEnterEmail) AppCompatEditText reEnterEmailText;
    @BindView(R.id.input_password) AppCompatEditText passwordText;
    @BindView(R.id.input_reEnterPassword) AppCompatEditText reEnterPasswordText;
    @BindView(R.id.btn_signup) Button signupButton;
    @BindView(R.id.link_login) TextView loginLink;
    @BindView(R.id.tilUsername) TextInputLayout tilUsername;
    @BindView(R.id.tilFirstName) TextInputLayout tilFirstname;
    @BindView(R.id.tilLastName) TextInputLayout tilLastname;
    @BindView(R.id.tilEmail) TextInputLayout tilEmail;
    @BindView(R.id.tilReEnterEmail) TextInputLayout tilReEnterEmail;
    @BindView(R.id.tilPassword) TextInputLayout tilPassword;
    @BindView(R.id.tilReEnterPassword) TextInputLayout tilReEnterPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);

        // Binding the views defined above
        ButterKnife.bind(this);

        // Initialize Retrofit
        initRetrofit();

        // Initializing click listeners
        initClickListeners();

        // Changing the font
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        tilUsername.setTypeface(tf);
        tilFirstname.setTypeface(tf);
        tilLastname.setTypeface(tf);
        tilEmail.setTypeface(tf);
        tilReEnterEmail.setTypeface(tf);
        tilPassword.setTypeface(tf);
        tilReEnterPassword.setTypeface(tf);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkReceiver);
    }

    private void initRetrofit() {
        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);
    }

    @Override
    public void onBackPressed() {
        //Ask the user if they want to quit
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(getString(R.string.exit))
                .setMessage(getString(R.string.exit_prompt))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), null)
                .setCancelable(false)
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    public void initClickListeners() {
        // Signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        // Login link
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void signup() {
        if(!isSignupValid()) {
            onSignupFailure();
            return;
        }

        signupButton.setEnabled(false);
        loginLink.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.CustomDialogTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.creating_account_dialog));
        progressDialog.show();

        String username = usernameText.getText().toString();
        String firstname = firstnameText.getText().toString();
        String lastname = lastnameText.getText().toString();
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        Call<User> call = sgApiClient.registerUser(
                new User(username, firstname, lastname, password, email)
        );
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    onSignupSuccess();
                                    progressDialog.dismiss();
                                }
                            }, 1000);
                } else {
                    try {
                        Log.e(TAG, "onResponse (register failed): " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                    onSignupFailure();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure (register failed): " + t.getMessage());
                progressDialog.dismiss();
                onSignupFailure();
            }
        });
    }

    public void onSignupSuccess() {
        signupButton.setEnabled(true);
        Answers.getInstance().logSignUp(new SignUpEvent()
                .putMethod("SpatialGuide Signup")
                .putSuccess(true));
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailure() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(R.string.error_signup_failed)
                .setMessage(getString(R.string.error_signup))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);

        loginLink.setEnabled(true);
        signupButton.setEnabled(true);
        Answers.getInstance().logSignUp(new SignUpEvent()
                .putMethod("SpatialGuide Signup")
                .putSuccess(false));
    }

    public boolean isSignupValid() {
        boolean valid = true;

        String username = usernameText.getText().toString();
        String firstname = firstnameText.getText().toString();
        String lastname = lastnameText.getText().toString();
        String email = emailText.getText().toString();
        String reEnterEmail = reEnterEmailText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        // check username
        if(username.isEmpty() || username.trim().isEmpty() || username.length() < 3) {
            usernameText.setError(getString(R.string.error_name));

            // todo future work - check if username exists via API

            valid = false;
        } else {
            usernameText.setError(null);
        }

        // check firstname
        if(firstname.isEmpty() || firstname.trim().isEmpty() || firstname.length() < 3) {
            firstnameText.setError(getString(R.string.error_name));
            valid = false;
        } else {
            firstnameText.setError(null);
        }

        // check lastname
        if(lastname.isEmpty() || lastname.trim().isEmpty() || lastname.length() < 3) {
            lastnameText.setError(getString(R.string.error_name));
            valid = false;
        } else {
            lastnameText.setError(null);
        }

        // check email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError(getString(R.string.error_email));
            valid = false;
        } else {
            emailText.setError(null);
        }

        // check re-entered email
        if (reEnterEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(reEnterEmail).matches() || !(reEnterEmail.equals(email))) {
            reEnterEmailText.setError(getString(R.string.error_email_match));
            valid = false;
        } else {
            reEnterEmailText.setError(null);
        }

        // check password
        if (password.isEmpty() || password.trim().isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError(getString(R.string.error_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        // check re-entered password
        if (reEnterPassword.isEmpty() || reEnterPassword.trim().isEmpty() || reEnterPassword.length() < 4 ||
                reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError(getString(R.string.error_password_match));
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }

    private void showDialogNoConnection() {
        if(connectionDialog != null && connectionDialog.isShowing()) return;
        // Not connected to Internet
        connectionDialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(this.getString(R.string.no_connection))
                .setMessage(this.getString(R.string.no_connection_message))
                .setPositiveButton(this.getString(R.string.exit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
        TextView textView = (TextView) connectionDialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(this, R.font.catamaran);
        textView.setTypeface(tf);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkUtil.getConnectivityStatus(SignupActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
                showDialogNoConnection();
            }
            else {
                // Connected
                if(connectionDialog != null)
                    connectionDialog.dismiss();
            }
        }
    };
}
