package com.hotel.hotelService.service;

import com.hotel.model.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.hotel.wsdl.OffreType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HotelMetierService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HotelMetierService.class);

    private Hotel hotel;
    private final Set<String> reservedOfferIds = ConcurrentHashMap.newKeySet();

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

        Agence a1 = new Agence("AG001", "agence1", "secret");
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
            LOGGER.warn("Unauthorized agency consultation attempt: {}", idAgence);
            return new ArrayList<>();
        }

        // Conversion dates (si plus tard tu veux filtrer finement)
        LocalDate debut = dateArrivee.toGregorianCalendar().toZonedDateTime().toLocalDate();
        LocalDate fin = dateDepart.toGregorianCalendar().toZonedDateTime().toLocalDate();

        List<OffreType> offres = new ArrayList<>();

        for (Chambre c : hotel.getChambres()) {

            // Filtres ville, capacité et période de disponibilité
            if (!hotel.getAdresse().getVille().equalsIgnoreCase(ville)) continue;
            if (c.getNombreLits() < nbPersonnes) continue;
            if (debut.isBefore(c.getDateDebutDisponible())) continue;
            if (fin.isAfter(c.getDateFinDisponible())) continue;

            String offerId = "Imperator-" + c.getNumero();
            if (reservedOfferIds.contains(offerId)) continue;

            OffreType o = new OffreType();
            o.setIdOffre(offerId);
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
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(date.toString());
        } catch (DatatypeConfigurationException exception) {
            throw new IllegalStateException(
                    "Unable to create XML date",
                    exception
            );
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
            LOGGER.warn("Unauthorized agency reservation attempt: {}", idAgence);
            return false;
        }

        boolean offerExists = hotel.getChambres().stream()
                .map(chambre -> "Imperator-" + chambre.getNumero())
                .anyMatch(idoffre::equals);

        if (!offerExists) {
            return false;
        }

        return reservedOfferIds.add(idoffre);
    }


    // Image
    private byte[] loadImage(String imageName) {
        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream("static/" + imageName)) {

            if (inputStream == null) {
                LOGGER.warn("Hotel room image not found: {}", imageName);
                return null;
            }

            return inputStream.readAllBytes();

        } catch (IOException exception) {
            LOGGER.warn(
                    "Unable to load hotel room image {}: {}",
                    imageName,
                    exception.getMessage()
            );
            return null;
        }
    }
}