package com.paydayme.spatialguide.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
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
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.maps.model.TravelMode;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.paydayme.spatialguide.BuildConfig;
import com.paydayme.spatialguide.R;
import com.paydayme.spatialguide.core.Constant;
import com.paydayme.spatialguide.core.api.RouteXLApiClient;
import com.paydayme.spatialguide.core.api.SGApiClient;
import com.paydayme.spatialguide.core.auralizationEngine.AuralizationEngine;
import com.paydayme.spatialguide.core.bluetooth.SGBluetoothService;
import com.paydayme.spatialguide.core.storage.InternalStorage;
import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.model.routexl.RouteXLRequest;
import com.paydayme.spatialguide.model.routexl.RouteXLResponse;
import com.paydayme.spatialguide.ui.adapter.BTDeviceAdapter;
import com.paydayme.spatialguide.ui.adapter.PointAdapter;
import com.paydayme.spatialguide.ui.helper.RouteOrderRecyclerHelper;
import com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity;
import com.squareup.picasso.Picasso;
import com.uncopt.android.widget.text.justify.JustifiedTextView;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.paydayme.spatialguide.core.Constant.DEFAULT_ZOOM_VALUE;
import static com.paydayme.spatialguide.core.Constant.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.paydayme.spatialguide.core.Constant.GOOGLE_DIRECTIONS_API_KEY;
import static com.paydayme.spatialguide.core.Constant.POINT_STORAGE_SEPARATOR;
import static com.paydayme.spatialguide.core.Constant.REQUEST_CHECK_SETTINGS;
import static com.paydayme.spatialguide.core.Constant.REQUEST_PERMISSIONS_REQUEST_CODE;
import static com.paydayme.spatialguide.core.Constant.ROUTE_XL_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.ROUTE_XL_BASE_URL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_EMAIL;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_IMAGE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_USER_NAMES;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_AURALIZATION;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_DIRECTION_LINE_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_HEATMAP;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_LOCATION_ACCURACY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MAP_TYPE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MARKER_UNVISITED_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MARKER_VISITED_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_TRAVEL_MODE;
import static com.paydayme.spatialguide.core.Constant.SPATIALGUIDE_WEBSITE;
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
    private final static String KEY_LOCATION = "location";
    private final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    private final static String KEY_ROUTE = "route";

    // Provides access to the Fused Location Provider API.
    private FusedLocationProviderClient mFusedLocationClient;

    // Provides access to the Location Settings API.
    private SettingsClient mSettingsClient;

    // Stores parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;

    // Stores the types of location services the client is interested in using. Used for checking
    // settings to determine if the device has optimal location settings.
    private LocationSettingsRequest mLocationSettingsRequest;

    // Callback for Location events.
    private LocationCallback mLocationCallback;

    //Represents a geographical location.
    private Location mCurrentLocation;

    // The Google Map
    private GoogleMap mMap;

    // Represents if the Maps camera has moved
    private Boolean mCameraMoved;

    // The RouteID selected on {@link RouteDetailsActivity}
    private Integer mRouteSelected;

    // The Route instance
    private Route mRoute;

    // Flag to represent if the shortestPath is activated
    private boolean shortestPath = false;

    // Instance of the interface responsible for connecting with RouteXL API
    private RouteXLApiClient routeXLApiClient;

    // Instance of the interface responsible for connecting with SpatialGuide API
    private SGApiClient sgApiClient;

    // Time when the location was updated represented as a String.
    private String mLastUpdateTime;

    // Alert dialog for user prompts (exit, on trigger area showing point info)
    private AlertDialog dialog;

    // Marker that will appear on Google Map when user click
    private Marker markerUserClick;

    // For getting User info
    private User currentUser;

    // Variable to store last time a point was visited
    private long lastTimestamp;
    private long heatmapTimestamp;

    private List<Integer> favoritePoints;


    // SharedPreferences object
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spEditor;

    // Variables that will take impact due to
    // sharedPreferences values from SGPreferenceActivity
    private boolean prefs_auralization;
    private boolean prefs_heatmap;
    private String prefs_travelmode;
    private String prefs_location_accuracy;
    private String prefs_map_type;
    private String prefs_unvisited_marker_color;
    private String prefs_visited_marker_color;
    private String prefs_direction_line_color;

    // Variables from SharedPreferences
    private String authenticationHeader;

    // UI Widgets.
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    @BindView(R.id.bottomNavView) BottomNavigationViewEx bottomNavigationViewEx;
    @BindView(R.id.toolbarRoutename) TextView toolbarRoutename;

    // UI from Header Menu
    private TextView userNameMenu;
    private CircleImageView userImageMenu;
    private TextView userEmailMenu;
    private LinearLayout menuErrorLayout;

    // Bluetooth and IMU Stuff

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 3;
    private static final int REQUEST_ENABLE_BT = 4;

    private float [] rotation = {0f, 0f, 0f};
    private BluetoothAdapter bluetoothAdapter = null;
    private String connectedDeviceName = null;
    private SGBluetoothService bluetoothService;
    private AuralizationEngine auralizationEngine;
    private Location lastLocation;

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
            if(p.isPointVisited()) {
                switch (Integer.valueOf(prefs_visited_marker_color)) {
                    case 1:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        break;
                    case 2:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        break;
                    case 3:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                        break;
                    case 4:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        break;
                    case 5:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        break;
                    case 6:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        break;
                    case 7:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                        break;
                    case 8:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                        break;
                    case 9:
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                        break;
                    default:
                        Log.e(TAG, "addMarkers: error on setting the visited marker color");
                        break;
                }
            }

            switch (Integer.valueOf(prefs_unvisited_marker_color)) {
                case 1:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    break;
                case 2:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    break;
                case 3:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    break;
                case 4:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                    break;
                case 5:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                    break;
                case 6:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    break;
                case 7:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
                    break;
                case 8:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                    break;
                case 9:
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                    break;
                default:
                    Log.e(TAG, "addMarkers: error on setting the unvisited marker color");
                    break;
            }
            Marker marker = mMap.addMarker(options);
            marker.setTag(p);
        }
    }

    private void init(Bundle savedInstanceState) {
        initMenuHeaderViews();

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

        // Getting SharedPreferences and their values
        initSharedPreferences();

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        initApis();

        getUserInfo();

        mRoute = getRoute();

        toolbarRoutename.setText(mRoute.getRouteName());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

        configBluetooth();
    }

    private void configBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter == null) {
            spEditor.putBoolean(SHARED_PREFS_AURALIZATION, false);
            spEditor.apply();
            prefs_auralization = false;
            Log.e(TAG, "configBluetooth: bluetooth is not available on this device");
        }

        ensureDiscoverable();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // If the BT is not ON, request for being enabled
        if(!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        } else if (bluetoothService == null) {
            setupBluetoothService();
        }
    }

    private void setupBluetoothService() {
        bluetoothService = new SGBluetoothService(this, bluetoothHandler);
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler bluetoothHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            StringBuilder stringBuilder = new StringBuilder();

            switch (msg.what) {
                case Constant.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case SGBluetoothService.STATE_CONNECTED:
                            Log.d(TAG, "handleMessage: connected");
                            break;
                        case SGBluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "handleMessage: connecting");
                            break;
                        case SGBluetoothService.STATE_LISTEN:
                        case SGBluetoothService.STATE_NONE:
                            Log.d(TAG, "handleMessage: not connected");
                            break;
                    }
                    break;
                case Constant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    stringBuilder.append(readMessage + "\n");
                    convertData(readMessage);

                    if(auralizationEngine != null && auralizationEngine.isPlaying()) {
                        Log.d(TAG, "handleMessage: updating auralization engine");

                        Log.d(TAG, "handleMessage: Rotations: " + rotation[0] + "Y: " + rotation[1] + "Z: " + rotation[2]);
                        if(!auralizationEngine.update(lastLocation.getLongitude(),
                                lastLocation.getLatitude(), mCurrentLocation.getLongitude(),
                                mCurrentLocation.getLatitude(), rotation[0], rotation[1], rotation[2])) {
                            Log.d(TAG, "handleMessage: stopping auralization engine");
                            auralizationEngine.StopAndUnload();
                            auralizationEngine = null;
                        }
                    }
                    break;
                case Constant.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = msg.getData().getString(Constant.DEVICE_NAME);
                    break;
                case Constant.MESSAGE_TOAST:
                    Toast.makeText(MapActivity.this, msg.getData().getString(Constant.TOAST),
                                Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * Establish connection with other device
     *
     * @param address The b
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothService.connect(device, secure);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    private void initMenuHeaderViews() {
        View header = navigationView.getHeaderView(0);
        userNameMenu = (TextView) header.findViewById(R.id.userNameMenu);
        userImageMenu = (CircleImageView) header.findViewById(R.id.userImageMenu);
        userEmailMenu = (TextView) header.findViewById(R.id.userEmailMenu);
        menuErrorLayout = (LinearLayout) header.findViewById(R.id.menuErrorLayout);
    }

    private void initSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sharedPreferences.edit();

        prefs_heatmap = sharedPreferences.getBoolean(SHARED_PREFS_HEATMAP, false);
        prefs_auralization = sharedPreferences.getBoolean(SHARED_PREFS_AURALIZATION, true);
        prefs_map_type = sharedPreferences.getString(SHARED_PREFS_MAP_TYPE, "1");
        prefs_location_accuracy = sharedPreferences.getString(SHARED_PREFS_LOCATION_ACCURACY, "1");
        prefs_travelmode = sharedPreferences.getString(SHARED_PREFS_TRAVEL_MODE, "1");
        prefs_unvisited_marker_color = sharedPreferences.getString(SHARED_PREFS_MARKER_UNVISITED_COLOR, "1");
        prefs_visited_marker_color = sharedPreferences.getString(SHARED_PREFS_MARKER_VISITED_COLOR, "1");
        prefs_direction_line_color = sharedPreferences.getString(SHARED_PREFS_DIRECTION_LINE_COLOR, "1");

        authenticationHeader = sharedPreferences.getString(SHARED_PREFERENCES_AUTH_KEY, "");
    }

    private void initApis() {
        // Initialization of Retrofit object for handling the RouteXL API requests
        Retrofit retrofitRouteXL = new Retrofit.Builder()
                .baseUrl(ROUTE_XL_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        routeXLApiClient = retrofitRouteXL.create(RouteXLApiClient.class);

        Retrofit retrofitSGApi = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        sgApiClient = retrofitSGApi.create(SGApiClient.class);
    }

    private void getUserInfo() {
        Call<User> call = sgApiClient.getUserInfo(authenticationHeader);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.isSuccessful()) {
                    currentUser = response.body();

                    favoritePoints = currentUser.getFavoritePoints();

                    String userNames = currentUser.getFirst_name() + " " + currentUser.getLast_name();
                    spEditor.putString(SHARED_PREFERENCES_USER_NAMES, userNames);
                    spEditor.putString(SHARED_PREFERENCES_USER_EMAIL, currentUser.getEmail());
                    spEditor.putString(SHARED_PREFERENCES_USER_IMAGE, currentUser.getUserImage());

                    onUpdateUserInfo();
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

    private void onUpdateUserInfo() {
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

    private void setupBottomNavigationView() {
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

    private boolean checkMapReady() {
        if(mMap == null) {
            Toast.makeText(this, getString(R.string.map_not_ready), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setMapToolbarEnabled(false);

        switch (Integer.valueOf(prefs_map_type)) {
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 3:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case 4:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                Log.e(TAG, "onMapReady: failed to set map type");
                break;
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(markerUserClick != null)
                    markerUserClick.remove();
                markerUserClick = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.logo_launcher)));
                markerUserClick.setTag(latLng);
            }
        });
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
                try {
                    Point markerPoint = (Point) marker.getTag();
                    if(markerPoint != null) {
                        showInfoDialog(false, markerPoint);
                    }
                } catch (ClassCastException e) {
                    Log.d(TAG, "onMarkerClick: " + e.getMessage());
                }

                try {
                    LatLng markerLatLng = (LatLng) marker.getTag();
                    if(markerLatLng != null) {
                        showSuggestionDialog(markerLatLng);
                    }
                } catch (ClassCastException e) {
                    Log.d(TAG, "onMarkerClick: " + e.getMessage());
                }

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

        switch (Integer.valueOf(prefs_location_accuracy)) {
            case 1:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                break;
            case 2:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                break;
            case 3:
                mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
                break;
            default:
                Log.e(TAG, "createLocationRequest: error on setting location priority");
        }
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

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
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetoothService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    prefs_auralization = false;
                }
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
        if (mCurrentLocation == null)
            return;

        if (!checkMapReady())
            return;

        Log.d(TAG, "updateLocationUI: lat: " + mCurrentLocation.getLatitude() +
                ", lon: " + mCurrentLocation.getLongitude());
        Log.d(TAG, "updateLocationUI: " + mLastUpdateTime);

        // Check if the user moved the map (drag to see other stuff)
        if (!mCameraMoved) {
            moveCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), DEFAULT_ZOOM_VALUE);
        }

        // If user toggled heatmap feature, send location to API
        if (prefs_heatmap) {

            // Send heatmaps each minute
            if(System.currentTimeMillis() - heatmapTimestamp > 60000) {
                heatmapTimestamp = System.currentTimeMillis();
                sendLocationHeatmap(mCurrentLocation);
            }
        }

        DirectionsResult directionsResult = shortestPath ? getDirectionsDetails(mCurrentLocation, false) :
                getDirectionsDetails(mCurrentLocation, true);

        if (directionsResult != null) {
            if (shortestPath) {
                List<Point> pointList = new ArrayList<>();
                for (int i : directionsResult.routes[0].waypointOrder) {
                    pointList.add(mRoute.getRoutePoints().get(i));
                }
                pointList.add(mRoute.getRoutePoints().get(mRoute.getRoutePoints().size() - 1));
                mRoute.setRoutePoints(pointList);
            }

            mMap.clear();
            addPolyline(directionsResult, mMap);
            addMarkers();
        }

        // Check nearest location and if we are in trigger area
        Pair<Location, Point> locationPointPair = getNearestPointLocation(mCurrentLocation);
        lastLocation = locationPointPair.first;
        if (mCurrentLocation.distanceTo(locationPointPair.first) <= Constant.TRIGGER_AREA_DISTANCE) {

            // if current time stamp is greater than 5 minutes (300000 ms)
            // then show the info dialog and do auralization
            if (locationPointPair.second != null && (System.currentTimeMillis() - lastTimestamp > 300000 || !locationPointPair.second.isPointVisited())) {
                // mark point as visited
                onPointVisited(locationPointPair.second);

                // update last timestamp
                lastTimestamp = System.currentTimeMillis();

                if (prefs_auralization && auralizationEngine == null) {
                    // TODO insert auralization trigger here

                    try {
                        File f = InternalStorage.getFile(this, POINT_STORAGE_SEPARATOR + locationPointPair.second.getPointID() + ".wav");
                        Log.d(TAG, "updateLocationUI: file: " + f.toString());
                        auralizationEngine = new AuralizationEngine(this, f.getAbsolutePath());
                    } catch (Exception e) {
                        prefs_auralization = false;
                    }
                }

                // show the dialog with info of location
                showInfoDialog(true, locationPointPair.second);
            }
        }
    }

    private void onPointVisited(Point point) {
        point.setPointVisited(true);

        HashMap tmpMap = new HashMap(1);
        tmpMap.put("point", point.getPointID());

        Call<ResponseBody> call = sgApiClient.markPointVisited(authenticationHeader, tmpMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()) {
                    Log.d(TAG, "onPointVisited - onResponse: point marked as visited in API");
                } else {
                    Log.e(TAG, "onPointVisited - onResponse: error while marking point as visited: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onPointVisited - onFailure: " + t.getMessage());
            }
        });
    }

    // TODO - try again :(
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
                    .origin(new com.google.maps.model.LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .destination(new com.google.maps.model.LatLng(destination.getPointLatitude(), destination.getPointLongitude()))
                    .departureTime(now)
                    .waypoints(waypoints.toArray(new com.google.maps.model.LatLng[waypoints.size()]));
            if(optimized)
                request.optimizeWaypoints(true);

            switch (Integer.valueOf(prefs_travelmode)) {
                case 1:
                    request.mode(TravelMode.WALKING);
                    break;
                case 2:
                    request.mode(TravelMode.DRIVING);
                    break;
                case 3:
                    request.mode(TravelMode.BICYCLING);
                    break;
            }

            return request.await();
        } catch (com.google.maps.errors.ApiException | InterruptedException | IOException e) {
            Log.e(TAG, "getDirectionsDetails: error getting the direction" + e.getMessage());
            return null;
        }
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        PolylineOptions options = new PolylineOptions().addAll(decodedPath);

        switch (Integer.valueOf(prefs_direction_line_color)) {
            case 1:
                options.color(Color.BLUE);
                break;
            case 2:
                options.color(Color.RED);
                break;
            case 3:
                options.color(Color.GREEN);
                break;
            case 4:
                options.color(Color.YELLOW);
                break;
            case 5:
                options.color(Color.BLACK);
                break;
            case 6:
                options.color(Color.CYAN);
                break;
            case 7:
                options.color(Color.MAGENTA);
                break;
            default:
                Log.e(TAG, "addPolyline: error setting color to the polyline");
                break;
        }

        mMap.addPolyline(options);
    }

    private GeoApiContext getGeoContext() {
        return new GeoApiContext.Builder()
                .queryRateLimit(3)
                .apiKey(GOOGLE_DIRECTIONS_API_KEY)
                .connectTimeout(1, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS).build();
    }

    private void showSuggestionDialog(final LatLng latLng) {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_suggestion, null);
        final AppCompatEditText suggestionText = (AppCompatEditText) view.findViewById(R.id.input_suggestion);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle("Suggest this location")
                .setView(view)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO backend
                        String suggestion = suggestionText.getText().toString();
                        Log.d(TAG, "show suggestion dialog onClick: " + suggestion);
                        Log.d(TAG, "suggestion position: lat: " + latLng.latitude + " lon: " + latLng.longitude);
                    }
                });

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // Setting custom font to dialog
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    private void showInfoDialog(boolean inLocation, final Point point) {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_point_info, null);

        TextView dialogTitle = (TextView) view.findViewById(R.id.dialog_title);
        dialogTitle.setText(point.getPointName());

        JustifiedTextView dialogText = (JustifiedTextView) view.findViewById(R.id.dialog_text);
        dialogText.setText(point.getPointDescription());

        final ImageView dialogImage = (ImageView) view.findViewById(R.id.dialog_image);
        if(!point.getPointImage().isEmpty()) {
            Picasso.get()
                    .load(point.getPointImage())
                    .into(dialogImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            dialogImage.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
        }

        LikeButton favoriteButton = (LikeButton) view.findViewById(R.id.favoriteButton);
        if(favoritePoints.contains(point.getPointID()))
            favoriteButton.setLiked(true);
        favoriteButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                HashMap tmpMap = new HashMap(1);
                tmpMap.put("point", point.getPointID());

                Call<ResponseBody> call = sgApiClient.markAsFavourite(authenticationHeader, tmpMap);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(MapActivity.this, R.string.point_favorited_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "liked - onResponse: " + response.errorBody().toString());
                            Toast.makeText(MapActivity.this, R.string.point_favourite_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "liked - onFailure: " + t.getMessage());
                        Toast.makeText(MapActivity.this, R.string.point_favourite_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                HashMap tmpMap = new HashMap(1);
                tmpMap.put("point", point.getPointID());

                Call<ResponseBody> call = sgApiClient.markAsUnfavourite(authenticationHeader, tmpMap);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()) {
                            Toast.makeText(MapActivity.this, R.string.point_unfavourite_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "unliked - onResponse: " + response.errorBody().toString());
                            Toast.makeText(MapActivity.this, R.string.point_unfavourite_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "unliked - onFailure: " + t.getMessage());
                        Toast.makeText(MapActivity.this, R.string.point_unfavourite_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

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

        if(point.getPointURL() != null) {
            final String url = point.getPointURL();
            if(Patterns.WEB_URL.matcher(url).matches()) {
                builder.setNeutralButton("Learn more", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                });
            }
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

        final PointAdapter pointAdapter = new PointAdapter(this, mRoute.getRoutePoints(), true);
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
        lp.gravity = Gravity.CENTER;

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // Setting custom font to dialog
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface tf = ResourcesCompat.getFont(getApplicationContext(), R.font.catamaran);
        textView.setTypeface(tf);
    }

    private void showBluetoothDevicesDialog() {
        // Check if the dialog exists and if its showing
        if(dialog != null && dialog.isShowing()) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bt_connection, null);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.btRecyclerView);

        final BTDeviceAdapter btDeviceAdapter = new BTDeviceAdapter(this, bluetoothAdapter.getBondedDevices(),
                new BTDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice item) {
                bluetoothAdapter.cancelDiscovery();
                connectDevice(item.getAddress(), false);
            }
        });

        recyclerView.setAdapter(btDeviceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //Ask the user if they want to quit
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle("Select Bluetooth Device")
                .setView(view)
                .setCancelable(true);

        // Creating dialog and adjusting size
        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = 900;
        lp.height = 1300;
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, defaultZoomValue));
    }

    private void sendLocationHeatmap(Location mCurrentLocation) {
        HashMap tmpMap = new HashMap(2);
        tmpMap.put("Latitude", mCurrentLocation.getLatitude());
        tmpMap.put("Longitude", mCurrentLocation.getLongitude());

        Call<ResponseBody> call = sgApiClient.sendLocationHeatmap(authenticationHeader, tmpMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if(response.isSuccessful()) {} else {
                    Log.e(TAG, "sendLocationHeatmap - onResponse: failed to send location to API");
                    Log.e(TAG, "sendLocationHeatmap - onResponse: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "sendLocationHeatmap - onFailure: failed to send location" + t.getMessage());
            }
        });
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    private void stopLocationUpdates() {
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

        // Register sharedPreferences onChange listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener);

        if (checkPermissions()) {
            startLocationUpdates();
        } else if (!checkPermissions()) {
            requestPermissions();
        }

        if(bluetoothService != null) {
            if(bluetoothService.getState() == SGBluetoothService.STATE_NONE) {
                bluetoothService.start();
            }
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

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
            startActivity(new Intent(MapActivity.this, SGPreferencesActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
            case R.id.nav_userpanel:
                startActivity(new Intent(MapActivity.this, UserPanelActivity.class));
                break;
            case R.id.nav_route:
                startActivity(new Intent(MapActivity.this, RouteActivity.class));
                break;
            case R.id.nav_history:
                startActivity(new Intent(MapActivity.this, HistoryActivity.class));
                break;
            case R.id.nav_favorites:
                startActivity(new Intent(MapActivity.this, FavoritesActivity.class));
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
                    startActivity(new Intent(MapActivity.this, LoginActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "onFailure: failed to logout" + t.getMessage());
            }
        });
    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferencesListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case SHARED_PREFS_AURALIZATION:
                    prefs_auralization = sharedPreferences.getBoolean(SHARED_PREFS_AURALIZATION, true);
                    break;
                case SHARED_PREFS_HEATMAP:
                    prefs_heatmap = sharedPreferences.getBoolean(SHARED_PREFS_HEATMAP, false);
                    break;
                case SHARED_PREFS_LOCATION_ACCURACY:
                    prefs_location_accuracy = sharedPreferences.getString(SHARED_PREFS_LOCATION_ACCURACY, "1");
                    break;
                case SHARED_PREFS_TRAVEL_MODE:
                    prefs_travelmode = sharedPreferences.getString(SHARED_PREFS_TRAVEL_MODE, "1");
                    break;
                case SHARED_PREFS_MARKER_UNVISITED_COLOR:
                    prefs_unvisited_marker_color = sharedPreferences.getString(SHARED_PREFS_MARKER_UNVISITED_COLOR, "1");
                    break;
                case SHARED_PREFS_MARKER_VISITED_COLOR:
                    prefs_visited_marker_color = sharedPreferences.getString(SHARED_PREFS_MARKER_VISITED_COLOR, "1");
                    break;
                case SHARED_PREFS_DIRECTION_LINE_COLOR:
                    prefs_direction_line_color = sharedPreferences.getString(SHARED_PREFS_DIRECTION_LINE_COLOR, "1");
                    break;
                case SHARED_PREFS_MAP_TYPE:
                    prefs_map_type = sharedPreferences.getString(SHARED_PREFS_MAP_TYPE, "1");
                    break;
                default:
                    Log.e(TAG, "onSharedPreferenceChanged: some error occurred");
                    break;
            }
        }
    };

    //Convert the Data Received to Float
    private void convertData(String data) {
        String [] tmp = data.split("/");
        rotation[0] = Float.valueOf(tmp[0]);
        rotation[1] = Float.valueOf(tmp[1]);
        rotation[2] = Float.valueOf(tmp[2]);
        Log.d(TAG, "convertData: float converted 0: " + Math.toDegrees(rotation[0]));
        Log.d(TAG, "convertData: float converted 1: " + Math.toDegrees(rotation[1]));
        Log.d(TAG, "convertData: float converted 2: " + Math.toDegrees(rotation[2]));
    }
}
