package com.paydayme.spatialguide.core.api;

import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.activity.LoginActivity;

import java.util.HashMap;
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

    /**
     * Get a route with a specific ID
     *
     * @param authKey the authorization key
     * @param id the Route ID
     * @return the Route returned
     */
    @Headers("Content-Type: application/json")
    @GET("route/{id}/?format=json")
    Call<Route> getRoute(@Header("Authorization") String authKey, @Path("id") int id);

    /**
     * Get all Routes
     *
     * @param authKey the authorization key
     * @return a list with all Routes
     */
    @Headers("Content-Type: application/json")
    @GET("route/?format=json")
    Call<List<Route>> getRoutes(@Header("Authorization") String authKey);

    /**
     * Send location to generate heatmaps on backend
     *
     * @param authKey the authorization key
     * @param location the user location
     * @return the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("heatmap/")
    Call<ResponseBody> sendLocationHeatmap(@Header("Authorization") String authKey, @Body HashMap<String, Object> location);

    /**
     * Register
     *
     * @param user the user to be registered
     * @return the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("register/")
    Call<User> registerUser(@Body User user);

    /**
     * Login
     *
     * @param user the user to be authenticated
     * @return the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("login/")
    Call<ResponseBody> login(@Body User user);

    /**
     * Logout
     *
     * @param authKey the authorization key
     * @return the JSON response body
     */
    @GET("logout/")
    Call<ResponseBody> logout(@Header("Authorization") String authKey);
}
