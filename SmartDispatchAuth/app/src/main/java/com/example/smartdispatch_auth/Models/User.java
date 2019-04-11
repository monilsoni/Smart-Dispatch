package com.example.smartdispatch_auth.Models;

public class User {

    protected String email, user_id; // So that the child functions can access these variables

    public User() {
    }

    public User(String email, String user_id) {
        this.email = email;
        this.user_id = user_id;
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


}