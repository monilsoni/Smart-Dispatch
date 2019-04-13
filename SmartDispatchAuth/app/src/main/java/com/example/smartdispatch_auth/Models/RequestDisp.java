package com.example.smartdispatch_auth.Models;

public class RequestDisp {

    private String usrname, drivername, contactno, vehicleno, usrage, usrsex;

    public RequestDisp(String usrname, String usrage, String usrsex, String drivername, String contactno, String vehicleno) {

        this.usrname = usrname;
        this.usrage = usrage;
        this.usrsex = usrsex;
        this.drivername = drivername;
        this.contactno = contactno;
        this.vehicleno = vehicleno;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getUsrname() {
        return usrname;
    }

    public void setUsrname(String usrname) {
        this.usrname = usrname;
    }

    public String getContactno() {
        return contactno;
    }

    public void setContactno(String contactno) {
        this.contactno = contactno;
    }

    public String getVehicleno() {
        return vehicleno;
    }

    public void setVehicleno(String vehicleno) {
        this.vehicleno = vehicleno;
    }

    public String getUsrage() {
        return usrage;
    }

    public void setUsrage(String usrage) {
        this.usrage = usrage;
    }

    public String getUsrsex() {
        return usrsex;
    }

    public void setUsrsex(String usrsex) {
        this.usrsex = usrsex;
    }
}