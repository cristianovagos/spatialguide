package com.paydayme.spatialguide.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
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
    private int routeID;

    @SerializedName("Name")
    private String routeName;

    @SerializedName("Description")
    private String routeDescription;

    @SerializedName("Route_Date")
    private String routeDate;

    @SerializedName("LastUpdate")
    private long lastUpdate;

    @SerializedName("Number_Downloads")
    private int routeDownloads;

    @SerializedName("Image")
    private String routeImage;

    @SerializedName("Points")
    private List<Point> routePoints;

    @SerializedName("Map_image")
    private String routeMapImage;

    private boolean isFavorite;

    /**
     * Route constructor
     *
     * ONLY FOR TESTING WITHOUT API CALLS
     */
    public Route(int routeID, String routeName, String routeDescription,
                 String routeImage, List<Point> routePoints, int routeDownloads, String routeDate, long lastUpdate) {
        this.routeID = routeID;
        this.routeName = routeName;
        this.routeDescription = routeDescription;
        this.routeImage = routeImage;
        this.routePoints = routePoints;
        this.routeMapImage = !this.routePoints.isEmpty() ? generateMapImage() : null;
        this.routeDownloads = routeDownloads;
        this.routeDate = routeDate;
        this.lastUpdate = lastUpdate;
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
