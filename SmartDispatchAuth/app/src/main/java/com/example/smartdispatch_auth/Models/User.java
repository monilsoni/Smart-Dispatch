package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    private String email, user_id, aadhar_number, phone_number;

    public User(String email, String user_id, String aadhar_number, String phone_number) {
        this.email = email;
        this.user_id = user_id;
        this.aadhar_number = aadhar_number;
        this.phone_number = phone_number;
    }

    public User() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    @Override
    public String toString() {
        return "User{" +
                ", email='" + email + '\'' +
                ", user_id='" + user_id + '\'' +
                ", aadhar_number='" + aadhar_number + '\'' +
                ", phone_number='" + phone_number + '\'' +
                '}';
    }

    protected User(Parcel in) {
        email = in.readString();
        user_id = in.readString();
        aadhar_number = in.readString();
        phone_number = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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
    }
}
