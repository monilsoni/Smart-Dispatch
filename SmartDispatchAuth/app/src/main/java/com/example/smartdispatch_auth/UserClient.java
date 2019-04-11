package com.example.smartdispatch_auth;

import android.app.Application;

import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.User;

public class UserClient extends Application {

    private Requester requester = null;

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }
}