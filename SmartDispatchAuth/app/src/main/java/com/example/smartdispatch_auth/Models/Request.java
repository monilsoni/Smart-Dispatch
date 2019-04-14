package com.example.smartdispatch_auth.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class Request implements Parcelable {

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel in) {
            return new Request(in);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };
    private Requester requester;
    private Vehicle vehicle;
    private Hospital hospital;
    private Emergency emergency;

    protected Request(Parcel in) {
        requester = in.readParcelable(Requester.class.getClassLoader());
        vehicle = in.readParcelable(Vehicle.class.getClassLoader());
        hospital = in.readParcelable(Hospital.class.getClassLoader());
        emergency = in.readParcelable(Emergency.class.getClassLoader());
    }

    public Request() {
    }

    public Request(Requester requester, Vehicle vehicle, Hospital hospital, Emergency emergency) {
        this.requester = requester;
        this.vehicle = vehicle;
        this.hospital = hospital;
        this.emergency = emergency;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requester=" + requester +
                ", vehicleLocation=" + vehicle +
                ", hospital=" + hospital +
                ", emergency=" + emergency +
                '}';
    }

    public Requester getRequester() {
        return requester;
    }

    public void setRequester(Requester requester) {
        this.requester = requester;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Emergency getEmergency() { return emergency; }

    public void setEmergency(Emergency emergency) { this.emergency = emergency; }

    public Hospital getHospital() {
        return hospital;
    }

    public void setHospital(Hospital hospital) {
        this.hospital = hospital;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(requester, flags);
        dest.writeParcelable(vehicle, flags);
        dest.writeParcelable(hospital, flags);
        dest.writeParcelable(emergency, flags);
    }
}
