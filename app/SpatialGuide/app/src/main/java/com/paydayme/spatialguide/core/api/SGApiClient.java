package com.paydayme.spatialguide.core.api;

import com.paydayme.spatialguide.model.Point;
import com.paydayme.spatialguide.model.Route;
import com.paydayme.spatialguide.model.User;
import com.paydayme.spatialguide.ui.activity.LoginActivity;

import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
     * Get a point with a specific ID
     *
     * @param authKey the authorization key
     * @param id the Point ID
     * @return the Point returned
     */
    @Headers("Content-Type: application/json")
    @GET("point/{id}/?format=json")
    Call<Point> getPoint(@Header("Authorization") String authKey, @Path("id") int id);

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
     * @return the new User
     */
    @Headers("Content-Type: application/json")
    @POST("register/")
    Call<User> registerUser(@Body User user);

    /**
     * Make a user sign in on the system
     *
     * @param user the user to be authenticated
     * @return HTTP Response of the request made with the JSON response body
     */
    @Headers("Content-Type: application/json")
    @POST("login/")
    Call<ResponseBody> login(@Body User user);

    /**
     * Make a user sign out of the system
     *
     * @param authKey the authorization key
     * @return HTTP Response of the request made with the JSON response body
     */
    @GET("logout/")
    Call<ResponseBody> logout(@Header("Authorization") String authKey);

    /**
     * Change user password
     *
     * @param passwordBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("changepass/")
    Call<ResponseBody> changePassword(@Body HashMap<String, Object> passwordBody);

    /**
     * Change user email
     *
     * @param emailBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("changeemail/")
    Call<ResponseBody> changeEmail(@Body HashMap<String, Object> emailBody);

    /**
     * Get user information (names, username, email, image, etc)
     *
     * @param authKey the authorization key
     * @return the User
     */
    @Headers("Content-Type: application/json")
    @GET("userinfo/")
    Call<User> getUserInfo(@Header("Authorization") String authKey);

    /**
     * Recover password
     *
     * @param recoverPasswordBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("recoverpass/")
    Call<ResponseBody> recoverPassword(@Body HashMap<String, Object> recoverPasswordBody);

    /**
     * Mark a point in the user favourites
     *
     * @param authKey the authorization key
     * @param favouriteBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("useraddfavourite/")
    Call<ResponseBody> markAsFavourite(@Header("Authorization") String authKey, @Body HashMap<String, Object> favouriteBody);

    /**
     * Unmark a point from user favourites
     *
     * @param authKey the authorization key
     * @param favouriteBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("userremovefavourite/")
    Call<ResponseBody> markAsUnfavourite(@Header("Authorization") String authKey, @Body HashMap<String, Object> favouriteBody);

    /**
     * Mark a point as visited
     *
     * @param authKey the authorization key
     * @param visitedBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("visitpoint/")
    Call<ResponseBody> markPointVisited(@Header("Authorization") String authKey, @Body HashMap<String, Object> visitedBody);

    /**
     * Send point suggestion
     *
     * @param authKey the authorization key
     * @param suggestionBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("suggest/")
    Call<ResponseBody> sendSuggestion(@Header("Authorization") String authKey, @Body HashMap<String, Object> suggestionBody);

    /**
     * Send comment
     *
     * @param authKey the authorization key
     * @param commentBody a map with the fields required for the request
     * @return HTTP Response of the request made
     */
    @Headers("Content-Type: application/json")
    @POST("comment/")
    Call<ResponseBody> sendComment(@Header("Authorization") String authKey, @Body HashMap<String, Object> commentBody);

    // TODO Backend
    @Multipart
    @POST("userimage/")
    Call<ResponseBody> changeUserImage(@Header("Authorization") String authKey, @Part MultipartBody.Part file, @Part("name") RequestBody name);
}
