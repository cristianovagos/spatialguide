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
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
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

    // SharedPreferences initialization
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;
    private int routeSelected;

    // Reference the views
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.noFavoritesText) TextView noFavoritesText;
    @BindView(R.id.favoritesList) RecyclerView favoritesRV;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout swipeRefreshLayout;

    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

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

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
//        swipeRefreshLayout.setProgressViewOffset(false, 120, 155);
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
                    .into(userImageMenu);
        }
    }

    private void getFavorites() {
        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    if (response.body() != null) {
                        Log.d(TAG, "getFavorites - onResponse: + " + response.body());
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
        for(int i : favoriteRoutes) {
            Call<Route> call = sgApiClient.getRoute(authenticationHeader, i);
            call.enqueue(new Callback<Route>() {
                @Override
                public void onResponse(Call<Route> call, Response<Route> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        favoritesList.add(response.body());
                        getPoints();
                    } else {
                        Log.e(TAG, "getRoutes - onResponse: " + response.errorBody().toString());
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
        if(favoritePoints.isEmpty())
            updateUI();
        for(int i : favoritePoints) {
            Call<Point> call = sgApiClient.getPoint(authenticationHeader, i);
            call.enqueue(new Callback<Point>() {
                @Override
                public void onResponse(Call<Point> call, Response<Point> response) {
                    if(response.isSuccessful() && response.body() != null) {
                        favoritesList.add(response.body());
                    } else {
                        Log.e(TAG, "getPoints - onResponse: " + response.errorBody().toString());
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

        if(favoritesList.isEmpty()) {
            noFavoritesText.setVisibility(View.VISIBLE);
        } else {
            noFavoritesText.setVisibility(View.GONE);
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
                        .putExtra("route", routeSelected));
                break;
            case R.id.nav_route:
                startActivity(new Intent(FavoritesActivity.this, RouteActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(FavoritesActivity.this, HistoryActivity.class));
                break;
            case R.id.nav_userpanel:
                startActivity(new Intent(FavoritesActivity.this, UserPanelActivity.class));
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
                    startActivity(new Intent(FavoritesActivity.this, LoginActivity.class));
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
        menu.findItem(R.id.nav_favorites).setVisible(false);
    }
}
