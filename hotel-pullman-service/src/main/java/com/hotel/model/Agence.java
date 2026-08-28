package com.hotel.model;

public class Agence {
    private String id;
    private String login;
    private String password;
    private double coefficient;

    public Agence(String id, String login, String password, double coefficient) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.coefficient = coefficient;
    }

    public String getId() { return id; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
    public double getCoefficient() { return coefficient; }
}
