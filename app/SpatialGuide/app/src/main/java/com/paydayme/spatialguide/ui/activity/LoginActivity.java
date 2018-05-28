package com.paydayme.spatialguide.ui.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.utils.NetworkUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Credentials;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;
import static com.paydayme.spatialguide.core.Constant.CONNECTIVITY_ACTION;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_LOGIN_USERNAME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_PASSWORD;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_REMEMBER_ME;

/**
 * Created by cvagos on 13-03-2018.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int REQUEST_FORGOT_PASSWORD = 1;

    // SharedPreferences to save authentication token
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    // API interface
    private SGApiClient sgApiClient;

    private IntentFilter intentFilter;
    private AlertDialog dialog;

    // Reference the views
    @BindView(R.id.input_email) AppCompatEditText emailText;
    @BindView(R.id.input_password) AppCompatEditText passwordText;
    @BindView(R.id.btn_login) Button loginButton;
    @BindView(R.id.link_signup) TextView signupLink;
    @BindView(R.id.checkbox_login) AppCompatCheckBox checkBoxRemember;
    @BindView(R.id.link_forgot_pw) TextView forgotPasswordLink;
    @BindView(R.id.tilEmail) TextInputLayout tilEmail;
    @BindView(R.id.tilPassword) TextInputLayout tilPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);

        // Binding the views defined above
        ButterKnife.bind(this);

        // Initialize Retrofit
        initRetrofit();

        // Initializing click listeners and fonts
        initClickListeners();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        checkSharedPreferences();

        // Changing the font
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        tilEmail.setTypeface(tf);
        tilPassword.setTypeface(tf);
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

    private void checkSharedPreferences() {
        String checkbox = sharedPreferences.getString(SHARED_PREFERENCES_REMEMBER_ME, "False");
        String username = sharedPreferences.getString(SHARED_PREFERENCES_LOGIN_USERNAME, "");
        String password = sharedPreferences.getString(SHARED_PREFERENCES_PASSWORD, "");

        emailText.setText(username);
        passwordText.setText(password);

        if(checkbox.equals("True")) {
            checkBoxRemember.setChecked(true);
        } else {
            checkBoxRemember.setChecked(false);
        }
    }

    private void initRetrofit() {
        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);
    }

    private void initClickListeners() {
        // Login Button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do login mechanism
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
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        forgotPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the forgot password activity
                Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
                startActivityForResult(intent, REQUEST_FORGOT_PASSWORD);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() {
        boolean usedUsername = false;

        // Check if data inserted is valid
        if(!isLoginValid()) {
            // data not valid
            onLoginFailure();
            return;
        }

        // Check if user entered email or username
        if(!Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches()) {
            usedUsername = true;
        }

        // store username on sharedpreferences
        String username = emailText.getText().toString();
        spEditor.putString(SHARED_PREFERENCES_LOGIN_USERNAME, username);
        spEditor.apply();

        // check if Remember me checked box was checked and then store values
        if(checkBoxRemember.isChecked()) {
            // store checkbox state
            spEditor.putString(SHARED_PREFERENCES_REMEMBER_ME, "True");
            spEditor.apply();

            // store password
            String password = passwordText.getText().toString();
            spEditor.putString(SHARED_PREFERENCES_PASSWORD, password);
            spEditor.apply();
        } else {
            // store checkbox state
            spEditor.putString(SHARED_PREFERENCES_REMEMBER_ME, "False");
            spEditor.apply();

            // store password
            spEditor.putString(SHARED_PREFERENCES_PASSWORD, "");
            spEditor.apply();
        }

        // disable login button
        loginButton.setEnabled(false);
        forgotPasswordLink.setClickable(false);
        signupLink.setClickable(false);

        // progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.CustomDialogTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.authenticating));
        progressDialog.show();

        final String emailUsername = emailText.getText().toString();
        final String password = passwordText.getText().toString();

        // Check username was used on authentication
        User userAuthenticated = usedUsername ?
                new User(emailUsername, null, password) :
                new User(null, emailUsername, password);

        Call<ResponseBody> call = sgApiClient.login(userAuthenticated);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    // Store Authorization header
                    spEditor.putString(Constant.SHARED_PREFERENCES_AUTH_KEY, Credentials.basic(emailUsername, password));
                    spEditor.apply();

                    // delay login to 1 second just to present dialog
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    onLoginSuccess();
                                    progressDialog.dismiss();
                                }
                            }, 1000);
                } else {
                    Log.e(TAG, "onResponse (login failed): server responded with error");
                    progressDialog.dismiss();
                    onLoginFailure();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure (login failed): " + t.getMessage());
                progressDialog.dismiss();
                onLoginFailure();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_SIGNUP) {
            if(resultCode == RESULT_OK) {
                // What to do when signup completed successfully
                Log.d(TAG, "RESULT_OK from SignupActivity");
                Toast.makeText(this, getString(R.string.login_toast), Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_FORGOT_PASSWORD) {
            if(resultCode == RESULT_OK) {
                // What to do when password recovery completed successfully
                Log.d(TAG, "RESULT_OK from ForgotPasswordActivity");
            }
        }
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
                        finish();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), null)
                .setCancelable(false)
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    public void onLoginSuccess() {
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod("SpatialGuide Login")
                .putSuccess(true));

        startActivity(new Intent(LoginActivity.this, RouteActivity.class));
        finish();
    }

    public void onLoginFailure() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(R.string.error_login_failed)
                .setMessage(getString(R.string.error_login))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);

        loginButton.setEnabled(true);
        forgotPasswordLink.setClickable(true);
        signupLink.setClickable(true);

        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod("SpatialGuide Login")
                .putSuccess(false));
    }

    public boolean isLoginValid() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        // Check email
        if(email.isEmpty()) {
            emailText.setError(getString(R.string.error_username_email));
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

    private void showDialogNoConnection() {
        if(dialog != null) return;
        // Not connected to Internet
        dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
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
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(this, R.font.catamaran);
        textView.setTypeface(tf);
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkUtil.getConnectivityStatus(LoginActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
                showDialogNoConnection();
            }
            else {
                // Connected
                if(dialog != null)
                    dialog.dismiss();
            }
        }
    };


}
