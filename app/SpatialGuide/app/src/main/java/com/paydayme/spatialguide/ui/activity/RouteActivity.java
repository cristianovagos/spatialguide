package com.paydayme.spatialguide.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.adapter.RouteAdapter;
import com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

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
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USERNAME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_EMAIL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_IMAGE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_NAMES;
import static com.paydayme.spatialguide.core.Constant.SPATIALGUIDE_WEBSITE;

/**
 * Created by cvagos on 17-03-2018.
 */

public class RouteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // TODO - add BroadcastReceiver to listen to internet connection, see LoginActivity and SignupActivity

    private static final String TAG = "RouteActivity";

    // API Stuff
    private SGApiClient sgApiClient;
    private List<Route> routeList = new ArrayList<>();

    // SharedPreferences to save authentication token
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;
    private int routeSelected;
    private String authenticationHeader;
    private User currentUser;

    // Views
    @BindView(R.id.availableRoutesList) RecyclerView avaliableRoutesRV;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.noRoutesText) TextView noRoutesText;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.loadingLayout) RelativeLayout loadingLayout;

    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        initMenuHeaderViews();

        // Setting action bar to the toolbar, removing text
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        avaliableRoutesRV.setLayoutManager(new LinearLayoutManager(this));
        avaliableRoutesRV.setAdapter(new RouteAdapter(this, routeList, new RouteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Route item, View view) {}
        }));

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
//        swipeRefreshLayout.setProgressViewOffset(false, 120, 155);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getRoutesAPI();
            }
        });

        // Add listener to the hamburger icon on left
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();
        routeSelected = sharedPreferences.getInt(SHARED_PREFERENCES_LAST_ROUTE, -1);

        prepareNavigationMenu();

        // Get Authentication Header from SharedPreferences
        authenticationHeader = sharedPreferences.getString(SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(RouteActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RouteActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
            textView.setTypeface(tf);
        }

        getUserInfo();
        getRoutesAPI();
    }

    private void initMenuHeaderViews() {
        View header = navigationView.getHeaderView(0);
        userNameMenu = (TextView) header.findViewById(R.id.userNameMenu);
        userImageMenu = (CircleImageView) header.findViewById(R.id.userImageMenu);
        userEmailMenu = (TextView) header.findViewById(R.id.userEmailMenu);
        menuErrorLayout = (LinearLayout) header.findViewById(R.id.menuErrorLayout);
    }

    private void getUserInfo() {
        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    currentUser = response.body();

                    String userNames = currentUser.getFirst_name() + " " + currentUser.getLast_name();
                    spEditor.putString(SHARED_PREFERENCES_USER_NAMES, userNames);
                    spEditor.putString(SHARED_PREFERENCES_USER_EMAIL, currentUser.getEmail());
                    spEditor.putString(SHARED_PREFERENCES_USER_IMAGE, currentUser.getUserImage());
                    spEditor.putString(SHARED_PREFERENCES_USERNAME, currentUser.getUsername());
                    spEditor.apply();

                    // Get user info to be displayed on the header menu
                    getUserInfoSharedPreferences();
                } else {
                    Log.e(TAG, "getUserInfo - onResponse: some error occurred: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "getUserInfo - onFailure: some error occurred: " + t.getMessage());
            }
        });
    }

    private void getUserInfoSharedPreferences() {
        String userNames = sharedPreferences.getString(SHARED_PREFERENCES_USER_NAMES, "");
        String userEmail = sharedPreferences.getString(SHARED_PREFERENCES_USER_EMAIL, "");
        String userImage = sharedPreferences.getString(SHARED_PREFERENCES_USER_IMAGE, "");

        if(userNames.isEmpty() || userEmail.isEmpty()) {
            menuErrorLayout.setVisibility(View.VISIBLE);
        } else {
            userNameMenu.setText(userNames);
            userEmailMenu.setText(userEmail);
        }

        if(!userImage.isEmpty()) {
            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.mipmap.ic_launcher_round)
                    .into(userImageMenu);
        }
    }

    private void updateUI() {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        if(routeList.isEmpty()) {
            noRoutesText.setVisibility(View.VISIBLE);
        } else {
            noRoutesText.setVisibility(View.GONE);
        }

        // Setting the point adapter and the recyclerview to receive route points
        RouteAdapter routeAdapter = new RouteAdapter(this, routeList, new RouteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Route item, View view) {
                view.setEnabled(false);
                Intent intent = new Intent(RouteActivity.this, RouteDetailsActivity.class);
                Bundle bundle = new Bundle();

                // The id of the route selected
                bundle.putInt("route", item.getRouteID());
                intent.putExtras(bundle);
                startActivity(intent);
                view.setEnabled(true);
                finish();
            }
        });

        avaliableRoutesRV.setHasFixedSize(true);
        avaliableRoutesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        avaliableRoutesRV.setItemAnimator(new DefaultItemAnimator());
        avaliableRoutesRV.setAdapter(routeAdapter);

        loadingLayout.setVisibility(View.GONE);
    }

    private void getRoutesAPI() {
        Call<List<Route>> call = sgApiClient.getRoutes(authenticationHeader);

        call.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if(response.isSuccessful()) {
                    routeList = response.body();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, "getRoutes - onFailure: " + t.getMessage());
                avaliableRoutesRV.setVisibility(View.GONE);
                noRoutesText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * What to do when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(RouteActivity.this, SGPreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
            case R.id.nav_userpanel:
                startActivity(new Intent(RouteActivity.this, UserPanelActivity.class));
                finish();
                break;
            case R.id.nav_history:
                startActivity(new Intent(RouteActivity.this, HistoryActivity.class));
                finish();
                break;
            case R.id.nav_map:
                startActivity(new Intent(RouteActivity.this, MapActivity.class)
                        .putExtra("route", routeSelected));
                finish();
                break;
            case R.id.nav_favorites:
                startActivity(new Intent(RouteActivity.this, FavoritesActivity.class));
                finish();
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
                    spEditor.putString(SHARED_PREFERENCES_AUTH_KEY, "");
                    spEditor.apply();
                    startActivity(new Intent(RouteActivity.this, LoginActivity.class));
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
        menu.findItem(R.id.nav_route).setVisible(false);
    }
}
