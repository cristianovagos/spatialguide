package com.paydayme.spatialguide.core;

import android.net.ConnectivityManager;

/**
 * Created by cvagos on 20-03-2018.
 */

public final class Constant {
    /**
     * Base URL of SpatialGuide backend (for API usage)
     */
//    public static final String BASE_URL = "http://192.168.43.111:8000/";
//    public static final String BASE_URL = "http://192.168.1.19:8000/";
//    public static final String BASE_URL = "http://192.168.43.250:8000/";
    public static final String BASE_URL = "http://94.60.61.49";
//    public static final String BASE_URL = "http://192.168.1.99:8000/";

    /**
     * Base URL of media files (Google Drive)
     */
    public static final String FILES_BASE_URL = "http://drive.google.com/";
    public static final String FILES_BASE_URL_SEPARATOR = "uc?export=view&id=";

    /**
     * SpatialGuide Website to go on menu drawer click
     */
    public static final String SPATIALGUIDE_WEBSITE = "http://xcoa.av.it.pt/~pei2017-2018_g09/";

    /**
     * Google Maps and Directions API Keys.
     * Required to make requests to their APIs.
     */
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyDeglQrbGdL6r49ToFw2Ft1ugFJmma2oJM";
    public static final String GOOGLE_DIRECTIONS_API_KEY = "AIzaSyADfY5DLDSx8J7D-SOV5jyBSmxECHALwOo";

    /**
     * Base URL of RouteXL API (for route optimizations)
     */
    public static final String ROUTE_XL_BASE_URL = "https://api.routexl.nl";

    /**
     * RouteXL auth key
     */
    public static final String ROUTE_XL_AUTH_KEY = "Basic Y3ZhZ29zOiFwYXlkYXltZQ==";

    /**
     * Distance of the Trigger Area, the minimum area required for the spatial audio
     * and point of interest information to display in the screen.
     *
     * This is the default value.
     */
//    public static final int TRIGGER_AREA_DISTANCE = 25;
    public static final int TRIGGER_AREA_DISTANCE = 50;

    /**
     * Minimum and maximum values for trigger area distance.
     * These will appear on the settings screen for letting user customize the trigger area.
     */
    public static final int MIN_TRIGGER_AREA_VALUE = 1;
    public static final int MAX_TRIGGER_AREA_VALUE = 100;

    /**
     * String used to store/access the routes saved in internal device storage.
     * Ex: for the Route with ID 12 -> route-12
     */
    public static final String ROUTE_STORAGE_SEPARATOR = "route-";

    /**
     * String used to store/access the point audio file saved in internal device storage.
     * Ex: for the Point with ID 12 -> point-12
     */
    public static final String POINT_STORAGE_SEPARATOR = "point-";

    /**
     * IDs for push notifications
     */
    public static final String NOTIFICATION_CHANNEL_ID = "com.paydayme.spatialguide.notification_id";
    public static final String PUSHER_INSTANCE_ID = "1be0bfa7-2af5-4fe7-9e84-e78d07244959";

    /**
     * SharedPreferences keys
     * (auth key, remember me checkbox state, login username and password)
     */
    public static final String SHARED_PREFERENCES_AUTH_KEY = "com.paydayme.spatialguide.shared.auth-token";
    public static final String SHARED_PREFERENCES_REMEMBER_ME = "com.paydayme.spatialguide.shared.remember-me";
    public static final String SHARED_PREFERENCES_LOGIN_USERNAME = "com.paydayme.spatialguide.shared.login-username";
    public static final String SHARED_PREFERENCES_PASSWORD = "com.paydayme.spatialguide.shared.login-password";
    public static final String SHARED_PREFERENCES_LAST_ROUTE = "com.paydayme.spatialguide.shared.last_route";
    public static final String SHARED_PREFERENCES_RESET_ROUTE = "com.paydayme.spatialguide.shared.reset_route";
    public static final String SHARED_PREFERENCES_USER_NAMES = "com.paydayme.spatialguide.shared.user_names";
    public static final String SHARED_PREFERENCES_USER_IMAGE = "com.paydayme.spatialguide.shared.user_image";
    public static final String SHARED_PREFERENCES_USER_EMAIL = "com.paydayme.spatialguide.shared.user_email";
    public static final String SHARED_PREFERENCES_USERNAME = "com.paydayme.spatialguide.shared.username";

    /**
     * SharedPreferences Preference keys
     * keys from the {@link com.paydayme.spatialguide.ui.preferences.SGPreferencesActivity} class
     */
    public static final String SHARED_PREFS_AURALIZATION = "com.paydayme.spatialguide.prefs.auralization_engine";
    public static final String SHARED_PREFS_EXTERNAL_IMU = "com.paydayme.spatialguide.prefs.external_imu_device";
    public static final String SHARED_PREFS_HEATMAP = "com.paydayme.spatialguide.prefs.send_location_heatmap";
    public static final String SHARED_PREFS_TRAVEL_MODE = "com.paydayme.spatialguide.prefs.travel_mode";
    public static final String SHARED_PREFS_TRIGGER_AREA_VALUE = "com.paydayme.spatialguide.prefs.trigger_area";
    public static final String SHARED_PREFS_LOCATION_ACCURACY = "com.paydayme.spatialguide.prefs.location_accuracy";
    public static final String SHARED_PREFS_MAP_TYPE = "com.paydayme.spatialguide.prefs.map_type";
    public static final String SHARED_PREFS_MARKER_UNVISITED_COLOR = "com.paydayme.spatialguide.prefs.unvisited_marker_color";
    public static final String SHARED_PREFS_MARKER_VISITED_COLOR = "com.paydayme.spatialguide.prefs.visited_marker_color";
    public static final String SHARED_PREFS_DIRECTION_LINE_COLOR = "com.paydayme.spatialguide.prefs.direction_line_color";

    /**
     * Keys to be used on Intent filter of BroadcastReceiver
     */
    public static final String BROADCAST_MESSAGE_PROGRESS = "com.paydayme.spatialguide.intent.message_progress";
    public static final String BROADCAST_ERROR_DOWNLOAD = "com.paydayme.spatialguide.intent.error_download";
    public static final String BROADCAST_DOWNLOAD_COMPLETED = "com.paydayme.spatialguide.intent.download_completed";
    public static final String BROADCAST_NO_POINTS = "com.paydayme.spatialguide.intent.no_points";

    /**
     * Code used in requesting runtime permissions.
     */
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    /**
     * Constant used in the location settings dialog.
     */
    public static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Google Play Services error
     */
    public static final int ERROR_DIALOG_REQUEST = 9001;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The minimum displacement (distance in meters) that location updates must occur.
     */
    public static final long MINIMUM_DISPLACEMENT = 3;

    /**
     * The default zoom value for the Google Maps camera.
     */
    public static final float DEFAULT_ZOOM_VALUE = 16f;

    /**
     * Constants for Connectivity Receiver
     */
    public static final String CONNECT_TO_WIFI = "WIFI";
    public static final String CONNECT_TO_MOBILE = "MOBILE";
    public static final String NOT_CONNECTED = "NOT_CONNECT";
    public final static String CONNECTIVITY_ACTION = ConnectivityManager.CONNECTIVITY_ACTION;

    /**
     *
     */
    public final static String HEADSET_PLUG_ACTION = "android.intent.action.HEADSET_PLUG";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}
