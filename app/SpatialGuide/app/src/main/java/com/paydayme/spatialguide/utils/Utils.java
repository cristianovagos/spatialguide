package com.paydayme.spatialguide.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static com.paydayme.spatialguide.core.Constant.ERROR_DIALOG_REQUEST;

/**
 * Created by cvagos on 31-03-2018.
 */

public final class Utils {

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
}
