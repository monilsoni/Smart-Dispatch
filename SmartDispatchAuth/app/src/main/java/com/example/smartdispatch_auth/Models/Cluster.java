package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Cluster implements Parcelable {

    public static final Creator<Cluster> CREATOR = new Creator<Cluster>() {
        @Override
        public Cluster createFromParcel(Parcel in) {
            return new Cluster(in);
        }

        @Override
        public Cluster[] newArray(int size) {
            return new Cluster[size];
        }
    };
    private String id;
    private GeoPoint geoPoint;
    private ArrayList<Vehicle> vehicles;

    public Cluster(String id, GeoPoint geoPoint, ArrayList<Vehicle> vehicles) {
        this.id = id;
        this.geoPoint = geoPoint;
        this.vehicles = vehicles;
    }

    protected Cluster(Parcel in) {
        id = in.readString();
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);
        vehicles = in.createTypedArrayList(Vehicle.CREATOR);
    }

    public Cluster() {
    }

    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(ArrayList<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());
        dest.writeList(vehicles);

    }
}