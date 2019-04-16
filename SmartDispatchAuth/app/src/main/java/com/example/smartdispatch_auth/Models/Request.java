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
    private String typeofemergency;
    private int scaleofemergency;
    private int vehiclereached;

    protected Request(Parcel in) {
        requester = in.readParcelable(Requester.class.getClassLoader());
        vehicle = in.readParcelable(Vehicle.class.getClassLoader());
        hospital = in.readParcelable(Hospital.class.getClassLoader());
        typeofemergency = in.readString();
        scaleofemergency = in.readInt();
        vehiclereached = in.readInt();
    }

    public Request() {
    }

    public Request(Requester requester, Vehicle vehicle, Hospital hospital, String typeofemergency, int scaleofemergency, int vehiclereached) {
        this.requester = requester;
        this.vehicle = vehicle;
        this.hospital = hospital;
        this.typeofemergency = typeofemergency;
        this.scaleofemergency = scaleofemergency;
        this.vehiclereached = vehiclereached;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requester=" + requester +
                ", vehicleLocation=" + vehicle +
                ", hospital=" + hospital +
                ", typeofemergency=" + typeofemergency +
                ", scaleofemergency=" + scaleofemergency +
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
    }

    public int getVehiclereached() {
        return vehiclereached;
    }

    public void setVehiclereached(int vehiclereached) {
        this.vehiclereached = vehiclereached;
    }
}
