package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Vehicle extends User implements Parcelable {

    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
        }
    };
    private String driver_name, driver_age, vehicle_number, phone_number, license_number, aadhar_number;

    public Vehicle(String driver_name, String driver_age, String vehicle_number, String phone_number, String license_number, String aadhar_number, String email, String user_id, GeoPoint geoPoint, Date timeStamp) {
        this.driver_name = driver_name;
        this.driver_age = driver_age;
        this.vehicle_number = vehicle_number;
        this.phone_number = phone_number;
        this.license_number = license_number;
        this.aadhar_number = aadhar_number;
        this.email = email;
        this.user_id = user_id;
        this.geoPoint = geoPoint;
        this.timeStamp = timeStamp;
        this.type = "vehicle";
    }

    public Vehicle() {
    }

    protected Vehicle(Parcel in) {
        driver_name = in.readString();
        driver_age = in.readString();
        vehicle_number = in.readString();
        phone_number = in.readString();
        license_number = in.readString();
        aadhar_number = in.readString();
        email = in.readString();
        user_id = in.readString();

        double latitude = in.readDouble();
        double longitude = in.readDouble();
        geoPoint = new GeoPoint(latitude, longitude);

        type = in.readString();
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_age() {
        return driver_age;
    }

    public void setDriver_age(String driver_age) {
        this.driver_age = driver_age;
    }

    public String getVehicle_number() {
        return vehicle_number;
    }

    public void setVehicle_number(String vehicle_number) {
        this.vehicle_number = vehicle_number;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getLicense_number() {
        return license_number;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }

    public String getAadhar_number() {
        return aadhar_number;
    }

    public void setAadhar_number(String aadhar_number) {
        this.aadhar_number = aadhar_number;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "driver_name='" + driver_name + '\'' +
                ", driver_age='" + driver_age + '\'' +
                ", vehicle_number='" + vehicle_number + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", license_number='" + license_number + '\'' +
                ", aadhar_number='" + aadhar_number + '\'' +
                ", email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", type='" + type + '\'' +
                ", geoPoint=" + geoPoint +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(driver_name);
        dest.writeString(driver_age);
        dest.writeString(vehicle_number);
        dest.writeString(phone_number);
        dest.writeString(license_number);
        dest.writeString(aadhar_number);
        dest.writeString(email);
        dest.writeString(user_id);

        dest.writeDouble(geoPoint.getLatitude());
        dest.writeDouble(geoPoint.getLongitude());

        dest.writeString(type);
    }
}
