package com.paydayme.spatialguide.model;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.paydayme.spatialguide.core.storage.InternalStorage;

import java.io.IOException;
import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by cvagos on 20-03-2018.
 */

@Data
@NoArgsConstructor
public class Point implements Serializable{
    @SerializedName("id")
    @Expose
    private int pointID;

    @SerializedName("Name")
    @Expose
    private String pointName;

    @SerializedName("Url")
    @Expose
    private String pointURL;

    @SerializedName("Description")
    @Expose
    private String pointDescription;

    @SerializedName("Latitude")
    @Expose
    private double pointLatitude;

    @SerializedName("Longitude")
    @Expose
    private double pointLongitude;

    /**
     * NEEDS connection to API
     * TODO BACKEND
     */
    private String pointImage;

    // TODO BACKEND
    private String pointAudioURL;

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
