package com.paydayme.spatialguide.model;

import android.content.Context;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.paydayme.spatialguide.core.storage.InternalStorage;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by cvagos on 20-03-2018.
 */

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
     */
    private String pointImage;

    /**
     * Point constructors
     *
     * ONLY FOR TESTING WITHOUT API CALLS
     */

    public Point () {}
    public Point(String name, double latitude, double longitude) {
        this.pointName = name;
        this.pointLatitude = latitude;
        this.pointLongitude = longitude;
    }

    /**
     * GETTERS
     */
    public int getPointID() {
        return pointID;
    }

    public String getPointName() {
        return pointName;
    }

    public String getPointURL() {
        return pointURL;
    }

    public String getPointDescription() {
        return pointDescription;
    }

    public double getPointLatitude() {
        return pointLatitude;
    }

    public double getPointLongitude() {
        return pointLongitude;
    }


    /**
     * SETTERS
     */
    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public void setPointURL(String pointURL) {
        this.pointURL = pointURL;
    }

    public void setPointDescription(String pointDescription) {
        this.pointDescription = pointDescription;
    }

    public void setPointLatitude(double pointLatitude) {
        this.pointLatitude = pointLatitude;
    }

    public void setPointLongitude(double pointLongitude) {
        this.pointLongitude = pointLongitude;
    }

    public void setPointImage(String pointImage) {
        this.pointImage = pointImage;
    }

    @Override
    public String toString() {
        return "Point{" +
                "pointID=" + pointID +
                ", pointName='" + pointName + '\'' +
                ", pointURL='" + pointURL + '\'' +
                ", pointDescription='" + pointDescription + '\'' +
                ", pointLatitude=" + pointLatitude +
                ", pointLongitude=" + pointLongitude +
                '}';
    }
}
