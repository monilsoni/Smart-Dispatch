package com.example.smartdispatch_auth;

import android.app.Application;

import com.example.smartdispatch_auth.Models.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}