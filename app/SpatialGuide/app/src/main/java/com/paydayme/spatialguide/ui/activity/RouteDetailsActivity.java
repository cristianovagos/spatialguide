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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
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
import com.paydayme.spatialguide.model.VisitedPoint;
import com.paydayme.spatialguide.model.download.Download;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.utils.NetworkUtil;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import static com.paydayme.spatialguide.core.Constant.CONNECTIVITY_ACTION;
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
    private AlertDialog alertDialog;
    private AlertDialog connectionDialog;

    private IntentFilter intentFilter;

    private int routeSelected;
    private boolean routeOnStorage = false;
    private Route currentRoute;
    private boolean hasUpdate = false;
    private List<Point> visitedPointsList = new ArrayList<>();
    private boolean noPoints = false;

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
    @BindView(R.id.loadingLayout) RelativeLayout loadingLayout;
    @BindView(R.id.mainLayout) LinearLayout mainLayout;
    @BindView(R.id.progressMap) ProgressBar progressMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        intentFilter = new IntentFilter(CONNECTIVITY_ACTION);

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
                                    Answers.getInstance().logCustom(new CustomEvent("Download Route")
                                            .putCustomAttribute("Route", route.getRouteName()));
                                    onDownloadRoute();
                                }
                            })
                            .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();
                    changeFontAlertDialog();
                } else {
                    if(visitedPointsList.size() == route.getRoutePoints().size()) {
                        // todos os pontos foram visitados
                        alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                                .setTitle("Reset this Route")
                                .setMessage("Looks like you've already visited all points of this route. You want to start it all again or continue?")
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue, normal behavior
                                        onNavigateRoute(false);
                                    }
                                })
                                .setNegativeButton("Reset Route", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onNavigateRoute(true);
                                    }
                                })
                                .setCancelable(false)
                                .show();
                        changeFontAlertDialog();
                    } else if(!visitedPointsList.isEmpty()) {
                        // perguntar se quer continuar a rota ou começar do inicio
                        alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                                .setTitle("Reset this Route")
                                .setMessage("Looks like you've already visited some points of this route. You want to start it all again or continue travelling?")
                                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // continue, normal behavior
                                        onNavigateRoute(false);
                                    }
                                })
                                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onNavigateRoute(true);
                                    }
                                })
                                .setCancelable(false)
                                .show();
                        changeFontAlertDialog();
                    } else {
                        showNavigateDialog();
                    }
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
                            Answers.getInstance().logCustom(new CustomEvent("Route Favorited")
                                    .putCustomAttribute("Route", route.getRouteName()));
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
                            Answers.getInstance().logCustom(new CustomEvent("Route Unfavorited")
                                    .putCustomAttribute("Route", route.getRouteName()));
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

        getRouteAPI(routeSelected);
        registerReceiver();
    }

    private void showNavigateDialog() {
        //Ask the user if wants to navigate in this route
        alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                .setTitle(getString(R.string.navigate_route))
                .setMessage(getString(R.string.navigate_route_prompt))
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onNavigateRoute(false);
                    }
                })
                .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
        changeFontAlertDialog();
    }

    private void getRouteAPI(int routeID) {
        Call<Route> call = sgApiClient.getRoute(authenticationHeader, routeID);
        call.enqueue(new Callback<Route>() {
            @Override
            public void onResponse(Call<Route> call, Response<Route> response) {
                if(response.isSuccessful() && response.body() != null) {
                    if(isOnStorage(routeSelected)) {
                        Log.d(TAG, "getRouteAPI - onResponse: route on storage");
                        currentRoute = response.body();
                        routeOnStorage = true;

                        // Verificar se existe update na rota
                        if(response.body().getLastUpdate() > route.getLastUpdate())
                            hasUpdate = true;
                        else {
                            // Verificar se existe update nos pontos
                            for (Point point : route.getRoutePoints())
                                for (Point currentPoint : response.body().getRoutePoints())
                                    if (point.getPointID() == currentPoint.getPointID() && currentPoint.getLastUpdate() > point.getLastUpdate()) {
                                        hasUpdate = true;
                                        break;
                                    }
                        }

                        // se houver update na rota/pontos perguntar ao utilizador se quer atualizar
                        // caso contrario mantem-se os dados que tem ate agora pois estao no dispositivo
                        if(hasUpdate) {
                            Log.d(TAG, "getRouteAPI - onResponse: route has update");
                            showUpdateRouteDialog();
                            return;
                        }
                        getUserInfo();
                        return;
                    }
                    Log.d(TAG, "getRouteAPI - onResponse: route not on storage");
                    route = response.body();
                    getUserInfo();
                } else {
                    Log.e(TAG, "getRouteAPI - onResponse: some error occurred");
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<Route> call, Throwable t) {
                Log.e(TAG, "getRouteAPI - onFailure: obtaining route: " + t.getMessage());
                updateUI();
            }
        });
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
                if(response.isSuccessful() && response.body() != null) {
                    // verificar as rotas favoritas para saber se a rota atual é favorita
                    for(int favRoutes : response.body().getFavoriteRoutes()) {
                        if (favRoutes == route.getRouteID()) {
                            // marcar o botao de favorito como liked
                            favoriteButton.setLiked(true);
                            break;
                        }
                    }
                    // verificar os pontos ja visitados
                    for(VisitedPoint visitedPoint : response.body().getVisitedPoints()) {
                        for(Point p : route.getRoutePoints()) {
                            if(p.getPointID() == visitedPoint.getId()) {
                                visitedPointsList.add(p);
                            }
                        }
                    }
                    updateUI();
                } else {
                    Log.e(TAG, "getUserInfo - onResponse: some error occurred: " + response.errorBody().toString());
                    updateUI();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "getUserInfo - onFailure: some error occurred: " + t.getMessage());
                updateUI();
            }
        });
    }

    private void onNavigateRoute(boolean reset) {
        Answers.getInstance().logCustom(new CustomEvent("Navigate Route")
                .putCustomAttribute("Route", route.getRouteName()));

        spEditor.putInt(SHARED_PREFERENCES_LAST_ROUTE, routeSelected);
        spEditor.apply();

        Intent intent = new Intent(RouteDetailsActivity.this, MapActivity.class);
        Bundle bundle = new Bundle();

        if(reset)
            bundle.putBoolean("reset_points", true);
        else
            bundle.putBoolean("reset_points", false);

        bundle.putInt("route", routeSelected);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void onDownloadRoute() {
        routeOnStorage = false;
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

                if(download.getProgress() == 100 && progressDialog != null) {
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
        Toast.makeText(RouteDetailsActivity.this, R.string.download_toast_no_points, Toast.LENGTH_LONG).show();
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

        AlertDialog downloadFailedDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                .setTitle(getString(R.string.download_failed_title))
                .setMessage(getString(R.string.download_failed_message))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
        TextView textView = (TextView) downloadFailedDialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
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
                        Answers.getInstance().logCustom(new CustomEvent("Route Updated")
                                .putCustomAttribute("Route", route.getRouteName()));
                        onDownloadRoute();
                    }
                })
                .setCancelable(false)
                .show();
        changeFontAlertDialog();
    }

    private void updateUI() {
        if(route != null) {
            // Route Image - displayed on the top: CollapsingToolbar
            if (!route.getRouteImage().isEmpty()) {
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
                                if(!noPoints)
                                    fabButton.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onError(Exception e) {
                                collapsingToolbarLayout.setVisibility(View.VISIBLE);
                                loadingLayout.setVisibility(View.GONE);
                                mainLayout.setVisibility(View.VISIBLE);
                                if(!noPoints)
                                    fabButton.setVisibility(View.VISIBLE);
                                Log.e(TAG, "updateUI - onError: error fetching image: " + e.getMessage());
                            }
                        });
            }


            if (!route.getRoutePoints().isEmpty()) {
                // Route Map Image - Static map from Google Maps API
                if (!route.getRouteMapImage().isEmpty()) {
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
                } else {
                    mapCard.setVisibility(View.GONE);
                }
                // Setting the point adapter and the recyclerview to receive route points
                recyclerView.setAdapter(new PointAdapter(this, route.getRoutePoints(), false));
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                noPoints = false;
            } else {
                fabButton.setVisibility(View.GONE);
                mapCard.setVisibility(View.GONE);
                noPointsText.setVisibility(View.VISIBLE);
                noPoints = true;
            }

            // Setting the remaining texts (Route name, description, etc)
            routeDescription.setText(route.getRouteDescription());
            collapsingToolbarLayout.setTitle(route.getRouteName());
            String downloadStr = route.getRouteDownloads() + " " + getString(R.string.downloads);
            routeDownloads.setText(downloadStr);
            routeDate.setText(route.getRouteDate());
            cardDetails.setVisibility(View.VISIBLE);
        } else {
            // a rota nao existe, ou houve erro
            alertDialog = new AlertDialog.Builder(RouteDetailsActivity.this, R.style.CustomDialogTheme)
                    .setTitle(getString(R.string.error_route_title))
                    .setMessage(getString(R.string.error_route_message))
                    .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RouteDetailsActivity.this, RouteActivity.class));
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
            changeFontAlertDialog();
            fabButton.setVisibility(View.GONE);
        }

        if (routeOnStorage)
            fabText.setVisibility(View.VISIBLE);
        else
            fabButton.setImageResource(R.drawable.ic_download2);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(RouteDetailsActivity.this, RouteActivity.class));
        finish();
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
            if(NetworkUtil.getConnectivityStatus(RouteDetailsActivity.this) == NetworkUtil.TYPE_NOT_CONNECTED) {
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
