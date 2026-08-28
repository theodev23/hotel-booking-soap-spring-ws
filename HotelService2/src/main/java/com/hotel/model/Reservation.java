package com.hotel.model;

import java.time.LocalDate;

public class Reservation {
	private String prenom;
	private String nom;
	private int numeroCarte;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private double prixTotal;
    private Chambre chambre;

    public Reservation(String prenom, String nom, int numeroCarte, LocalDate dateArrivee, LocalDate dateDepart, double prixTotal, Chambre chambre) {
        this.prenom = prenom;
        this.nom = nom;
        this.numeroCarte = numeroCarte;
    	this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.prixTotal = prixTotal;
        this.chambre = chambre;
    }

    public Chambre getChambre() {
        return chambre;
    }

    public boolean confirmer() {
        return true;
    }

    @Override
    public String toString() {
        return "Réservation de la " + chambre.toString()
             + " du " + dateArrivee + " au " + dateDepart
             + " pour le prix total de : " + prixTotal + " €";
    }
}
