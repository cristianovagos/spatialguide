package com.paydayme.spatialguide.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;

/**
 * Created by cvagos on 22-03-2018.
 */

public class RouteDetailsActivity extends AppCompatActivity {

    public static final String TAG = "RouteDetailsActivity";

    // API Stuff
    private SGApiClient sgApiClient;
    private Route route;

    private int routeSelected;
    private PointAdapter pointAdapter;
    private List<Point> pointList = new ArrayList<>();
    private boolean routeOnStorage = false;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.routeList) RecyclerView recyclerView;
    @BindView(R.id.noRoutesText) TextView noRoutesText;
    @BindView(R.id.routeDescription) TextView routeDescription;
    @BindView(R.id.routeDate) TextView routeDate;
    @BindView(R.id.routeImage) ImageView routeImage;
    @BindView(R.id.routeDownloads) TextView routeDownloads;
    @BindView(R.id.mapImage) ImageView routeMapImage;
    @BindView(R.id.fabButton) FloatingActionButton fabButton;
    @BindView(R.id.fabText) TextView fabText;
    @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.layoutDetails) LinearLayout detailsLayout;
    @BindView(R.id.layoutPoints) LinearLayout pointsLayout;
    @BindView(R.id.layoutMap) LinearLayout mapLayout;
    @BindView(R.id.layoutImage) LinearLayout imageLayout;
    @BindView(R.id.progressDetails) ProgressBar detailsProgress;
    @BindView(R.id.progressPoints) ProgressBar pointsProgress;
    @BindView(R.id.progressMap) ProgressBar mapProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        ButterKnife.bind(this);

        // Get route selected on the previous screen
        Intent intent = getIntent();
        routeSelected = intent.getIntExtra("route", -1);
        Log.d(TAG, "route selected: " + routeSelected);

        if (routeSelected != -1) {
            init();

            /**
             * TODO: functioning with API data
             */
//            getRouteDetailsAPI(routeSelected);
//            getRoutePointsAPI(routeSelected);
            if (!isOnStorage(routeSelected))
                getFakeData();
            updateUI();
        } else {
            // Route not selected!! ERROR
            Log.e(TAG, "Invalid route!");
        }
    }

    private void init() {
        // Toolbar init
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setting Catamaran font to collapsing toolbar (collapsed and expanded)
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        collapsingToolbarLayout.setCollapsedTitleTypeface(tf);
        collapsingToolbarLayout.setExpandedTitleTypeface(tf);

        // Setting onClick event to toolbar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RouteDetailsActivity.this, RouteActivity.class));
            }
        });

        // Init retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofit.create(SGApiClient.class);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!routeOnStorage) {
                    //Ask the user if wants to download the route
                    AlertDialog dialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                            .setTitle(getString(R.string.download_route))
                            .setMessage(getString(R.string.download_route_prompt))
                            .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        InternalStorage.writeObject(getApplicationContext(), Constant.ROUTE_STORAGE_SEPARATOR + routeSelected, route);
                                        Toast.makeText(RouteDetailsActivity.this, getString(R.string.downloading_route), Toast.LENGTH_SHORT).show();
                                        routeOnStorage = true;
                                        fabButton.setImageDrawable(null);
                                        fabText.setVisibility(View.VISIBLE);
                                    } catch (IOException e) {
                                        Log.d(TAG, e.getMessage());
                                    }
                                }
                            })
                            .setNegativeButton(getString(android.R.string.no), null)
                            .setCancelable(false)
                            .show();
                    TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                    Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
                    textView.setTypeface(tf);
                } else {
                    //Ask the user if wants to navigate in this route
                    AlertDialog dialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                            .setTitle(getString(R.string.navigate_route))
                            .setMessage(getString(R.string.navigate_route_prompt))
                            .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(Utils.isServicesOK(getApplicationContext(), TAG)) {
                                        Intent intent = new Intent(RouteDetailsActivity.this, MapActivity.class);
                                        Bundle bundle = new Bundle();

                                        bundle.putInt("route", routeSelected);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                        finish();
                                    }
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
        });

        if(isOnStorage(routeSelected)) {
            routeOnStorage = true;
            fabText.setVisibility(View.VISIBLE);
        } else {
            fabButton.setImageResource(R.drawable.ic_download);
        }

        detailsProgress.setIndeterminate(true);
        mapProgress.setIndeterminate(true);
        pointsProgress.setIndeterminate(true);
    }

    private boolean isOnStorage(int routeID) {
        try {
            route = (Route) InternalStorage.readObject(this, Constant.ROUTE_STORAGE_SEPARATOR + routeID);
            long currentLastUpdate = getRouteLastUpdateAPI(routeID);
            Log.d(TAG, "isOnStorage - server LastUpdate: " + currentLastUpdate);
            Log.d(TAG, "isOnStorage - internal LastUpdate: " + route.getLastUpdate());
//            if (currentLastUpdate > route.getLastUpdate())
                // NEEDS UPDATE!
            return true;
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException: " + e.getMessage());
            return false;
        }
    }

    private void getFakeData() {
        List<Point> tmpList = new ArrayList<>();
        tmpList.add(new Point("Fórum Aveiro", 40.641475, -8.653675));
        tmpList.add(new Point("Praça do Peixe", 40.642313, -8.655352));
        tmpList.add(new Point("Estação de Comboios", 40.643304, -8.641302));
        tmpList.add(new Point("Sé de Aveiro", 40.639469, -8.650397));

        route = new Route(1,
                "Test Route",
                "Welcome to the test route! Here it is how a route will look like in the SpatialGuide app.",
                "https://i0.wp.com/gazetarural.com/wp-content/uploads/2017/12/Aveiro-Ria.jpg",
                tmpList, 0, 20001024, System.currentTimeMillis());
    }

    private void updateUI() {
        Log.d(TAG, "route: "+ route);

        // Route Image - displayed on the top: CollapsingToolbar
        if (route.getRouteImage() != null) {
            Picasso.get()
                    .load(route.getRouteImage())
                    .placeholder(R.drawable.not_available)
                    .into(routeImage);
            routeImage.setVisibility(View.VISIBLE);
        }
        imageLayout.setVisibility(View.GONE);

        // Route Map Image - Static map from Google Maps API
        if(route.getRouteMapImage() != null) {
            Picasso.get()
                    .load(route.getRouteMapImage())
                    .placeholder(R.drawable.not_available)
                    .into(routeMapImage);
            mapLayout.setVisibility(View.VISIBLE);
        }
        mapProgress.setVisibility(View.GONE);

        // Setting the remaining texts (Route name, description, etc)
        routeDescription.setText(route.getRouteDescription());
        collapsingToolbarLayout.setTitle(route.getRouteName());
        String downloadStr = route.getRouteDownloads() + " " + getString(R.string.downloads);
        routeDownloads.setText(downloadStr);
        routeDate.setText(route.getRouteDate());
        detailsProgress.setVisibility(View.GONE);
        detailsLayout.setVisibility(View.VISIBLE);

        // Setting the point adapter and the recyclerview to receive route points
        pointList = route.getRoutePoints();
        pointAdapter = new PointAdapter(this, pointList);
        recyclerView.setAdapter(pointAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        pointsProgress.setVisibility(View.GONE);
        pointsLayout.setVisibility(View.VISIBLE);
    }

    private void getRouteDetailsAPI(int routeID) {
        Call<List<Route>> call = sgApiClient.getRoute(routeID);

        call.enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if (response.isSuccessful()) {
                    route = response.body().get(0);

                    /**
                     * TODO - check getFakeData() and updateUI() functions
                     */
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Log.d(TAG, "getRouteDetails - onFailure: " + t.getMessage());
            }
        });
    }

    private long getRouteLastUpdateAPI(int routeID) {
        Call<RequestBody> call = sgApiClient.getRouteLastUpdate(routeID);

        call.enqueue(new Callback<RequestBody>() {
            @Override
            public void onResponse(Call<RequestBody> call, Response<RequestBody> response) {
                if (response.isSuccessful()) {
                    // TODO - get lastUpdate value from a Route and return it
                }
            }

            @Override
            public void onFailure(Call<RequestBody> call, Throwable t) {
                Log.d(TAG, "getRouteLastUpdateAPI - onFailure: " + t.getMessage());
            }
        });

        return 0;
    }

    private void getRoutePointsAPI(int routeID) {
        Call<List<Point>> call = sgApiClient.getRoutePoints(routeID);

        call.enqueue(new Callback<List<Point>>() {
            @Override
            public void onResponse(Call<List<Point>> call, Response<List<Point>> response) {
                if (response.isSuccessful()) {
                    pointList = response.body();

                    /**
                     * TODO - check getFakeData() and updateUI() functions
                     */
//                    if (pointList.size() > 0) {
//                        pointAdapter.notifyDataSetChanged();
//                    } else {
//                        noRoutesText.setVisibility(View.VISIBLE);
//                    }
                }
            }

            @Override
            public void onFailure(Call<List<Point>> call, Throwable t) {
                Log.d(TAG, "getRoutePoints - onFailure: " + t.getMessage());
            }
        });
    }


}