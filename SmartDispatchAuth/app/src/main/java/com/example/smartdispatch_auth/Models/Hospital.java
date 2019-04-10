package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Hospital implements Parcelable {

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
    private GeoPoint geoPoint;
    private @ServerTimestamp
    Date timeStamp;
    private String hospital_name;
    private String hospital_id;

    protected Hospital(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);

        hospital_name = in.readString();
        hospital_id = in.readString();
    }

    public Hospital() {
    }

    public Hospital(GeoPoint geoPoint, Date timeStamp, String hospital_name, String hospital_id) {
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.hospital_name = hospital_name;
        this.hospital_id = hospital_id;
    }

    public String getHospital_id() {
        return hospital_id;
    }

    public void setHospital_id(String hospital_id) {
        this.hospital_id = hospital_id;
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", hospital_name='" + hospital_name + '\'' +
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
        dest.writeString(hospital_id);
    }
}
