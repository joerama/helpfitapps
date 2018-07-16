package com.example.ramajoe.helpfitapps.Model;

public class User {
    private String fullname, username, email, password, type;

    public User() {
    }

    public User(String fullname, String username, String email, String password, String type) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = type;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
