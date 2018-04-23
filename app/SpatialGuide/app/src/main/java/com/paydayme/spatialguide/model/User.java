package com.paydayme.spatialguide.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Data;

@Data
public class User {

    @SerializedName("username")
    String username;

    @SerializedName("first_name")
    String first_name;

    @SerializedName("last_name")
    String last_name;

    @SerializedName("password")
    String password;

    @SerializedName("password2")
    String password2;

    @SerializedName("email")
    String email;

    @SerializedName("email2")
    String email2;

    public User(String username, String first_name, String last_name, String password, String email) {
        this.username = username;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.password2 = password;
        this.email = email;
        this.email2 = email;
    }

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
