package com.sohaibazmi.remote;

public class User {
    public String name, email, phone, earnings;

    public User() { }  // required

    public User(String name, String email, String phone, String earnings) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.earnings = earnings;
    }
}
