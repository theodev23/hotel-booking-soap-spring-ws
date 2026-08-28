package com.hotel.hotelService.service;

import com.hotel.model.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.hotel.wsdl.OffreType;
import org.springframework.stereotype.Service;

@Service
public class HotelMetierService {

    private Hotel hotel;
    private List<Reservation> reservations = new ArrayList<>();

    public HotelMetierService() {

        Adresse adresse1 = new Adresse(
                "France",
                "Montpellier",
                "Rue de Rivoli",
                10,
                "Établissement incroyable",
                634
        );

        hotel = new Hotel("Hotel de l'imperator", adresse1, 4);

        hotel.ajouterChambre(new Chambre(
                "101",
                2,
                120,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                "chambre1.jpg"
        ));

        Agence a1 = new Agence("AG001", "agence1", "secret", 0.9);
        hotel.ajouterAgence(a1);
    }


    // Consultation
    public List<OffreType> consulter(String ville,
                                     XMLGregorianCalendar dateArrivee,
                                     XMLGregorianCalendar dateDepart,
                                     int nbPersonnes,
                                     String idAgence,
                                     String login,
                                     String mdp) {

        if (hotel.verifierAgence(idAgence, login, mdp) == null) {
            System.out.println("Agence non autorisée – accès refusé");
            return new ArrayList<>();
        }

        // Conversion dates (si plus tard tu veux filtrer finement)
        LocalDate debut = dateArrivee.toGregorianCalendar().toZonedDateTime().toLocalDate();
        LocalDate fin = dateDepart.toGregorianCalendar().toZonedDateTime().toLocalDate();

        List<OffreType> offres = new ArrayList<>();

        for (Chambre c : hotel.getChambres()) {

            // Filtre minimum : ville + capacité
            if (!hotel.getAdresse().getVille().equalsIgnoreCase(ville)) continue;
            if (c.getNombreLits() < nbPersonnes) continue;

            OffreType o = new OffreType();
            o.setIdOffre("Imperator-" + c.getNumero());
            o.setPrix(c.getPrixParNuit());
            o.setDateDebut(toXML(c.getDateDebutDisponible()));
            o.setDateFin(toXML(c.getDateFinDisponible()));
            o.setNbLits(c.getNombreLits());
            o.setHotel(hotel.getNom());

            // image
            byte[] imgBytes = loadImage(c.getImagePath());
            o.setImage(imgBytes);

            offres.add(o);
        }

        return offres;
    }

    // Conversion date
    private XMLGregorianCalendar toXML(LocalDate date) {
        try {
            return javax.xml.datatype.DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendar(date.toString());
        } catch (Exception e) {
            return null;
        }
    }


    // Reservation
    public boolean reserver(String idoffre,
                            String nom,
                            String prenom,
                            String carte,
                            String idAgence,
                            String login,
                            String mdp) {

        if (hotel.verifierAgence(idAgence, login, mdp) == null) {
            System.out.println("Agence non autorisée — réservation refusée");
            return false;
        }

        return true;
    }


    // Image
    private byte[] loadImage(String imageName) {
        try {
            InputStream in = getClass()
                    .getClassLoader()
                    .getResourceAsStream("static/" + imageName);

            if (in == null) return null;

            return in.readAllBytes();

        } catch (Exception e) {
            System.out.println("Erreur chargement image : " + e.getMessage());
            return null;
        }
    }
}