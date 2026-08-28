package com.hotel.hotelService.service;

import com.hotel.model.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class GestionReservation {
    private List<Reservation> reservations = new ArrayList<>();
    

    public Reservation creerReservation(String prenom, String nom, int numeroCarte, LocalDate dateArrivee, LocalDate dateDepart, Chambre chambre, int nombreNuit) {
        double prixTotal = chambre.getPrixParNuit() * nombreNuit ; // exemple calcul
        Reservation r = new Reservation(prenom, nom, numeroCarte, dateArrivee, dateDepart, prixTotal, chambre);
        reservations.add(r);
        return r;
    }

    public boolean annulerReservation(Reservation r) {
        return reservations.remove(r);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}
