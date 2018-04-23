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
import android.widget.TextView;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.ui.adapter.RouteAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;

/**
 * Created by cvagos on 17-03-2018.
 */

public class RouteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "RouteActivity";

    // API Stuff
    private SGApiClient sgApiClient;
    private List<Route> routeList = new ArrayList<>();

    // SharedPreferences to save authentication token
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;

    // Views
    @BindView(R.id.availableRoutesList) RecyclerView avaliableRoutesRV;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.noRoutesText) TextView noRoutesText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        ButterKnife.bind(this);

        init();
    }

    private void init() {
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        // Get Authentication Header from SharedPreferences
        authenticationHeader = sharedPreferences.getString(Constant.SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(RouteActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RouteActivity.this, LoginActivity.class);
                            startActivity(intent);
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

//        getRoutesAPI();
        getFakeRoutes();
    }

    private void getUserInfo() {
        // TODO - get user info to be displayed on the menu
        // first name, last name, email and image
    }

    private void getFakeRoutes() {
        List<Point> tmpList = new ArrayList<>();
        tmpList.add(new Point("Fórum Aveiro", 40.641475, -8.653675));
        tmpList.add(new Point("Praça do Peixe", 40.642313, -8.655352));
        tmpList.add(new Point("Estação de Comboios", 40.643304, -8.641302));
        tmpList.add(new Point("Sé de Aveiro", 40.639469, -8.650397));

        routeList.add( new Route(1,
                "Test Route 1",
                "Welcome to the test route 1! Here it is how a route will look like in the SpatialGuide app.",
                "https://i0.wp.com/gazetarural.com/wp-content/uploads/2017/12/Aveiro-Ria.jpg",
                tmpList, 0, 20001024, System.currentTimeMillis()) );

        routeList.add( new Route(2,
                "Test Route 2",
                "Welcome to the test route 2! Here it is how a route will look like in the SpatialGuide app.",
                "https://www.visitportugal.com/sites/www.visitportugal.com/files/mediateca/TAP_PracaComercio_01e_CL-co.jpg",
                tmpList, 0, 20001024, System.currentTimeMillis()) );

        routeList.add( new Route(3,
                "Test Route 3",
                "Welcome to the test route 3! Here it is how a route will look like in the SpatialGuide app.",
                "https://www.visitportugal.com/sites/www.visitportugal.com/files/styles/destinos_galeria/public/mediateca/N22312.jpg",
                tmpList, 0, 20001024, System.currentTimeMillis()) );

        updateUI();
    }

    private void updateUI() {
        // Setting the point adapter and the recyclerview to receive route points
        RouteAdapter routeAdapter = new RouteAdapter(this, routeList, new RouteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Route item) {
                Intent intent = new Intent(RouteActivity.this, RouteDetailsActivity.class);
                Bundle bundle = new Bundle();

                // The id of the route selected
                bundle.putInt("route", item.getRouteID());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        avaliableRoutesRV.setHasFixedSize(true);
        avaliableRoutesRV.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        avaliableRoutesRV.setItemAnimator(new DefaultItemAnimator());
        avaliableRoutesRV.setAdapter(routeAdapter);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_website) {
            String url = "http://xcoa.av.it.pt/~pei2017-2018_g09/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
