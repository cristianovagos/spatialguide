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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.ui.helper.RouteOrderRecyclerHelper;
import com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;

public class UserPanelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "UserPanelActivity";

    // API stuff
    private SGApiClient sgApiClient;

    // SharedPreferences initialization
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private AlertDialog dialog;

    // Reference the views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.userimage) CircleImageView userimage;
    @BindView(R.id.username) TextView username;
    @BindView(R.id.username_first_last) TextView username_first_last;
    @BindView(R.id.email) TextView email;
    @BindView(R.id.datemember) TextView datemember;
    @BindView(R.id.changepassword) Button btn_changepassword;
    @BindView(R.id.changeemail) Button btn_changeemail;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        initClickListeners();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        // Setting action bar to the toolbar, removing text
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        // Add listener to the hamburger icon on left
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set listener of navigation view to this class
        navigationView.setNavigationItemSelectedListener(this);

        getUserInfo();
    }

    private void initClickListeners() {
        btn_changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDialog(false);
            }
        });

        btn_changeemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDialog(true);
            }
        });
    }

    private void getUserInfo() {
        // TODO (BACKEND needed) get user info and show on view
    }

    private void showChangeDialog(boolean isEmail) {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = isEmail ? inflater.inflate(R.layout.dialog_change_email, null) :
                inflater.inflate(R.layout.dialog_change_password, null);

        //Ask the user if they want to quit
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(view)
                .setCancelable(true);

        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);

        if(!isEmail) {
            TextInputLayout tilPassword = view.findViewById(R.id.tilPassword);
            TextInputLayout tilNewPassword = view.findViewById(R.id.tilNewPassword);
            TextInputLayout tilReEnterNewPassword = view.findViewById(R.id.tilReEnterNewPassword);
            final AppCompatEditText password = view.findViewById(R.id.input_password);
            final AppCompatEditText newPassword = view.findViewById(R.id.input_newPassword);
            final AppCompatEditText reEnterNewPassword = view.findViewById(R.id.input_reEnterNewPassword);

            builder.setTitle(getString(R.string.change_password))
                .setPositiveButton(getString(R.string.change_password), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String passwordText = password.getText().toString();
                    String newPasswordText = newPassword.getText().toString();
                    String reEnterNewPasswordText = reEnterNewPassword.getText().toString();
                    onChangePassword(passwordText, newPasswordText, reEnterNewPasswordText);
                }
            });

            // Setting custom font to dialog
            tilPassword.setTypeface(tf);
            tilNewPassword.setTypeface(tf);
            tilReEnterNewPassword.setTypeface(tf);
            password.setTypeface(tf);
            newPassword.setTypeface(tf);
            reEnterNewPassword.setTypeface(tf);
        } else {
            TextInputLayout tilEmail = view.findViewById(R.id.tilEmail);
            TextInputLayout tilNewEmail = view.findViewById(R.id.tilNewEmail);
            TextInputLayout tilReEnterNewEmail = view.findViewById(R.id.tilReEnterNewEmail);
            final AppCompatEditText email = view.findViewById(R.id.input_email);
            final AppCompatEditText newEmail = view.findViewById(R.id.input_newEmail);
            final AppCompatEditText reEnterNewEmail = view.findViewById(R.id.input_reEnterNewEmail);

            builder.setTitle(getString(R.string.change_email))
                    .setPositiveButton(getString(R.string.change_email), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String emailText = email.getText().toString();
                            String newEmailText = newEmail.getText().toString();
                            String reEnterNewEmailText = reEnterNewEmail.getText().toString();
                            onChangeEmail(emailText, newEmailText, reEnterNewEmailText);
                        }
                    });

            // Setting custom font to dialog
            tilEmail.setTypeface(tf);
            tilNewEmail.setTypeface(tf);
            tilReEnterNewEmail.setTypeface(tf);
            email.setTypeface(tf);
            newEmail.setTypeface(tf);
            reEnterNewEmail.setTypeface(tf);
        }

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
        lp.gravity = Gravity.CENTER;

        dialog.show();
        dialog.getWindow().setAttributes(lp);
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        textView.setTypeface(tf);
    }

    private void onChangeEmail(String emailText, String newEmailText, String reEnterNewEmailText) {
        if(!newEmailText.equals(reEnterNewEmailText))
            return;

        // TODO backend
    }

    private void onChangePassword(String password, String newPassword, String newPassword2) {
        if(!newPassword.equals(newPassword2))
            return;

        // TODO backend
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
            case R.id.nav_website: {
                String url = "http://xcoa.av.it.pt/~pei2017-2018_g09/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
            case R.id.nav_route: {
                // Routes
                startActivity(new Intent(UserPanelActivity.this, RouteActivity.class));
                break;
            }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
