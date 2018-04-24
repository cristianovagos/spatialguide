package com.paydayme.spatialguide.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
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

    public static Call fetchJSONfromURL(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static <T> Collection<List<T>> generatePermutationsNoRepetition(Set<T> availableNumbers) {
        Collection<List<T>> permutations = new HashSet<>();

        for (T number : availableNumbers) {
            Set<T> numbers = new HashSet<>(availableNumbers);
            numbers.remove(number);

            if (!numbers.isEmpty()) {
                Collection<List<T>> childPermutations = generatePermutationsNoRepetition(numbers);
                for (List<T> childPermutation : childPermutations) {
                    List<T> permutation = new ArrayList<>();
                    permutation.add(number);
                    permutation.addAll(childPermutation);
                    permutations.add(permutation);
                }
            } else {
                List<T> permutation = new ArrayList<>();
                permutation.add(number);
                permutations.add(permutation);
            }
        }

        return permutations;
    }

    public static float distance(double x1, double y1, double x2, double y2)
    {
        float dist;
        dist = (float) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
        return dist;
    }
}
