package com.paydayme.spatialguide.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.paydayme.spatialguide.core.Constant;

import java.util.Date;

import static com.paydayme.spatialguide.core.Constant.CONNECT_TO_MOBILE;
import static com.paydayme.spatialguide.core.Constant.CONNECT_TO_WIFI;
import static com.paydayme.spatialguide.core.Constant.NOT_CONNECTED;

public class NetworkUtil {
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {

        int conn = NetworkUtil.getConnectivityStatus(context);

        String status = null;
        if (conn == NetworkUtil.TYPE_WIFI) {
            //status = "Wifi enabled";
            status = CONNECT_TO_WIFI;
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            //status = "Mobile data enabled";
            System.out.println(CONNECT_TO_MOBILE);
            status = getNetworkClass(context);
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = NOT_CONNECTED;
        }

        return status + " / " + new Date().toString();
    }

    public static String getNetworkClass(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info == null || !info.isConnected())
            return "-"; //not connected
        if(info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if(info.getType() == ConnectivityManager.TYPE_MOBILE){
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                    return "4G";
                default:
                    return "UNKNOWN";
            }
        }
        return "UNKNOWN";
    }
}
