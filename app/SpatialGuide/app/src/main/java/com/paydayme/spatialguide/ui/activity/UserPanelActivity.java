package com.paydayme.spatialguide.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.ui.helper.RouteOrderRecyclerHelper;
import com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_LAST_ROUTE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_LOGIN_USERNAME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_PASSWORD;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_REMEMBER_ME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USERNAME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_EMAIL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_IMAGE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_NAMES;
import static com.paydayme.spatialguide.core.Constant.SPATIALGUIDE_WEBSITE;

public class UserPanelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserPanelActivity";

    // API stuff
    private SGApiClient sgApiClient;

    // SharedPreferences initialization
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private AlertDialog dialog;

    private String authenticationHeader;
    private int routeSelected;

    private User currentUser;

    private AppCompatEditText passwordEditText;
    private AppCompatEditText newPasswordEditText;
    private AppCompatEditText reEnterNewPasswordEditText;
    private AppCompatEditText emailEditText;
    private AppCompatEditText newEmailEditText;
    private AppCompatEditText reEnterNewEmailEditText;

    // Reference the views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.userimage) CircleImageView userimage;
    @BindView(R.id.username) TextView username;
    @BindView(R.id.username_first_last) TextView username_first_last;
    @BindView(R.id.email) TextView email;
    @BindView(R.id.changepassword) Button btn_changepassword;
    @BindView(R.id.changeemail) Button btn_changeemail;

    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        initMenuHeaderViews();

        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        initClickListeners();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();
        routeSelected = sharedPreferences.getInt(SHARED_PREFERENCES_LAST_ROUTE, -1);

        authenticationHeader = sharedPreferences.getString(SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(UserPanelActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(UserPanelActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
            textView.setTypeface(tf);
        }

        getUserInfoSharedPreferences();

        // Setting action bar to the toolbar, removing text
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        // Add listener to the hamburger icon on left
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        prepareNavigationMenu();
    }

    private void initClickListeners() {
        btn_changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        btn_changeemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeEmailDialog();
            }
        });
    }

    private void initMenuHeaderViews() {
        View header = navigationView.getHeaderView(0);
        userNameMenu = (TextView) header.findViewById(R.id.userNameMenu);
        userImageMenu = (CircleImageView) header.findViewById(R.id.userImageMenu);
        userEmailMenu = (TextView) header.findViewById(R.id.userEmailMenu);
        menuErrorLayout = (LinearLayout) header.findViewById(R.id.menuErrorLayout);
    }

    private void getUserInfoSharedPreferences() {
        String userNames = sharedPreferences.getString(SHARED_PREFERENCES_USER_NAMES, "");
        String userEmail = sharedPreferences.getString(SHARED_PREFERENCES_USER_EMAIL, "");
        String userImage = sharedPreferences.getString(SHARED_PREFERENCES_USER_IMAGE, "");
        String userName = sharedPreferences.getString(SHARED_PREFERENCES_USERNAME, "");

        if(userNames.isEmpty() || userEmail.isEmpty() || userName.isEmpty()) {
            menuErrorLayout.setVisibility(View.VISIBLE);
        } else {
            userNameMenu.setText(userNames);
            userEmailMenu.setText(userEmail);

            username_first_last.setText(userNames);
            username.setText(userName);
            email.setText(userEmail);
        }

        if(!userImage.isEmpty()) {
            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.mipmap.ic_launcher_round)
                    .into(userImageMenu);

            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.mipmap.ic_launcher_round)
                    .into(userimage);
        }
    }

    private void showChangeEmailDialog() {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_email, null);

        // The dialog that will appear
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(view)
                .setCancelable(true);

        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);

        TextInputLayout tilPassword = view.findViewById(R.id.tilPassword);
        TextInputLayout tilEmail = view.findViewById(R.id.tilEmail);
        TextInputLayout tilNewEmail = view.findViewById(R.id.tilNewEmail);
        TextInputLayout tilReEnterNewEmail = view.findViewById(R.id.tilReEnterNewEmail);
        passwordEditText = view.findViewById(R.id.input_password);
        emailEditText = view.findViewById(R.id.input_email);
        newEmailEditText = view.findViewById(R.id.input_newEmail);
        reEnterNewEmailEditText = view.findViewById(R.id.input_reEnterNewEmail);

        builder.setTitle(getString(R.string.change_email))
                .setPositiveButton(getString(R.string.change_email), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });

        // Setting custom font to dialog
        tilPassword.setTypeface(tf);
        tilEmail.setTypeface(tf);
        tilNewEmail.setTypeface(tf);
        tilReEnterNewEmail.setTypeface(tf);
        passwordEditText.setTypeface(tf);
        emailEditText.setTypeface(tf);
        newEmailEditText.setTypeface(tf);
        reEnterNewEmailEditText.setTypeface(tf);

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
        lp.gravity = Gravity.CENTER;

        dialog.show();

        // Override the button handler to check if data is valid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;

                String password = passwordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String newEmail = newEmailEditText.getText().toString();
                String reEnterNewEmail = reEnterNewEmailEditText.getText().toString();

                // check email
                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailEditText.setError(getString(R.string.error_email));
                    valid = false;
                } else {
                    emailEditText.setError(null);
                }

                // check email
                if (newEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    newEmailEditText.setError(getString(R.string.error_email));
                    valid = false;
                } else if (newEmail.equals(email)) {
                    newEmailEditText.setError(getString(R.string.error_email_change_equal));
                    valid = false;
                } else{
                    newEmailEditText.setError(null);
                }

                // check email
                if (reEnterNewEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(reEnterNewEmail).matches()) {
                    reEnterNewEmailEditText.setError(getString(R.string.error_email));
                    valid = false;
                } else if (reEnterNewEmail.equals(email)) {
                    reEnterNewEmailEditText.setError(getString(R.string.error_email_change_equal));
                    valid = false;
                } else if (!reEnterNewEmail.equals(newEmail)) {
                    reEnterNewEmailEditText.setError(getString(R.string.error_email_match));
                    valid = false;
                } else {
                    reEnterNewEmailEditText.setError(null);
                }

                // check password
                if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                    passwordEditText.setError(getString(R.string.error_password));
                    valid = false;
                } else {
                    passwordEditText.setError(null);
                }

                if(valid) {
                    onChangeEmail(password, email, newEmail, reEnterNewEmail);
                    dialog.dismiss();
                }
            }
        });

        dialog.getWindow().setAttributes(lp);
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(tf);
    }

    private void showChangePasswordDialog() {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_password, null);

        // The dialog that will appear
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(view)
                .setCancelable(true);

        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);

        TextInputLayout tilPassword = view.findViewById(R.id.tilPassword);
        TextInputLayout tilNewPassword = view.findViewById(R.id.tilNewPassword);
        TextInputLayout tilReEnterNewPassword = view.findViewById(R.id.tilReEnterNewPassword);
        passwordEditText = view.findViewById(R.id.input_password);
        newPasswordEditText = view.findViewById(R.id.input_newPassword);
        reEnterNewPasswordEditText = view.findViewById(R.id.input_reEnterNewPassword);

        builder.setTitle(getString(R.string.change_password))
                .setPositiveButton(getString(R.string.change_password), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });

        // Setting custom font to dialog
        tilPassword.setTypeface(tf);
        tilNewPassword.setTypeface(tf);
        tilReEnterNewPassword.setTypeface(tf);
        passwordEditText.setTypeface(tf);
        newPasswordEditText.setTypeface(tf);
        reEnterNewPasswordEditText.setTypeface(tf);

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
        lp.gravity = Gravity.CENTER;

        dialog.show();

        // Override the button handler to check if data is valid
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;

                String password = passwordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String reEnterNewPassword = reEnterNewPasswordEditText.getText().toString();

                // check password
                if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
                    passwordEditText.setError(getString(R.string.error_password));
                    valid = false;
                } else {
                    passwordEditText.setError(null);
                }

                // check password
                if (newPassword.isEmpty() || newPassword.length() < 4 || newPassword.length() > 10) {
                    passwordEditText.setError(getString(R.string.error_password));
                    valid = false;
                } else if(newPassword.equals(password)) {
                    passwordEditText.setError(getString(R.string.error_password_change_equal));
                    valid = false;
                } else {
                    passwordEditText.setError(null);
                }

                // check password
                if (reEnterNewPassword.isEmpty() || reEnterNewPassword.length() < 4 || reEnterNewPassword.length() > 10) {
                    reEnterNewPasswordEditText.setError(getString(R.string.error_password));
                    valid = false;
                } else if(reEnterNewPassword.equals(password)) {
                    reEnterNewPasswordEditText.setError(getString(R.string.error_password_change_equal));
                    valid = false;
                } else if(!reEnterNewPassword.equals(newPassword)) {
                    reEnterNewPasswordEditText.setError(getString(R.string.error_password_match));
                    valid = false;
                } else {
                    passwordEditText.setError(null);
                }

                if(valid) {
                    onChangePassword(password, newPassword, reEnterNewPassword);
                    dialog.dismiss();
                }
            }
        });

        dialog.getWindow().setAttributes(lp);
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(tf);
    }

    private void onChangeEmail(String passwordText, String emailText, final String newEmailText, String reEnterNewEmailText) {
        HashMap tmpMap = new HashMap(4);
        tmpMap.put("password", passwordText);
        tmpMap.put("old_email", emailText);
        tmpMap.put("new_email", newEmailText);
        tmpMap.put("email_confirmation", reEnterNewEmailText);

        Call<ResponseBody> call = sgApiClient.changeEmail(tmpMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "onChangeEmail - onResponse (changed email successfully): " + response.body().toString());

                    // If on sharedpreferences checkbox remember me is enabled
                    // if the stored username/email is the email, it should be updated
                    String checkbox = sharedPreferences.getString(SHARED_PREFERENCES_REMEMBER_ME, "False");
                    if(checkbox.equals("True")) {
                        String usernameEmail = sharedPreferences.getString(SHARED_PREFERENCES_LOGIN_USERNAME, "");
                        if(Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()) {
                            spEditor.putString(SHARED_PREFERENCES_LOGIN_USERNAME, newEmailText);
                            spEditor.apply();
                        }
                    }

                    // Edit the current shared prefs email for menu display
                    spEditor.putString(SHARED_PREFERENCES_USER_EMAIL, newEmailText);
                    spEditor.apply();

                    Toast.makeText(UserPanelActivity.this, "Email changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onChangeEmail - onResponse: failed to send location to API");
                    Log.d(TAG, "onChangeEmail - onResponse: " + response.errorBody().toString());
                    Toast.makeText(UserPanelActivity.this, "Some error occurred while changing email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onChangeEmail - onFailure: failed to change email" + t.getMessage());
                Toast.makeText(UserPanelActivity.this, "Some error occurred while changing email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onChangePassword(String password, final String newPassword, String newPassword2) {
        HashMap tmpMap = new HashMap(3);
        tmpMap.put("old_pass", password);
        tmpMap.put("new_pass", newPassword);
        tmpMap.put("pass_confirmation", newPassword2);

        Call<ResponseBody> call = sgApiClient.changePassword(tmpMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "onChangePassword - onResponse (changed password successfully): " + response.body().toString());

                    // If on sharedpreferences checkbox remember me is enabled
                    // the stored password should be updated
                    String checkbox = sharedPreferences.getString(SHARED_PREFERENCES_REMEMBER_ME, "False");
                    if(checkbox.equals("True")) {
                        spEditor.putString(SHARED_PREFERENCES_PASSWORD, newPassword);
                        spEditor.apply();
                    }

                    Toast.makeText(UserPanelActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onChangePassword - onResponse: failed to send location to API");
                    Log.d(TAG, "onChangePassword - onResponse: " + response.errorBody().toString());
                    Toast.makeText(UserPanelActivity.this, "Some error occurred while changing password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onChangePassword - onFailure: failed to change password" + t.getMessage());
                Toast.makeText(UserPanelActivity.this, "Some error occurred while changing password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(UserPanelActivity.this, SGPreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_website:
                startActivity(new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(SPATIALGUIDE_WEBSITE)));
                break;
            case R.id.nav_logout:
                onLogout();
                break;
            case R.id.nav_map:
                startActivity(new Intent(UserPanelActivity.this, MapActivity.class)
                        .putExtra("route", routeSelected));
                break;
            case R.id.nav_route:
                startActivity(new Intent(UserPanelActivity.this, RouteActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(UserPanelActivity.this, HistoryActivity.class));
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void onLogout() {
        Call<ResponseBody> call = sgApiClient.logout(authenticationHeader);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    spEditor.putString(Constant.SHARED_PREFERENCES_AUTH_KEY, "");
                    spEditor.apply();
                    startActivity(new Intent(UserPanelActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: failed to logout" + t.getMessage());
            }
        });
    }

    private void prepareNavigationMenu() {
        // Set listener of navigation view to this class
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        if(routeSelected != -1) {
            menu.findItem(R.id.nav_map).setVisible(true);
        }
        menu.findItem(R.id.nav_userpanel).setVisible(false);
    }
}
