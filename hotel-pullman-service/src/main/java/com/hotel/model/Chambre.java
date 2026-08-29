package com.hotel.model;

import java.math.BigDecimal;

import java.time.LocalDate;

public class Chambre {
    private String numero;
    private int nombreLits;
    private BigDecimal prixParNuit;
    private LocalDate dateDebutDisponible;
    private LocalDate dateFinDisponible;
    private String imagePath;


    public Chambre(String numero, int nombreLits, BigDecimal prixParNuit, LocalDate dateDebutDisponible, LocalDate dateFinDisponible, String imagePath) {
        this.numero = numero;
        this.nombreLits = nombreLits;
        this.prixParNuit = prixParNuit;
        this.dateDebutDisponible = dateDebutDisponible;
        this.dateFinDisponible = dateFinDisponible;
        this.imagePath = imagePath;
    }
    
    public String getNumero() {
        return numero;
    }
    
    public int getNombreLits() {
        return nombreLits;
    }
    
    public BigDecimal getPrixParNuit() {
        return prixParNuit;
    }

    public LocalDate getDateDebutDisponible () {
    	return dateDebutDisponible;
    }
    
    public LocalDate getDateFinDisponible () {
    	return dateFinDisponible;
    }
    
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    @Override
    public String toString() {
        return "Chambre " + numero + " (Il y a : " + nombreLits + " lits. Le prix est :" + prixParNuit + "€/nuit)";
    }
    
    
}
