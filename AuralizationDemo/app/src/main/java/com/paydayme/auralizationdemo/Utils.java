package com.paydayme.auralizationdemo;

/**
 * Created by cvagos on 31-03-2018.
 */

public final class Utils {
    private static final String TAG = "Utils";

    public static float distance(double x1, double y1, double x2, double y2)
    {
        float dist;
        dist = (float) Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
        return dist;
    }
}
