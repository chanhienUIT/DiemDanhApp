package com.example.diemdanhapp;

public class Class {

    public String classID;
    public String className;
    public String teacher;
    public String startTime;
    public String endTime;
    public String day;
    public String teacherEmail;

    public Class() {}

    public Class(String classID, String className, String teacher, String startTime, String endTime, String day) {
        this.classID = classID;
        this.className = className;
        this.teacher = teacher;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
    }

    public String getClassID() {
        return classID;
    }

    public String getClassName() {
        return className;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDay() {
        return day;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
