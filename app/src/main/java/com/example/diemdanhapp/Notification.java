package com.example.diemdanhapp;

public class Notification {

    public String classID;
    public String time;
    public String buoi;

    public Notification() {}

    public Notification(String classID, String time, String buoi) {
        this.classID = classID;
        this.time = time;
        this.buoi = buoi;
    }

    public String getClassID() {
        return classID;
    }

    public String getTime() {
        return time;
    }

    public String getBuoi() {
        return buoi;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setBuoi(String buoi) {
        this.buoi = buoi;
    }
}
