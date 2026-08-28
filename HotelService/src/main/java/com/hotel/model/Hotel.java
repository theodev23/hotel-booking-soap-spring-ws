package com.hotel.model;

import java.util.ArrayList;
import java.util.List;
import com.hotel.wsdl.OffreType;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;

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

    
     // Recherche les chambres correspondant aux critères
     
    public List<Chambre> rechercherChambres(CritereRecherche crit) {
        List<Chambre> resultats = new ArrayList<>();

        // Vérification ville
        if (!this.adresse.getVille().equalsIgnoreCase(crit.getVille())) {
            return resultats;
        }

        // Vérification étoiles
        if (this.nombreEtoiles < crit.getNbEtoiles()) {
            return resultats;
        }

        // Vérification critères chambre
        for (Chambre c : chambres) {
            if (!c.getDateDebutDisponible().isAfter(crit.getDateArrivee())
                    && !c.getDateFinDisponible().isBefore(crit.getDateDepart())
                    && c.getPrixParNuit() >= crit.getPrixMin()
                    && c.getPrixParNuit() <= crit.getPrixMax()
                    && c.getNombreLits() >= crit.getNbPersonnes()) {

                resultats.add(c);
            }
        }

        return resultats;
    }

    
 // Méthode adaptée pour le Web Service SOAP → Convertit les chambres en OffresType
    public List<OffreType> getOffres() {

        List<OffreType> offres = new ArrayList<>();

        for (Chambre c : chambres) {

            OffreType o = new OffreType();

            // Identifiant de l’offre → on utilise le numéro de la chambre
            o.setIdOffre(c.getNumero());

            // Prix par nuit
            o.setPrix(c.getPrixParNuit());

            // Dates (conversion LocalDate → XMLGregorianCalendar)
            o.setDateDebut(toXML(c.getDateDebutDisponible()));
            o.setDateFin(toXML(c.getDateFinDisponible()));

            // Nombre de lits
            o.setNbLits(c.getNombreLits());

            // Nom de l’hôtel
            o.setHotel(this.nom);

            offres.add(o);
        }

        return offres;
    }
    
    // Méthode pour convertir les dates
    private XMLGregorianCalendar toXML(LocalDate date) {
        try {
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(date.toString());
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
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