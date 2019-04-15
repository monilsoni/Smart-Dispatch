package com.example.smartdispatch_auth.Models;

public class CurrentRequest {
    private String hospitalname, drivername, contactno, vehicleno, hospitaladdr;

    public CurrentRequest(String hospitalname, String hospitaladdr, String drivername, String contactno, String vehicleno){

        this.hospitalname = hospitalname;
        this.hospitaladdr = hospitaladdr;
        this.drivername = drivername;
        this.contactno = contactno;
        this.vehicleno = vehicleno;
    }

    public String getDrivername() {
        return drivername;
    }

    public String getHospitalname() {
        return hospitalname;
    }

    public String getContactno() {
        return contactno;
    }

    public String getVehicleno() {
        return vehicleno;
    }

    public String getHospitalddr() {
        return hospitaladdr;
    }
}
