package com.example.smartdispatch_auth.Models;

public class SMSRequest {

    private String emergencyType, severity, latitude, longitude, requesterID;

    public SMSRequest(String emergencyType, String severity, String latitude, String longitude, String requesterID) {
        this.emergencyType = emergencyType;
        this.severity = severity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.requesterID = requesterID;
    }

    public SMSRequest() {
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

    public String getRequesterID() {
        return requesterID;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setRequesterID(String requesterID) {
        this.requesterID = requesterID;
    }
}