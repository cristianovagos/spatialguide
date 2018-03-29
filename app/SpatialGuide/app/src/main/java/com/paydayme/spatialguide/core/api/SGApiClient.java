package com.paydayme.spatialguide.core.api;

import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created by cvagos on 21-03-2018.
 */

public interface SGApiClient {
    @Headers("Content-Type: application/json")
    @GET("route/{id}/?format=json")
    Call<List<Route>> getRoute(@Path("id") int id);

    /**
     * TODO - needs to be done in the API!
     * Just return the value of last update of a route, given it's ID
     */
    @Headers("Content-Type: application/json")
    @GET("route/{id}/?format=json&fields=lastupdate")
    Call<RequestBody> getRouteLastUpdate(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @GET("route/?format=json")
    Call<List<Route>> getRoutes();

    @Headers("Content-Type: application/json")
    @GET("route_points/{id}/?format=json")
    Call<List<Point>> getRoutePoints(@Path("id") int id);

    @Headers("Content-Type: application/json")
    @GET("point/?format=json")
    Call<List<Point>> getPoints();
}
