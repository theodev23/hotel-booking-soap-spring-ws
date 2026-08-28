package com.hotel.model;

public class Adresse {
    private String pays;
    private String ville;
    private String rue;
    private int numero;
    private String lieuDit;
    private int positionGps;

    public Adresse(String pays, String ville, String rue, int numero, String lieuDit, int positionGps) {
        this.pays = pays;
        this.ville = ville;
        this.rue = rue;
        this.numero = numero;
        this.lieuDit = lieuDit;
        this.positionGps = positionGps;
    }
    
    public String getVille() {
    	return ville;
    }

    @Override
    public String toString() {
        return numero + " " + rue + ", " + ville + ", " + pays + ", " + lieuDit + ", " + positionGps;
    }
}
