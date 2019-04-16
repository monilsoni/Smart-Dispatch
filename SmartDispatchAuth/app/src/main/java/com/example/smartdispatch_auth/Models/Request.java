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
    private String typeofemergency, request_id;
    private int scaleofemergency;
    private int vehiclereached;

    protected Request(Parcel in) {
        requester = in.readParcelable(Requester.class.getClassLoader());
        vehicle = in.readParcelable(Vehicle.class.getClassLoader());
        hospital = in.readParcelable(Hospital.class.getClassLoader());
        typeofemergency = in.readString();
        scaleofemergency = in.readInt();
        vehiclereached = in.readInt();
        request_id = in.readString();
    }

    public Request() {
    }

    public Request(Requester requester, Vehicle vehicle, Hospital hospital, String typeofemergency, int scaleofemergency, int vehiclereached, String request_id) {
        this.requester = requester;
        this.vehicle = vehicle;
        this.hospital = hospital;
        this.typeofemergency = typeofemergency;
        this.scaleofemergency = scaleofemergency;
        this.vehiclereached = vehiclereached;
        this.request_id = request_id;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requester=" + requester +
                ",\n vehicle=" + vehicle +
                ",\n hospital=" + hospital +
                ",\n typeofemergency='" + typeofemergency + '\'' +
                ",\n request_id='" + request_id + '\'' +
                ",\n scaleofemergency=" + scaleofemergency +
                ",\n vehiclereached=" + vehiclereached +
                '}';
    }

    public String getTypeofemergency() {
        return typeofemergency;
    }

    public void setTypeofemergency(String typeofemergency) {
        this.typeofemergency = typeofemergency;
    }

    public int getScaleofemergency() {
        return scaleofemergency;
    }

    public void setScaleofemergency(int scaleofemergency) {
        this.scaleofemergency = scaleofemergency;
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
        dest.writeString(typeofemergency);
        dest.writeInt(scaleofemergency);
        dest.writeInt(vehiclereached);
        dest.writeString(request_id);
    }

    public int getVehiclereached() {
        return vehiclereached;
    }

    public void setVehiclereached(int vehiclereached) {
        this.vehiclereached = vehiclereached;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }
}
