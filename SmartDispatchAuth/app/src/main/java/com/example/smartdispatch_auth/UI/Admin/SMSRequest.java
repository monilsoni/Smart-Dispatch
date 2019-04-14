package com.example.smartdispatch_auth.UI.Admin;

public class SMSRequest {

    private String emergencyType, severity, latitude, longitude, vehicleId, hospitalId, requesterName;

    public SMSRequest(String emergencyType, String severity, String latitude, String longitude, String vehicleId, String hospitalId, String requesterName) {
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.vehicleId = vehicleId;
        this.hospitalId = hospitalId;
        this.requesterName = requesterName;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public String getSeverity() {
        return severity;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getRequesterName() {
        return requesterName;
    }

}
