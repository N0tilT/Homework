package com.example.homework;

public class User {

    private String userLogin;
    private int userId;

    public User(int id, String login){
        this.userId = id;
        this.userLogin = login;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }
}
