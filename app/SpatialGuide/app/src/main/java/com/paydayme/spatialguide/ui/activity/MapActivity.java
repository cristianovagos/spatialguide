package com.paydayme.spatialguide.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.paydayme.spatialguide.BuildConfig;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.RouteXLApiClient;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.routexl.RouteXLRequest;
import com.paydayme.spatialguide.model.routexl.RouteXLResponse;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.ui.helper.RouteOrderRecyclerHelper;
import com.paydayme.spatialguide.utils.Utils;
import com.squareup.picasso.Picasso;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.DEFAULT_ZOOM_VALUE;
import static com.paydayme.spatialguide.core.Constant.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.paydayme.spatialguide.core.Constant.GOOGLE_DIRECTIONS_API_KEY;
import static com.paydayme.spatialguide.core.Constant.MINIMUM_DISPLACEMENT;
import static com.paydayme.spatialguide.core.Constant.REQUEST_CHECK_SETTINGS;
import static com.paydayme.spatialguide.core.Constant.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.paydayme.spatialguide.core.Constant.ROUTE_XL_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.ROUTE_XL_BASE_URL;
import static com.paydayme.spatialguide.core.Constant.UPDATE_INTERVAL_IN_MILLISECONDS;

/**
 * MapActivity
 *
 * @author cvagos
 *
 * The activity responsible for handling the Google Maps, and displaying info about
 * the SpatialGuide experience (location updates, etc).
 *
 * Fused Location Provider and Location Updates functioning based on Google example:
 * https://github.com/googlesamples/android-play-location/
 */
public class MapActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();

    // Keys for storing activity state in the Bundle.
//    private final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private final static String KEY_ROUTE = "route";

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;

    /**
     * The Google Map
     */
    private GoogleMap mMap;

    /**
     * Represents if the Maps camera has moved
     */
    private Boolean mCameraMoved;

    /**
     * The RouteID selected on {@link RouteDetailsActivity}
     */
    private Integer mRouteSelected;

    /**
     * The Route instance
     */
    private Route mRoute;

    /**
     * Flag to represent if the shortestPath is activated
     */
    private boolean shortestPath = false;

    /**
     * Instance of the interface responsible for connecting with RouteXL API
     */
    private RouteXLApiClient routeXLApiClient;

    /**
     * Time when the location was updated represented as a String.
     */
    private String mLastUpdateTime;

    /**
     * Alert dialog for user prompts (exit, on trigger area showing point info)
     */
    private AlertDialog dialog;

    // UI Widgets.
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.bottomNavView) BottomNavigationViewEx bottomNavigationViewEx;
    @BindView(R.id.toolbarRoutename) TextView toolbarRoutename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        init(savedInstanceState);
        initMap();
    }

    private void addMarkers() {
        for (Point p : mRoute.getRoutePoints()) {
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(p.getPointLatitude(), p.getPointLongitude()))
                    .title(p.getPointName());
            if(p.isPointVisited())
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Marker marker = mMap.addMarker(options);
            marker.setTag(p);
        }
    }

    private void init(Bundle savedInstanceState) {
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

        // Setting up bottom navigation view
        setupBottomNavigationView();

        mLastUpdateTime = "";

        Intent intent = getIntent();
        mRouteSelected = intent.getIntExtra("route", -1);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        initApis();

        Log.d(TAG, "route selected: " + mRouteSelected);

        //TODO - change THIS!! DUMMY DATA
//        mRoute = getRoute();
        List<Point> tmpList = new ArrayList<>();
        tmpList.add(new Point("Fórum Aveiro", 40.641475, -8.653675));
        tmpList.add(new Point("Praça do Peixe", 40.642313, -8.655352));
        tmpList.add(new Point("Estação de Comboios", 40.643304, -8.641302));
        tmpList.add(new Point("Sé de Aveiro", 40.639469, -8.650397));
        mRoute = new Route(1,
                "Test Route 1",
                "Welcome to the test route 1! Here it is how a route will look like in the SpatialGuide app.",
                "https://i0.wp.com/gazetarural.com/wp-content/uploads/2017/12/Aveiro-Ria.jpg",
                tmpList, 0, 20001024, System.currentTimeMillis());

        toolbarRoutename.setText(mRoute.getRouteName());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    private void initApis() {
        // Initialization of Retrofit object for handling the RouteXL API requests
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ROUTE_XL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        routeXLApiClient = retrofit.create(RouteXLApiClient.class);
    }

    private void setupBottomNavigationView() {
        Log.d(TAG, "setupBottomNavigationView: setting up bottom nav view");
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);

        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_sortroute: {
                        showRouteOrderDialog();
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mCameraMoved = false;
                // Returning false, as the normal behavior should occur
                return false;
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                mCameraMoved = true;
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "onMarkerClick: marker " + marker.getTag());
                showInfoDialog(false, (Point) marker.getTag());
                return false;
            }
        });
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        addMarkers();
    }

    private void initMap() {
        mCameraMoved = false;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private Route getRoute() {
        try {
            return (Route) InternalStorage.readObject(this, Constant.ROUTE_STORAGE_SEPARATOR + mRouteSelected);
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "ClassNotFoundException: " + e.getMessage());
        }
        return null;
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }

            // Update the value of mRouteSelected from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_ROUTE)) {
                mRouteSelected = savedInstanceState.getInt(KEY_ROUTE);
            }

            updateUI();
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        //TODO - setting smallest displacement (DEBUG)
//        mLocationRequest.setSmallestDisplacement(MINIMUM_DISPLACEMENT);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation().toString());

                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
            }
        };
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        updateUI();
                        break;
                }
                break;
        }
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((com.google.android.gms.common.api.ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MapActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateUI();
                    }
                });
    }

    /**
     * Updates all UI fields.
     */
    private void updateUI() {
        updateLocationUI();
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        Log.d(TAG, "updateLocationUI: updating location UI");
        if (mCurrentLocation == null)
            return;

        Log.d(TAG, "updateLocationUI: lat: " + mCurrentLocation.getLatitude() +
                ", lon: " + mCurrentLocation.getLongitude());
        Log.d(TAG, "updateLocationUI: " + mLastUpdateTime);

        // Check if the user moved the map (drag to see other stuff)
        if(!mCameraMoved) {
            moveCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM_VALUE);
        }

        DirectionsResult directionsResult = shortestPath ? getDirectionsDetails(mCurrentLocation, false) :
                getDirectionsDetails(mCurrentLocation, true);

        if(directionsResult != null) {
            if(shortestPath) {
                List<Point> pointList = new ArrayList<>();
                for(int i : directionsResult.routes[0].waypointOrder) {
                    pointList.add(mRoute.getRoutePoints().get(i));
                }
                pointList.add(mRoute.getRoutePoints().get(mRoute.getRoutePoints().size()-1));
            }

            mMap.clear();
            addPolyline(directionsResult, mMap);
            addMarkers();
        }

//        getOptimizedRoute(mCurrentLocation);

        // Check nearest location and if we are in trigger area
        Pair<Location, Point> locationPointPair = getNearestPointLocation(mCurrentLocation);
        if(mCurrentLocation.distanceTo(locationPointPair.first) <= Constant.TRIGGER_AREA_DISTANCE) {
            Log.d(TAG, "updateLocationUI: on trigger area of point " + locationPointPair.second.getPointName());

            // point visited
            locationPointPair.second.setPointVisited(true);

            // show the dialog with info of location
            showInfoDialog(true, locationPointPair.second);

            // TODO insert auralization trigger here
        }
    }

    private void getOptimizedRoute(Location mCurrentLocation) {
        Gson gson = new Gson();
        List<RouteXLRequest> requestList = new ArrayList<>();
        requestList.add(new RouteXLRequest("current", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));

        int i = 0;
        for(Point p : mRoute.getRoutePoints()) {
            requestList.add(new RouteXLRequest(String.valueOf(i), p.getPointLatitude(), p.getPointLongitude()));
            i++;
        }

        Log.d(TAG, "getOptimizedRoute: " + gson.toJson(requestList));
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), gson.toJson(requestList));

        retrofit2.Call<RouteXLResponse> call = routeXLApiClient.getOptimizedRoute(body, ROUTE_XL_AUTH_KEY);
        call.enqueue(new retrofit2.Callback<RouteXLResponse>() {
            @Override
            public void onResponse(retrofit2.Call<RouteXLResponse> call, retrofit2.Response<RouteXLResponse> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "getOptimizedRoute - onResponse: " + response.body().toString());
                } else {
                    Log.e(TAG, "getOptimizedRoute - onResponse: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<RouteXLResponse> call, Throwable t) {
                Log.e(TAG, "getOptimizedRoute - onFailure call: " + call.toString());
                Log.e(TAG, "getOptimizedRoute - onFailure: " + t.getMessage());
            }
        });

    }

    private DirectionsResult getDirectionsDetails(Location currentLocation, boolean optimized) {
        DateTime now = new DateTime();
        List<com.google.maps.model.LatLng> waypoints = new ArrayList<>();
        Point destination = mRoute.getRoutePoints().get(mRoute.getRoutePoints().size()-1);

        for (Point p : mRoute.getRoutePoints()) {
            if(p.equals(destination))
                continue;
            if(p.isPointVisited())
                continue;
            waypoints.add(new com.google.maps.model.LatLng(p.getPointLatitude(), p.getPointLongitude()));
        }

        try {
            DirectionsApiRequest request = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.WALKING)
                    .origin(new com.google.maps.model.LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .destination(new com.google.maps.model.LatLng(destination.getPointLatitude(), destination.getPointLongitude()))
                    .departureTime(now)
                    .waypoints(waypoints.toArray(new com.google.maps.model.LatLng[waypoints.size()]));
            if(optimized)
                request.optimizeWaypoints(true);
            return request.await();
        } catch (com.google.maps.errors.ApiException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        mMap.addPolyline(new PolylineOptions().addAll(decodedPath).color(Color.BLUE));
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(3)
                .setApiKey(GOOGLE_DIRECTIONS_API_KEY)
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void showInfoDialog(boolean inLocation, final Point point) {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_layout, null);

        TextView dialogTitle = (TextView) view.findViewById(R.id.dialog_title);
        dialogTitle.setText(point.getPointName());

        TextView dialogText = (TextView) view.findViewById(R.id.dialog_text);
//        dialogText.setText("O Departamento de Eletrónica, Telecomunicações e Informática (DETI) foi fundado em 1974, " +
//                "com o nome de Departamento de Eletrónica e Telecomunicações, tendo sido um dos primeiros " +
//                "departamentos a iniciar atividade após a criação da Universidade de Aveiro em 1973. " +
//                "Em 2006 foi alterada a sua designação por forma a espelhar a atividade existente no Departamento na área da Informática.");
        dialogText.setText(point.getPointDescription());

        final ImageView dialogImage = (ImageView) view.findViewById(R.id.dialog_image);
        Picasso.get()
                .load(point.getPointImage())
                .placeholder(R.drawable.progress_animation)
                .into(dialogImage, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        dialogImage.setVisibility(View.GONE);
                    }
                });
//        ImageView dialogImage = (ImageView) view.findViewById(R.id.dialog_image);
//        dialogImage.setImageResource(R.drawable.deti);


        //Ask the user if they want to quit
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setView(view)
                .setIcon(R.mipmap.ic_info)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true);

        if(!point.getPointURL().isEmpty() || point.getPointURL() != null) {
            builder.setNeutralButton("Learn more", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String url = point.getPointURL();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
        }

        // Setting dialog title
        if (inLocation)
            builder.setTitle("You are at " + point.getPointName());
        else
            builder.setTitle(point.getPointName());

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // Setting custom font to dialog
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    private void showRouteOrderDialog() {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_route_order, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.routeDragRecyclerview);

        final PointAdapter pointAdapter = new PointAdapter(this, mRoute.getRoutePoints());
        recyclerView.setAdapter(pointAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Setting up the drag and drop functioning to the recycler view
        RouteOrderRecyclerHelper.RouteOrderRecyclerCallback callback = new RouteOrderRecyclerHelper.RouteOrderRecyclerCallback() {
            @Override
            public void onItemMove(int initialPosition, int finalPosition) {
                pointAdapter.onItemMove(initialPosition, finalPosition);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RouteOrderRecyclerHelper(callback));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        //Ask the user if they want to quit
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle("Change Route Order")
                .setView(view)
                .setNegativeButton("Shortest Path", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shortestPath = true;
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Adventure Mode", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Collections.shuffle(mRoute.getRoutePoints());
                        shortestPath = false;
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shortestPath = false;
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // Setting custom font to dialog
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    private Pair<Location, Point> getNearestPointLocation(Location currentLocation) {
        float minDistance = Float.MAX_VALUE;
        Location minLocation = new Location("");
        Point minPoint = new Point();
        for(Point point : mRoute.getRoutePoints()) {
            Location loc = new Location("");
            loc.setLongitude(point.getPointLongitude());
            loc.setLatitude(point.getPointLatitude());

            if(loc.distanceTo(currentLocation) < minDistance) {
                minDistance = loc.distanceTo(currentLocation);
                minLocation = loc;
                minPoint = point;
            }
        }
        return new Pair<>(minLocation, minPoint);
    }

    private void moveCamera(LatLng latLng, float defaultZoomValue) {
        Log.d(TAG, "moveCamera: moving camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoomValue));
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates: stopping location updates");
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "removeLocationUpdates: completed");
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resumed activity, checking permissions...");

        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (checkPermissions()) {
            Log.d(TAG, "onResume: permissions granted, starting location updates");
            startLocationUpdates();
        } else if (!checkPermissions()) {
            Log.d(TAG, "onResume: permissions not granted, asking...");
            requestPermissions();
        }

        Log.d(TAG, "onResume: updating UI");
        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: paused activity, stopping location updates");

        // Remove location updates to save battery.
        stopLocationUpdates();
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        savedInstanceState.putInt(KEY_ROUTE, mRouteSelected);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Starting location updates.");
                startLocationUpdates();
            } else {
                Log.i(TAG, "Permission denied.");
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
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
