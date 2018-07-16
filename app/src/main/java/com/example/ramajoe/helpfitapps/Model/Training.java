package com.example.ramajoe.helpfitapps.Model;

public class Training {
    String sessionID, title, date, time, fee, status, mode, classType, notes, maxParticipant;

    public Training() {
    }

    public Training(String sessionID, String title, String date, String time, String fee, String status, String mode, String classType, String notes, String maxParticipant) {
        this.sessionID = sessionID;
        this.title = title;
        this.date = date;
        this.time = time;
        this.fee = fee;
        this.status = status;
        this.mode = mode;
        this.classType = classType;
        this.notes = notes;
        this.maxParticipant = maxParticipant;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMaxParticipant() {
        return maxParticipant;
    }

    public void setMaxParticipant(String maxParticipant) {
        this.maxParticipant = maxParticipant;
    }
}
