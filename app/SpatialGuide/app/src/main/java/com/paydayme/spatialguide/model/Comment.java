package com.paydayme.spatialguide.model;

import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class Comment {
    @SerializedName("id")
    private int id;

    @SerializedName("Point")
    private int pointID;

    @SerializedName("User")
    private String username;

    @SerializedName("Image")
    private String userimage;

    @SerializedName("Comment")
    private String comment;
}
