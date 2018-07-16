package com.example.ramajoe.helpfitapps.Model;

public class Review {
    String name,timeStamp,rating,comment;

    public Review() {
    }

    public Review(String name, String timeStamp, String rating, String comment) {
        this.name = name;
        this.timeStamp = timeStamp;
        this.rating = rating;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
