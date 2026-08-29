package com.hotel.model;

import java.util.ArrayList;
import java.util.List;

public class Hotel {

    private String nom;
    private Adresse adresse;
    private int nombreEtoiles;
    private List<Chambre> chambres;
    private List<Agence> agences = new ArrayList<>();

    public Hotel(String nom, Adresse adresse, int nombreEtoiles) {
        this.nom = nom;
        this.adresse = adresse;
        this.nombreEtoiles = nombreEtoiles;
        this.chambres = new ArrayList<>();
    }
    
    public String getNom() {
        return nom;
    }

    public void ajouterChambre(Chambre c) {
        this.chambres.add(c);
    }
    
    public List<Chambre> getChambres() {
        return chambres;
    }
    
    public Adresse getAdresse() { 
    	return adresse; 
    }


    // Méthode pour ajouter une agence
    public void ajouterAgence(Agence a){
        agences.add(a);
    }

    // Méthode pour vérifier l'agence
    public Agence verifierAgence(String id, String login, String password){
        return agences.stream()
                .filter(a -> a.getId().equals(id)
                        && a.getLogin().equals(login)
                        && a.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return nom + " (" + nombreEtoiles + "*) - " + adresse;
    }
}