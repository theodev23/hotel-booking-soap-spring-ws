package com.hotel.model;
import java.time.LocalDate;

public class CritereRecherche {
    private String ville;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private double prixMin;
    private double prixMax;
    private int nbEtoiles;
    private int nbPersonnes;

    public CritereRecherche(String ville, LocalDate dateArrivee, LocalDate dateDepart, double prixMin, double prixMax, int nbEtoiles, int nbPersonnes) {
        this.ville = ville;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.prixMin = prixMin;
        this.prixMax = prixMax;
        this.nbEtoiles = nbEtoiles;
        this.nbPersonnes = nbPersonnes;
    }

    public String getVille() { 
    	return ville; 
    }
    
    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }
    
    public double getPrixMin() { 
    	return prixMin; 
    }
    
    public double getPrixMax() { 
    	return prixMax; 
    }
    
    public int getNbEtoiles() {
    	return nbEtoiles;
    }
    
    public int getNbPersonnes() { 
    	return nbPersonnes; 
    }
}
