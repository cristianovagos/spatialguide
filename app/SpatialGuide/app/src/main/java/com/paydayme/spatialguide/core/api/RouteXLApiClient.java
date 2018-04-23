package com.paydayme.spatialguide.core.api;

import com.paydayme.spatialguide.model.routexl.RouteXLResponse;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface RouteXLApiClient {

    @FormUrlEncoded
    @POST("/tour")
    Call<RouteXLResponse> getOptimizedRoute(@Field("locations") RequestBody param, @Header("Authorization") String authKey);

}
