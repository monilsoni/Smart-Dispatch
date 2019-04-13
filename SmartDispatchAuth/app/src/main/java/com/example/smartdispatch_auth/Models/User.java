package com.example.smartdispatch_auth.Models;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class User {

    protected String email, user_id, type, token; // So that the child functions can access these variables
    protected GeoPoint geoPoint;
    protected @ServerTimestamp
    Date timeStamp;

    public User() {
    }

    public User(String email, String user_id, String type, GeoPoint geoPoint, Date timeStamp) {
        this.email = email;
        this.user_id = user_id;
        this.type = type;
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}