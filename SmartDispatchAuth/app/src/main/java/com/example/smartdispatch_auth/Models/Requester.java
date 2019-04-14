package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Requester extends User implements Parcelable {

    String aadhar_number, phone_number, name, sex, age;

    public Requester() {

    }

    public Requester(String email, String user_id, String aadhar_number, String phone_number, String name, String sex, String age, GeoPoint geoPoint, Date timeStamp, String type, String token) {
        this.email = email;
        this.user_id = user_id;
        this.aadhar_number = aadhar_number;
        this.phone_number = phone_number;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.type = type;
        this.token = token;
    }

    protected Requester(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        aadhar_number = in.readString();
        phone_number = in.readString();
        name = in.readString();
        sex = in.readString();
        age = in.readString();

        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);
        type = in.readString();
        token = in.readString();
    }

    public static final Creator<Requester> CREATOR = new Creator<Requester>() {
        @Override
        public Requester createFromParcel(Parcel in) {
            return new Requester(in);
        }

        @Override
        public Requester[] newArray(int size) {
            return new Requester[size];
        }
    };

    @Override
    public String toString() {
        return "Requester{" +
                "aadhar_number='" + aadhar_number + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age='" + age + '\'' +
                ", geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                ", email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", type='" + type + '\'' +
                ", token='" + token +'\'' +
                '}';
    }

    public String getAadhar_number() {
        return aadhar_number;
    }

    public void setAadhar_number(String aadhar_number) {
        this.aadhar_number = aadhar_number;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(user_id);
        dest.writeString(aadhar_number);
        dest.writeString(phone_number);
        dest.writeString(name);
        dest.writeString(sex);
        dest.writeString(age);

        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());

        dest.writeString(type);
        dest.writeString(token);
    }
}
