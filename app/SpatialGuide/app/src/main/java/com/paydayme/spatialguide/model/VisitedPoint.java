package com.paydayme.spatialguide.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

import lombok.Data;

@Data
public class VisitedPoint {
    @SerializedName("id")
    private int id;

    @SerializedName("Visit_Data")
    private Date visitData;
}
