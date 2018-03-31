package com.paydayme.spatialguide.core;

/**
 * Created by cvagos on 20-03-2018.
 */

public final class Constant {
    /**
     * URL Endpoint of SpatialGuide backend (for API usage)
     */
//    public static final String BASE_URL = "http://192.168.43.111:8000/";
    public static final String BASE_URL = "http://192.168.1.19:8000/";

    /**
     * Distance of the Trigger Area, the minimum area required for the spatial audio
     * and point of interest information to display in the screen.
     */
    public static final int TRIGGER_AREA_DISTANCE = 25;

    /**
     * String used to store/access the routes saved in internal device storage.
     * Ex: for the Route with ID 12 -> route-12
     */
    public static final String ROUTE_STORAGE_SEPARATOR = "route-";

    /**
     * Google Maps and Directions API Keys.
     * Required to make requests to their APIs.
     */
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyDeglQrbGdL6r49ToFw2Ft1ugFJmma2oJM";
    public static final String GOOGLE_DIRECTIONS_API_KEY = "AIzaSyADfY5DLDSx8J7D-SOV5jyBSmxECHALwOo";

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
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The default zoom value for the Google Maps camera.
     */
    public static final float DEFAULT_ZOOM_VALUE = 16f;
}
