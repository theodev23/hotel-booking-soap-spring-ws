package com.hotel.model;

public class Agence {
    private String id;
    private String login;
    private String password;

    public Agence(String id, String login, String password) {
        this.id = id;
        this.login = login;
        this.password = password;
    }

    public String getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
}
