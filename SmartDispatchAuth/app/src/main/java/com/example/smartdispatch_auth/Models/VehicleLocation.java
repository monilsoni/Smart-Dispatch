package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class VehicleLocation implements Parcelable {

    public static final Creator<VehicleLocation> CREATOR = new Creator<VehicleLocation>() {
        @Override
        public VehicleLocation createFromParcel(Parcel in) {
            return new VehicleLocation(in);
        }

        @Override
        public VehicleLocation[] newArray(int size) {
            return new VehicleLocation[size];
        }
    };

    private GeoPoint geoPoint;
    private @ServerTimestamp
    Date timeStamp;
    private Vehicle vehicle;

    protected VehicleLocation(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);

        vehicle = in.readParcelable(Vehicle.class.getClassLoader());
    }

    public VehicleLocation() {
    }

    public VehicleLocation(GeoPoint geoPoint, Date timeStamp, Vehicle vehicle) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.vehicle = vehicle;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    @Override
    public String toString() {
        return "VehicleLocation{" +
                "geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", vehicle=" + vehicle +
                '}';
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());

        dest.writeParcelable(vehicle, flags);
    }
}
