package com.hotel.model;

import java.time.LocalDate;

public class Offre {

 private String idOffre;
 private Chambre chambre;
 private double prix;
 private LocalDate dateDebut;
 private LocalDate dateFin;
 private String imagePath;

 public Offre(String idOffre,
              Chambre chambre,
              double prix,
              LocalDate dateDebut,
              LocalDate dateFin) {

     this.idOffre = idOffre;
     this.chambre = chambre;
     this.prix = prix;
     this.dateDebut = dateDebut;
     this.dateFin = dateFin;
 }

 public String getIdOffre() {
     return idOffre;
 }

 public Chambre getChambre() {
     return chambre;
 }

 public double getPrix() {
     return prix;
 }

 public LocalDate getDateDebut() {
     return dateDebut;
 }

 public LocalDate getDateFin() {
     return dateFin;
 }
 
 public String getImagePath() {
	    return imagePath;
	}

	public void setImagePath(String imagePath) {
	    this.imagePath = imagePath;
	}

 @Override
 public String toString() {
     return "Offre{" +
             "idOffre='" + idOffre + '\'' +
             ", chambre=" + chambre +
             ", prix=" + prix +
             ", dateDebut=" + dateDebut +
             ", dateFin=" + dateFin +
             '}';
 }
}
