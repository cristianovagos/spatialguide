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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import com.paydayme.spatialguide.core.service.DownloadService;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.download.Download;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.BASE_URL;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_DOWNLOAD_COMPLETED;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_ERROR_DOWNLOAD;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_MESSAGE_PROGRESS;
import static com.paydayme.spatialguide.core.Constant.BROADCAST_NO_POINTS;
import static com.paydayme.spatialguide.core.Constant.FILES_BASE_URL;
import static com.paydayme.spatialguide.core.Constant.ROUTE_STORAGE_SEPARATOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_LAST_ROUTE;

/**
 * Created by cvagos on 22-03-2018.
 */

public class RouteDetailsActivity extends AppCompatActivity {

    public static final String TAG = "RouteDetailsActivity";

    // API Stuff
    private SGApiClient sgApiClient;
    private Route route;

    // SharedPreferences to save authentication token
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;

    private ProgressDialog progressDialog;

    private int routeSelected;
    private PointAdapter pointAdapter;
    private List<Point> pointList = new ArrayList<>();
    private boolean routeOnStorage = false;
    private long currentRouteLastUpdate = 0;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.routeList) RecyclerView recyclerView;
    @BindView(R.id.noPointsText) TextView noPointsText;
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
    @BindView(R.id.mapCard) CardView mapCard;
    @BindView(R.id.layoutImage) LinearLayout imageLayout;
    @BindView(R.id.progressDetails) ProgressBar detailsProgress;
    @BindView(R.id.progressPoints) ProgressBar pointsProgress;

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

            getRouteDetailsAPI(routeSelected);
//            getFakeData();
        } else {
            Log.e(TAG, "Invalid route!");
            AlertDialog dialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    })
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
            textView.setTypeface(tf);
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        // Get Authentication Header from SharedPreferences
        authenticationHeader = sharedPreferences.getString(Constant.SHARED_PREFERENCES_AUTH_KEY, "");
        if(authenticationHeader.isEmpty()) {
            AlertDialog dialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RouteDetailsActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            TextView textView = (TextView) dialog.findViewById(android.R.id.message);
            textView.setTypeface(tf);
        }

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
                                    onDownloadRoute();
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
                                    onNavigateRoute();
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

        getRouteLastUpdateAPI(routeSelected);
        if(isOnStorage(routeSelected)) {
            routeOnStorage = true;
            fabText.setVisibility(View.VISIBLE);
        } else {
            fabButton.setImageResource(R.drawable.ic_download);
        }

        detailsProgress.setIndeterminate(true);
        pointsProgress.setIndeterminate(true);

        registerReceiver();
    }

    private void onNavigateRoute() {
        spEditor.putInt(SHARED_PREFERENCES_LAST_ROUTE, routeSelected);
        spEditor.apply();

        Intent intent = new Intent(RouteDetailsActivity.this, MapActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt("route", routeSelected);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void onDownloadRoute() {
        progressDialog = new ProgressDialog(RouteDetailsActivity.this, R.style.CustomDialogTheme);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Downloading files...");
        progressDialog.show();

        try {
            InternalStorage.writeObject(getApplicationContext(), Constant.ROUTE_STORAGE_SEPARATOR + routeSelected, route);
            Intent intent = new Intent(this, DownloadService.class);
            intent.putExtra("route", routeSelected);
            startService(intent);
        } catch (IOException e) {
            onDownloadFailed();
        }
    }

    private void registerReceiver() {
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_MESSAGE_PROGRESS);
        intentFilter.addAction(BROADCAST_ERROR_DOWNLOAD);
        intentFilter.addAction(BROADCAST_DOWNLOAD_COMPLETED);
        bManager.registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: intent " + intent.getAction());
            if(intent.getAction().equals(BROADCAST_MESSAGE_PROGRESS)){
                Download download = intent.getParcelableExtra("download");
                int index = intent.getIntExtra("index", -1);
                int total = intent.getIntExtra("total", -1);

                if(download.getProgress() == 100) {
                    progressDialog.setMessage("Downloading files...\n" + index + "/" + total + " completed");
                }
            } else if (intent.getAction().equals(BROADCAST_ERROR_DOWNLOAD)) {
                Log.e(TAG, "onReceive: going to on download failed");
                onDownloadFailed();
            } else if (intent.getAction().equals(BROADCAST_DOWNLOAD_COMPLETED)) {
                Log.d(TAG, "onReceive: download completed successfully");
                onDownloadCompleted();
            } else if (intent.getAction().equals(BROADCAST_NO_POINTS)) {
                Log.d(TAG, "onReceive: there are no points, no download");
                onDownloadNoPoints();
            }
        }
    };

    private void onDownloadNoPoints() {
        progressDialog.dismiss();
        Toast.makeText(RouteDetailsActivity.this, R.string.download_toast_no_points, Toast.LENGTH_SHORT).show();
    }

    private void onDownloadCompleted() {
        progressDialog.dismiss();
        routeOnStorage = true;
        fabButton.setImageDrawable(null);
        fabText.setVisibility(View.VISIBLE);
        Toast.makeText(RouteDetailsActivity.this, R.string.download_toast_completed, Toast.LENGTH_SHORT).show();
    }

    private void onDownloadFailed() {
        progressDialog.dismiss();
        InternalStorage.deleteFile(this, ROUTE_STORAGE_SEPARATOR + routeSelected);
        Toast.makeText(RouteDetailsActivity.this, R.string.download_toast_error, Toast.LENGTH_SHORT).show();
    }

    private boolean isOnStorage(int routeID) {
        try {
            route = (Route) InternalStorage.readObject(this, Constant.ROUTE_STORAGE_SEPARATOR + routeID);
            if(route.getRoutePoints().size() < 1)
                return false;
            for(Point p : route.getRoutePoints()) {
                try {
                    if(InternalStorage.getFile(this, Constant.POINT_STORAGE_SEPARATOR + p.getPointID() + ".wav") == null)
                        return false;
                } catch (Exception e) {
                    Log.d(TAG, "isOnStorage IOException: " + e.getMessage());
                    return false;
                }
            }

            Log.d(TAG, "isOnStorage - server LastUpdate: " + currentRouteLastUpdate);
            Log.d(TAG, "isOnStorage - internal LastUpdate: " + route.getLastUpdate());
            if (currentRouteLastUpdate > route.getLastUpdate()) {
                return false;
            }
            return true;
        } catch (IOException e) {
            Log.d(TAG, "isOnStorage IOException: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "isOnStorage ClassNotFoundException: " + e.getMessage());
            return false;
        }
    }

    private void getFakeData() {
        List<Point> tmpList = new ArrayList<>();
        tmpList.add(new Point(1,"Fórum Aveiro", 40.641475, -8.653675, "examples/mp3/SoundHelix-Song-1.mp3"));
        tmpList.add(new Point(2,"Praça do Peixe", 40.642313, -8.655352, "examples/mp3/SoundHelix-Song-2.mp3"));
        tmpList.add(new Point(3,"Estação de Comboios", 40.643304, -8.641302, "examples/mp3/SoundHelix-Song-3.mp3"));
        tmpList.add(new Point(4,"Sé de Aveiro", 40.639469, -8.650397, "examples/mp3/SoundHelix-Song-4.mp3"));

        route = new Route(1,
                "Test Route",
                "Welcome to the test route! Here it is how a route will look like in the SpatialGuide app.",
                "https://i0.wp.com/gazetarural.com/wp-content/uploads/2017/12/Aveiro-Ria.jpg",
                tmpList, 0, "2010-02-03", 1234567890);
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "route: "+ route);

        // Route Image - displayed on the top: CollapsingToolbar
        if(!route.getRouteImage().isEmpty()) {
            Picasso.get()
                    .load(route.getRouteImage())
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.not_available)
                    .into(routeImage);
            routeImage.setVisibility(View.VISIBLE);
        }
        imageLayout.setVisibility(View.GONE);

        // Route Map Image - Static map from Google Maps API
        if(route.getRouteMapImage() != null) {
            Picasso.get()
                    .load(route.getRouteMapImage())
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.not_available)
                    .into(routeMapImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            mapCard.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {}
                    });
        }

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
        pointAdapter = new PointAdapter(this, pointList, false);
        recyclerView.setAdapter(pointAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if(pointList.isEmpty()) {
            noPointsText.setVisibility(View.VISIBLE);
        } else {
            noPointsText.setVisibility(View.GONE);
            fabButton.setVisibility(View.VISIBLE);
        }
        pointsLayout.setVisibility(View.VISIBLE);
        pointsProgress.setVisibility(View.GONE);
    }

    private void getRouteDetailsAPI(int routeID) {
        Call<Route> call = sgApiClient.getRoute(authenticationHeader, routeID);

        call.enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if (response.isSuccessful()) {
                    route = response.body();
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                Log.e(TAG, "getRouteDetails - onFailure: " + t.getMessage());
            }
        });
    }

    private void getRouteLastUpdateAPI(int routeID) {
        Call<Route> call = sgApiClient.getRoute(authenticationHeader, routeID);

        call.enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if(response.isSuccessful()) {
                    currentRouteLastUpdate = response.body().getLastUpdate();
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                Log.e(TAG, "onFailure: obtaining route last update " + t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RouteDetailsActivity.this, RouteActivity.class));
    }
}
