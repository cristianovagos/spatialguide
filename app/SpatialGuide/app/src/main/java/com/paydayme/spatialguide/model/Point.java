package com.paydayme.spatialguide.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by cvagos on 20-03-2018.
 */

@Data
@NoArgsConstructor
public class Point implements Serializable{
    @SerializedName("id")
    private int pointID;

    @SerializedName("Name")
    private String pointName;

    @SerializedName("Url")
    private String pointURL;

    @SerializedName("Description")
    private String pointDescription;

    @SerializedName("Latitude")
    private double pointLatitude;

    @SerializedName("Longitude")
    private double pointLongitude;

    @SerializedName("Image")
    private String pointImage;

    @SerializedName("Sound")
    private String pointAudioURL;

    @SerializedName("Point_Date")
    private Date pointDate;

    @SerializedName("LastUpdate")
    private long lastUpdate;

    // TODO BACKEND - Needs to be stored if the point has been visited by the user
    private boolean pointVisited;

    public Point(String name, double latitude, double longitude) {
        this.pointName = name;
        this.pointLatitude = latitude;
        this.pointLongitude = longitude;
    }

    public Point(int pointID, String name, double latitude, double longitude, String audioURL) {
        this.pointID = pointID;
        this.pointName = name;
        this.pointLatitude = latitude;
        this.pointLongitude = longitude;
        this.pointAudioURL = audioURL;
    }

    @Override
    public String toString() {
        return "Point{" +
                "pointName='" + pointName + '\'' +
                ", pointLatitude=" + pointLatitude +
                ", pointLongitude=" + pointLongitude +
                '}';
    }
}
