package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation implements Parcelable {

    private GeoPoint geoPoint;
    private @ServerTimestamp Date timeStamp;
    private User user;

    public UserLocation(GeoPoint geoPoint, Date timeStamp, User user) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.user = user;
    }

    public UserLocation() {

    }

    protected UserLocation(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);

        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<UserLocation> CREATOR = new Creator<UserLocation>() {
        @Override
        public UserLocation createFromParcel(Parcel in) {
            return new UserLocation(in);
        }

        @Override
        public UserLocation[] newArray(int size) {
            return new UserLocation[size];
        }
    };

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", user=" + user +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());

        dest.writeParcelable(user, flags);
    }
}
