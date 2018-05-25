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
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.model.VisitedPoint;
import com.paydayme.spatialguide.ui.adapter.HistoryFavoritesAdapter;
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
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_EMAIL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_IMAGE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_NAMES;
import static com.paydayme.spatialguide.core.Constant.SPATIALGUIDE_WEBSITE;

public class HistoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // TODO - add BroadcastReceiver to listen to internet connection, see LoginActivity and SignupActivity

    private static final String TAG = "HistoryActivity";

    // API stuff
    private SGApiClient sgApiClient;

    // SharedPreferences initialization
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;
    private int routeSelected;
    private List<VisitedPoint> pointsVisited;
    private List<Object> pointsList = new ArrayList<>();

    // Reference the views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.noHistoryText) TextView noHistoryText;
    @BindView(R.id.historyList) RecyclerView historyRV;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.loadingLayout) RelativeLayout loadingLayout;

    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();
        routeSelected = sharedPreferences.getInt(SHARED_PREFERENCES_LAST_ROUTE, -1);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getHistory();
            }
        });

        authenticationHeader = sharedPreferences.getString(SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(HistoryActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(HistoryActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
            textView.setTypeface(tf);
        }

        // Get user info to be displayed on the header menu
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

        getHistory();
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


    private void getHistory() {
        historyRV.setVisibility(View.GONE);
        noHistoryText.setVisibility(View.GONE);

        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful() && response.body() != null) {
                    pointsVisited = response.body().getVisitedPoints();
                    getPoints();
                } else {
                    Log.e(TAG, "getHistory - onResponse: some error occurred: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "getHistory - onFailure: " + t.getMessage());
            }
        });
    }

    private void getPoints() {
        if(pointsVisited.isEmpty()) {
            updateUI();
            return;
        }
        for(VisitedPoint visitedPoint : pointsVisited) {
            Call<Point> call = sgApiClient.getPoint(authenticationHeader, visitedPoint.getId());
            call.enqueue(new Callback<Point>() {
                @Override
                public void onResponse(Call<Point> call, Response<Point> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        if(pointsList != null && !pointsList.contains(response.body()))
                            pointsList.add(response.body());
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

        if(pointsList.isEmpty()) {
            noHistoryText.setVisibility(View.VISIBLE);
            return;
        } else {
            noHistoryText.setVisibility(View.GONE);
            historyRV.setVisibility(View.VISIBLE);
        }

        // Setting the point adapter and the recyclerview to receive route points
        HistoryFavoritesAdapter historyFavoritesAdapter = new HistoryFavoritesAdapter(this, pointsList);

        historyRV.setHasFixedSize(true);
        historyRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        historyRV.setItemAnimator(new DefaultItemAnimator());
        historyRV.setAdapter(historyFavoritesAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(HistoryActivity.this, SGPreferencesActivity.class));
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
                startActivity(new Intent(HistoryActivity.this, MapActivity.class)
                        .putExtra("route", routeSelected));
                finish();
                break;
            case R.id.nav_route:
                startActivity(new Intent(HistoryActivity.this, RouteActivity.class));
                finish();
                break;
            case R.id.nav_userpanel:
                startActivity(new Intent(HistoryActivity.this, UserPanelActivity.class));
                finish();
                break;
            case R.id.nav_favorites:
                startActivity(new Intent(HistoryActivity.this, FavoritesActivity.class));
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
                    spEditor.putString(Constant.SHARED_PREFERENCES_AUTH_KEY, "");
                    spEditor.apply();
                    startActivity(new Intent(HistoryActivity.this, LoginActivity.class));
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
        menu.findItem(R.id.nav_history).setVisible(false);
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
}
