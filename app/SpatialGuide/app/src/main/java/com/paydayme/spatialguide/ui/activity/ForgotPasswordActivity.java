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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.utils.NetworkUtil;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;
import static com.paydayme.spatialguide.core.Constant.CONNECTIVITY_ACTION;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    // API interface
    private SGApiClient sgApiClient;

    private AlertDialog connectionDialog;
    private IntentFilter intentFilter;

    @BindView(R.id.input_email) AppCompatEditText emailUsernameText;
    @BindView(R.id.input_password) AppCompatEditText passwordText;
    @BindView(R.id.input_reEnterPassword) AppCompatEditText reEnterPasswordText;
    @BindView(R.id.btn_recover_password) Button recoverPasswordButton;
    @BindView(R.id.link_login) TextView loginLink;
    @BindView(R.id.tilEmail) TextInputLayout tilEmailUsername;
    @BindView(R.id.tilPassword) TextInputLayout tilPassword;
    @BindView(R.id.tilReEnterPassword) TextInputLayout tilReEnterPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);

        // Binding the views defined above
        ButterKnife.bind(this);

        // Initialize Retrofit
        initRetrofit();

        // Initializing click listeners
        initClickListeners();

        // Changing the font
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        tilEmailUsername.setTypeface(tf);
        tilPassword.setTypeface(tf);
        tilReEnterPassword.setTypeface(tf);
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
                .setTitle(getString(R.string.exit_dialog_title))
                .setMessage(getString(R.string.exit_dialog_prompt))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Stop the activity
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

    public void initClickListeners() {
        // recover password button
        recoverPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recoverPassword();
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

    private void recoverPassword() {
        if(!isRecoverPassswordValid()) {
            onRecoverPassswordFailure();
            return;
        }

        recoverPasswordButton.setEnabled(false);
        loginLink.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(ForgotPasswordActivity.this,
                R.style.CustomDialogTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.recovering_password));
        progressDialog.show();

        String username = emailUsernameText.getText().toString();
        String newPassword = passwordText.getText().toString();
        String newPassword2 = reEnterPasswordText.getText().toString();

        HashMap tmpMap = new HashMap(3);
        tmpMap.put("username", username);
        tmpMap.put("new_pass", newPassword);
        tmpMap.put("confirm_pass", newPassword2);

        Call<ResponseBody> call = sgApiClient.recoverPassword(tmpMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    onRecoverPassswordSuccess();
                    progressDialog.dismiss();
                } else {
                    Log.e(TAG, "recoverPassword - onResponse: some error occurred: " + response.errorBody().toString());
                    progressDialog.dismiss();
                    onRecoverPassswordFailure();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "recoverPassword - onFailure: " + t.getMessage());
                progressDialog.dismiss();
                onRecoverPassswordFailure();
            }
        });
    }

    public void onRecoverPassswordSuccess() {
        Log.d(TAG, "recovery success, going back to LoginActivity...");
        Toast.makeText(ForgotPasswordActivity.this, "Password recovered successfully!", Toast.LENGTH_SHORT).show();
        recoverPasswordButton.setEnabled(true);
        loginLink.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onRecoverPassswordFailure() {
        Toast.makeText(getBaseContext(), getText(R.string.error_recovering_password), Toast.LENGTH_LONG).show();
        recoverPasswordButton.setEnabled(true);
        loginLink.setEnabled(true);
    }

    public boolean isRecoverPassswordValid() {
        boolean valid = true;

        String emailUsername = emailUsernameText.getText().toString();
        String password = passwordText.getText().toString();
        String reEnterPassword = reEnterPasswordText.getText().toString();

        // check username
        if(emailUsername.isEmpty() || emailUsername.length() < 3) {
            emailUsernameText.setError(getString(R.string.error_name));
            valid = false;
        } else {
            emailUsernameText.setError(null);
        }

        // check password
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            passwordText.setError(getString(R.string.error_password));
            valid = false;
        } else {
            passwordText.setError(null);
        }

        // check re-entered password
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 || !(reEnterPassword.equals(password))) {
            reEnterPasswordText.setError(getString(R.string.error_password_match));
            valid = false;
        } else {
            reEnterPasswordText.setError(null);
        }

        return valid;
    }

    private void showDialogNoConnection() {
        if(connectionDialog != null) return;
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

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkUtil.getConnectivityStatus(ForgotPasswordActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
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
