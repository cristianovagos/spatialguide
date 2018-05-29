package com.paydayme.spatialguide.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.adapter.HistoryFavoritesAdapter;
import com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity;
import com.paydayme.spatialguide.utils.NetworkUtil;
import com.paydayme.spatialguide.utils.Utils;
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
import static com.paydayme.spatialguide.core.Constant.CONNECTIVITY_ACTION;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_LAST_ROUTE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_RESET_ROUTE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USERNAME;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_EMAIL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_IMAGE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_NAMES;
import static com.paydayme.spatialguide.core.Constant.SPATIALGUIDE_WEBSITE;

public class FavoritesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "FavoritesActivity";

    // API stuff
    private SGApiClient sgApiClient;
    private List<Object> favoritesList = new ArrayList<>();
    private List<Integer> favoriteRoutes, favoritePoints;

    private AlertDialog connectionDialog;
    private IntentFilter intentFilter;

    // SharedPreferences initialization
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;
    private int routeSelected;
    private boolean resetVisitedPoints;

    // Reference the views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.noFavoritesText) TextView noFavoritesText;
    @BindView(R.id.favoritesList) RecyclerView favoritesRV;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.loadingLayout) RelativeLayout loadingLayout;

    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
        initMenuHeaderViews();

        favoritesRV.setLayoutManager(new LinearLayoutManager(this));
        favoritesRV.setAdapter(new HistoryFavoritesAdapter(this, favoritesList));

        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();
        routeSelected = sharedPreferences.getInt(SHARED_PREFERENCES_LAST_ROUTE, -1);
        resetVisitedPoints = sharedPreferences.getBoolean(SHARED_PREFERENCES_RESET_ROUTE, false);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFavorites();
            }
        });

        authenticationHeader = sharedPreferences.getString(SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(FavoritesActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(FavoritesActivity.this, LoginActivity.class));
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
        
        getFavorites();
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
        }

        if(!userImage.isEmpty()) {
            Picasso.get()
                    .load(userImage)
                    .placeholder(R.drawable.progress_animation)
                    .error(R.mipmap.ic_launcher_round)
                    .into(userImageMenu, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            userImageMenu.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            menuErrorLayout.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    private void getFavorites() {
        favoritesRV.setVisibility(View.GONE);
        noFavoritesText.setVisibility(View.GONE);

        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        favoritePoints = response.body().getFavoritePoints();
                        favoriteRoutes = response.body().getFavoriteRoutes();
                    }
                    getRoutes();
                } else {
                    Log.e(TAG, "getFavorites - onResponse: some error occurred: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "getFavorites - onFailure: some error occurred: " + t.getMessage());
            }
        });
    }

    private void getRoutes() {
        if(favoriteRoutes.isEmpty()) {
            updateUI();
            return;
        }
        for(int i : favoriteRoutes) {
            Call<Route> call = sgApiClient.getRoute(authenticationHeader, i);
            call.enqueue(new Callback<Route>() {
                @Override
                public void onResponse(Call<Route> call, Response<Route> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        if(favoritesList != null && !favoritesList.contains(response.body()))
                            favoritesList.add(response.body());
                        getPoints();
                    } else {
                        Log.e(TAG, "getRoutes - onResponse: some error occurred: " + response.errorBody().toString());
                    }
                }

                @Override
                public void onFailure(Call<Route> call, Throwable t) {
                    Log.e(TAG, "getRoutes - onFailure: " + t.getMessage());
                }
            });
        }
    }

    private void getPoints() {
        if(favoritePoints.isEmpty()) {
            updateUI();
            return;
        }
        for(int i : favoritePoints) {
            Call<Point> call = sgApiClient.getPoint(authenticationHeader, i);
            call.enqueue(new Callback<Point>() {
                @Override
                public void onResponse(Call<Point> call, Response<Point> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        if(favoritesList != null && !favoritesList.contains(response.body()))
                            favoritesList.add(response.body());
                    } else {
                        Log.e(TAG, "getPoints - onResponse: some error occurred: " + response.errorBody().toString());
                    }
                    updateUI();
                }

                @Override
                public void onFailure(Call<Point> call, Throwable t) {
                    Log.e(TAG, "getPoints - onFailure: " + t.getMessage());
                }
            });
        }
    }

    private void updateUI() {
        if(swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        loadingLayout.setVisibility(View.GONE);

        if(favoritesList.isEmpty()) {
            noFavoritesText.setVisibility(View.VISIBLE);
            return;
        } else {
            noFavoritesText.setVisibility(View.GONE);
            favoritesRV.setVisibility(View.VISIBLE);
        }

        // Setting the point adapter and the recyclerview to receive route points
        HistoryFavoritesAdapter historyFavoritesAdapter = new HistoryFavoritesAdapter(this, favoritesList);

        favoritesRV.setHasFixedSize(true);
        favoritesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        favoritesRV.setItemAnimator(new DefaultItemAnimator());
        favoritesRV.setAdapter(historyFavoritesAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(FavoritesActivity.this, SGPreferencesActivity.class));
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
                startActivity(new Intent(FavoritesActivity.this, MapActivity.class)
                        .putExtra("route", routeSelected)
                        .putExtra("reset_points", resetVisitedPoints)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            case R.id.nav_route:
                startActivity(new Intent(FavoritesActivity.this, RouteActivity.class));
                finish();
                break;
            case R.id.nav_history:
                startActivity(new Intent(FavoritesActivity.this, HistoryActivity.class));
                finish();
                break;
            case R.id.nav_userpanel:
                startActivity(new Intent(FavoritesActivity.this, UserPanelActivity.class));
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
                Utils.deleteAllSharedPreferences(FavoritesActivity.this);
                startActivity(new Intent(FavoritesActivity.this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: failed to logout" + t.getMessage());
                Utils.deleteAllSharedPreferences(FavoritesActivity.this);
                startActivity(new Intent(FavoritesActivity.this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
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
        menu.findItem(R.id.nav_favorites).setVisible(false);
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
            if(NetworkUtil.getConnectivityStatus(FavoritesActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
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
