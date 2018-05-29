package com.paydayme.spatialguide.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.paydayme.spatialguide.core.Constant.ERROR_DIALOG_REQUEST;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFERENCES_AUTH_KEY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_AURALIZATION;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_DIRECTION_LINE_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_EXTERNAL_IMU;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_HEATMAP;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_LOCATION_ACCURACY;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MAP_TYPE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MARKER_UNVISITED_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_MARKER_VISITED_COLOR;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_TRAVEL_MODE;
import static com.paydayme.spatialguide.core.Constant.SHARED_PREFS_TRIGGER_AREA_VALUE;
import static com.paydayme.spatialguide.core.Constant.TRIGGER_AREA_DISTANCE;

/**
 * Created by cvagos on 31-03-2018.
 */

public final class Utils {
    private static final String TAG = "Utils";

    public static boolean isServicesOK(Context context, String tag) {
        Log.d(tag, "isServicesOK: checking Google Services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        if (available == ConnectionResult.SUCCESS) {
            // Everything is fine and the user can make map requests
            Log.d(tag, "isServicesOK: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // An error occurred but we can fix it
            Log.d(tag, "isServicesOK: an error occurred but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog((Activity) context, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(context, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static float distance(double x1, double y1, double x2, double y2)
    {
        float dist;
        dist = (float) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
        return dist;
    }

    public static void deleteAllSharedPreferences(Context context) {
        Log.d(TAG, "deleteAllSharedPreferences: delete all sharedpreferences");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.clear().apply();
    }
}
