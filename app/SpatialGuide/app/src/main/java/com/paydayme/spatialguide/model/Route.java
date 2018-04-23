package com.paydayme.spatialguide.model;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import lombok.Data;

import static com.paydayme.spatialguide.core.Constant.GOOGLE_MAPS_API_KEY;

/**
 * Created by cvagos on 20-03-2018.
 */
@Data
public class Route implements Serializable {
    @SerializedName("id")
    @Expose
    private int routeID;

    @SerializedName("Name")
    @Expose
    private String routeName;

    @SerializedName("Description")
    @Expose
    private String routeDescription;

    /**
     * FIELDS LEFT in API
     */
    private String routeImage;
    private List<Point> routePoints;
    private String routeMapImage;
    private int routeDownloads;
    private String routeDate;
    private long lastUpdate;

    /**
     * Route constructor
     *
     * ONLY FOR TESTING WITHOUT API CALLS
     */
    public Route(int routeID, String routeName, String routeDescription,
                 String routeImage, List<Point> routePoints, int routeDownloads, int routeDate, long lastUpdate) {
        this.routeID = routeID;
        this.routeName = routeName;
        this.routeDescription = routeDescription;
        this.routeImage = routeImage;
        this.routePoints = routePoints;
        this.routeMapImage = generateMapImage();
        this.routeDownloads = routeDownloads;
        this.routeDate = generateDate(routeDate);
        this.lastUpdate = lastUpdate;
    }

    private String generateDate(int date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat finalDate = new SimpleDateFormat("dd-MM-yyyy");
        Date d = null;
        try {
            d = df.parse(String.valueOf(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return finalDate.format(d);
    }

    private String generateMapImage() {
        if (routePoints.isEmpty()) return null;

        String url = "https://maps.googleapis.com/maps/api/staticmap?size=400x400&maptype=roadmap";
        for (Point p : routePoints) {
            url += "&markers=color:red%7C" + p.getPointLatitude() + "," + p.getPointLongitude();
        }
        url += "&key=" + GOOGLE_MAPS_API_KEY;

        return url;
    }
}
