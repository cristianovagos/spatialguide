package com.paydayme.spatialguide.core.api;

import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.activity.LoginActivity;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by cvagos on 21-03-2018.
 */

public interface SGApiClient {

    @Headers("Content-Type: application/json")
    @GET("route/{id}/?format=json")
    Call<List<Route>> getRoute(@Header("Authorization") String authKey, @Path("id") int id);

    /**
     * BACKEND TODO - needs to be done in the API!
     * Just return the value of last update of a route, given it's ID
     */
    @Headers("Content-Type: application/json")
    @GET("route/{id}/?format=json&fields=lastupdate")
    Call<RequestBody> getRouteLastUpdate(@Header("Authorization") String authKey, @Path("id") int id);

    @Headers("Content-Type: application/json")
    @GET("route/?format=json")
    Call<List<Route>> getRoutes(@Header("Authorization") String authKey);

    @Headers("Content-Type: application/json")
    @GET("route_points/{id}/?format=json")
    Call<List<Point>> getRoutePoints(@Header("Authorization") String authKey, @Path("id") int id);

    @Headers("Content-Type: application/json")
    @GET("point/?format=json")
    Call<List<Point>> getPoints(@Header("Authorization") String authKey);


    /**
     * (POST) Register API endpoint
     *
     * @param user the user to be registered
     * @return the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("register/")
    Call<User> registerUser(@Body User user);

    /**
     * Login API endpoint
     *
     * @param user the user to be authenticated
     * @return the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("login/")
    Call<ResponseBody> login(@Body User user);
}
