package com.paydayme.spatialguide.model.routexl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class RouteXLRoute {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("arrival")
    @Expose
    private Integer arrival;

    @SerializedName("distance")
    @Expose
    private Double distance;
}
