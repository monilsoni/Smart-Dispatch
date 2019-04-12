package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Hospital extends User implements Parcelable {

    public static final Creator<Hospital> CREATOR = new Creator<Hospital>() {
        @Override
        public Hospital createFromParcel(Parcel in) {
            return new Hospital(in);
        }

        @Override
        public Hospital[] newArray(int size) {
            return new Hospital[size];
        }
    };

    private String hospital_name;

    protected Hospital(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);

        hospital_name = in.readString();
        type = in.readString();
        email = in.readString();
        user_id = in.readString();
    }

    public Hospital() {
    }

    public Hospital(GeoPoint geoPoint, Date timeStamp, String hospital_name, String email, String user_id, String type) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.hospital_name = hospital_name;
        this.type = "hospital";
        this.email = email;
        this.user_id = user_id;
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "hospital_name='" + hospital_name + '\'' +
                ", email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", type='" + type + '\'' +
                ", geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                '}';
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

    public String getHospital_name() {
        return hospital_name;
    }

    public void setHospital_name(String hospital_name) {
        this.hospital_name = hospital_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());

        dest.writeString(hospital_name);
        dest.writeString(type);
        dest.writeString(email);
        dest.writeString(user_id);
    }
}
