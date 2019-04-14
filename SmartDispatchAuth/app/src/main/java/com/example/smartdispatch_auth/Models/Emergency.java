package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Emergency implements Parcelable {

    public static final Creator<Emergency> CREATOR = new Creator<Emergency>() {
        @Override
        public Emergency createFromParcel(Parcel in) {
            return new Emergency(in);
        }

        @Override
        public Emergency[] newArray(int size) { return new Emergency[size]; }
    };

    private String type;
    private int severity;

    public Emergency(String type, int severity) {
        this.type = type;
        this.severity = severity;
    }

    public Emergency() {
    }

    protected Emergency(Parcel in) {
        type = in.readString();
        severity = in.readInt();

    }



    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "Emergency{" +
                "type='" + type + '\'' +
                ", severity='" + severity + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeInt(severity);
    }
}
