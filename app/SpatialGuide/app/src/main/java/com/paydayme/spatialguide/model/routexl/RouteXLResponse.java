package com.paydayme.spatialguide.model.routexl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Data;

@Data
public class RouteXLResponse {
    @SerializedName("feasible")
    @Expose
    private boolean feasible;

    @SerializedName("route")
    @Expose
    private List<RouteXLRoute> routes;
}

