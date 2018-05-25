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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.core.service.DownloadService;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.model.download.Download;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    // TODO - add BroadcastReceiver to listen to internet connection, see LoginActivity and SignupActivity

    public static final String TAG = "RouteDetailsActivity";

    // API Stuff
    private SGApiClient sgApiClient;
    private Route route;

    // SharedPreferences to save authentication token
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    private String authenticationHeader;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private int routeSelected;
    private PointAdapter pointAdapter;
    private List<Point> pointList = new ArrayList<>();
    private boolean routeOnStorage = false;
    private long currentRouteLastUpdate = 0;
    private Route currentRoute;
    private boolean hasUpdate = false;

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
    @BindView(R.id.mapCard) CardView mapCard;
    @BindView(R.id.favoriteButton) LikeButton favoriteButton;
    @BindView(R.id.cardDetails) CardView cardDetails;
    @BindView(R.id.cardPoints) CardView cardPoints;
    @BindView(R.id.loadingLayout) RelativeLayout loadingLayout;
    @BindView(R.id.mainLayout) LinearLayout mainLayout;
    @BindView(R.id.progressMap) ProgressBar progressMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        ButterKnife.bind(this);

        // Get route selected on the previous screen
        Intent intent = getIntent();
        routeSelected = intent.getIntExtra("route", -1);

        if (routeSelected != -1) {
            init();
        } else {
            Log.e(TAG, "Invalid route!");
            AlertDialog dialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.not_auth_dialog_title))
                    .setMessage(getString(R.string.not_auth_dialog_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
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
                finish();
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
                if(alertDialog != null && alertDialog.isShowing()) return;
                fabButton.setEnabled(false);
                if (!routeOnStorage) {
                    //Ask the user if wants to download the route
                    alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
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
                    changeFontAlertDialog();
                } else {
                    //Ask the user if wants to navigate in this route
                    alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
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
                    changeFontAlertDialog();
                }
                fabButton.setEnabled(true);
            }
        });

        favoriteButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(final LikeButton likeButton) {
                likeButton.setEnabled(false);
                HashMap tmpMap = new HashMap(1);
                tmpMap.put("route", route.getRouteID());

                Call<ResponseBody> call = sgApiClient.markAsFavourite(authenticationHeader, tmpMap);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(RouteDetailsActivity.this, R.string.route_favorited_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "onFavoriteChanged - onResponse: " + response.errorBody().toString());
                            Toast.makeText(RouteDetailsActivity.this, R.string.route_favourite_error, Toast.LENGTH_SHORT).show();
                        }
                        likeButton.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "onFavoriteChanged - onFailure: " + t.getMessage());
                        Toast.makeText(RouteDetailsActivity.this, R.string.route_favourite_error, Toast.LENGTH_SHORT).show();
                        likeButton.setEnabled(true);
                    }
                });
            }

            @Override
            public void unLiked(final LikeButton likeButton) {
                likeButton.setEnabled(false);
                HashMap tmpMap = new HashMap(1);
                tmpMap.put("route", route.getRouteID());

                Call<ResponseBody> call = sgApiClient.markAsUnfavourite(authenticationHeader, tmpMap);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(RouteDetailsActivity.this, R.string.route_unfavourite_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "onFavoriteChanged - onResponse: " + response.errorBody().toString());
                            Toast.makeText(RouteDetailsActivity.this, R.string.route_unfavourite_error, Toast.LENGTH_SHORT).show();
                        }
                        likeButton.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "onFavoriteChanged - onFailure: " + t.getMessage());
                        Toast.makeText(RouteDetailsActivity.this, R.string.route_unfavourite_error, Toast.LENGTH_SHORT).show();
                        likeButton.setEnabled(true);
                    }
                });
            }
        });

        getRouteLastUpdateAPI(routeSelected);
        registerReceiver();
    }

    private void changeFontAlertDialog() {
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    private void getUserInfo() {
        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    User currentUser = response.body();
                    for(Integer favRoutes : currentUser.getFavoriteRoutes()) {
                        if (favRoutes.equals(route.getRouteID())) {
                            favoriteButton.setLiked(true);
                        }
                    }
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
                Integer index = intent.getIntExtra("index", -1);
                Integer total = intent.getIntExtra("total", -1);

                if(download.getProgress() == 100) {
                    if(index != null && total != null && total > 1)
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
            return true;
        } catch (IOException e) {
            Log.d(TAG, "isOnStorage IOException: " + e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "isOnStorage ClassNotFoundException: " + e.getMessage());
            return false;
        }
    }

    private void showUpdateRouteDialog() {
        alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                .setTitle(getString(R.string.update_dialog_title))
                .setMessage(getString(R.string.update_dialog_message))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        route = currentRoute;
                        onDownloadRoute();
                        getUserInfo();
                        updateUI();
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getUserInfo();
                        updateUI();
                    }
                })
                .setCancelable(false)
                .show();
        changeFontAlertDialog();
    }

    private void updateUI() {
        Log.d(TAG, "route: "+ route);

        // Route Image - displayed on the top: CollapsingToolbar
        if(!route.getRouteImage().isEmpty()) {
            Picasso.get()
                    .load(route.getRouteImage())
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.not_available)
                    .into(routeImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            collapsingToolbarLayout.setVisibility(View.VISIBLE);
                            loadingLayout.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                            fabButton.setVisibility(View.VISIBLE);

                            if(routeOnStorage)
                                fabText.setVisibility(View.VISIBLE);
                            else
                                fabButton.setImageResource(R.drawable.ic_download2);
                        }

                        @Override
                        public void onError(Exception e) {
                            collapsingToolbarLayout.setVisibility(View.VISIBLE);
                            loadingLayout.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                            fabButton.setVisibility(View.VISIBLE);

                            if(routeOnStorage)
                                fabText.setVisibility(View.VISIBLE);
                            else
                                fabButton.setImageResource(R.drawable.ic_download2);
                            Log.e(TAG, "updateUI - onError: error fetching image: " + e.getMessage());
                        }
                    });
        }

        // Route Map Image - Static map from Google Maps API
        if(!route.getRouteMapImage().isEmpty()) {
            Picasso.get()
                    .load(route.getRouteMapImage())
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.not_available)
                    .into(routeMapImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            progressMap.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            mapCard.setVisibility(View.GONE);
                        }
                    });
        }

        // Setting the remaining texts (Route name, description, etc)
        routeDescription.setText(route.getRouteDescription());
        collapsingToolbarLayout.setTitle(route.getRouteName());
        String downloadStr = route.getRouteDownloads() + " " + getString(R.string.downloads);
        routeDownloads.setText(downloadStr);
        routeDate.setText(route.getRouteDate());
        cardDetails.setVisibility(View.VISIBLE);

        // Setting the point adapter and the recyclerview to receive route points
        pointList = route.getRoutePoints();
        pointAdapter = new PointAdapter(this, pointList, false);
        recyclerView.setAdapter(pointAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if(pointList.isEmpty())
            noPointsText.setVisibility(View.VISIBLE);
        else
            noPointsText.setVisibility(View.GONE);

        cardPoints.setVisibility(View.VISIBLE);
    }

    private void getRouteDetailsAPI(int routeID) {
        Call<Route> call = sgApiClient.getRoute(authenticationHeader, routeID);

        call.enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if (response.isSuccessful()) {
                    route = response.body();
                    getUserInfo();
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
                    currentRoute = response.body();
                    currentRouteLastUpdate = currentRoute.getLastUpdate();

                    if(isOnStorage(routeSelected)) {
                        routeOnStorage = true;

                        // Verificar se existe update na rota / pontos
                        if(currentRouteLastUpdate > route.getLastUpdate())
                            hasUpdate = true;
                        for(Point point : route.getRoutePoints())
                            for(Point currentPoint : currentRoute.getRoutePoints())
                                if(point.getPointID() == currentPoint.getPointID() && currentPoint.getLastUpdate() > point.getLastUpdate())
                                    hasUpdate = true;

                        // se houver update na rota/pontos perguntar ao utilizador se quer atualizar
                        // caso contrario mantem-se com os dados que tem ate agora pois estao no dispositivo
                        if(hasUpdate) {
                            showUpdateRouteDialog();
                        } else {
                            getUserInfo();
                            updateUI();
                        }
                    } else {
                        // a rota nao existe no dispositivo, vamos buscar os dados à API
                        getRouteDetailsAPI(routeSelected);
                    }
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                Log.e(TAG, "onFailure: obtaining route last update " + t.getMessage());
                getUserInfo();
                updateUI();
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RouteDetailsActivity.this, RouteActivity.class));
        finish();
    }
}
