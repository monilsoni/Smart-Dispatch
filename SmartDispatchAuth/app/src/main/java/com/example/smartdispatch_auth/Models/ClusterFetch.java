package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class ClusterFetch implements Parcelable {
    public static final Creator<ClusterFetch> CREATOR = new Creator<ClusterFetch>() {
        @Override
        public ClusterFetch createFromParcel(Parcel in) {
            return new ClusterFetch(in);
        }

        @Override
        public ClusterFetch[] newArray(int size) {
            return new ClusterFetch[size];
        }
    };
    String x, y;
    int noOfVehicles;

    public ClusterFetch(int noOfVehicles, String x, String y) {
        this.x = x;
        this.y = y;
        this.noOfVehicles = noOfVehicles;
    }

    public ClusterFetch() {
    }

    protected ClusterFetch(Parcel in) {
        x = in.readString();
        y = in.readString();
        noOfVehicles = in.readInt();
    }

    public int getNoOfVehicles() {
        return noOfVehicles;
    }

    public void setNoOfVehicles(int noOfVehicles) {
        this.noOfVehicles = noOfVehicles;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(x);
        dest.writeString(y);
        dest.writeInt(noOfVehicles);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

}
