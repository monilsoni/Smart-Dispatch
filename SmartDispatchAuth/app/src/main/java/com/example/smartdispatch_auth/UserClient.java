package com.example.smartdispatch_auth;

import android.app.Application;

import com.example.smartdispatch_auth.Models.Hospital;
import com.example.smartdispatch_auth.Models.Requester;
import com.example.smartdispatch_auth.Models.User;
import com.example.smartdispatch_auth.Models.Vehicle;

public class UserClient extends Application {

    private Requester requester = null;
    private Hospital hospital = null;
    private Vehicle vehicle = null;

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }
}