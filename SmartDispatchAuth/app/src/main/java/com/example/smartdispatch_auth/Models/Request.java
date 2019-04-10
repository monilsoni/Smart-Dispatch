package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
    private UserLocation userLocation;
    private VehicleLocation vehicleLocation;
    private Hospital hospital;

    protected Request(Parcel in) {
        userLocation = in.readParcelable(UserLocation.class.getClassLoader());
        vehicleLocation = in.readParcelable(VehicleLocation.class.getClassLoader());
        hospital = in.readParcelable(Hospital.class.getClassLoader());
    }

    public Request() {
    }

    public Request(UserLocation userLocation, VehicleLocation vehicleLocation, Hospital hospital) {
        this.userLocation = userLocation;
        this.vehicleLocation = vehicleLocation;
        this.hospital = hospital;
    }

    @Override
    public String toString() {
        return "Request{" +
                "userLocation=" + userLocation +
                ", vehicleLocation=" + vehicleLocation +
                ", hospital=" + hospital +
                '}';
    }

    public UserLocation getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(UserLocation userLocation) {
        this.userLocation = userLocation;
    }

    public VehicleLocation getVehicleLocation() {
        return vehicleLocation;
    }

    public void setVehicleLocation(VehicleLocation vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(userLocation, flags);
        dest.writeParcelable(vehicleLocation, flags);
        dest.writeParcelable(hospital, flags);
    }
}
