package com.hotel.hotelservice.service;

import java.math.BigDecimal;

import com.hotel.model.Adresse;
import com.hotel.model.Agence;
import com.hotel.model.Chambre;
import com.hotel.model.Hotel;
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

        Adresse adresse2 = new Adresse(
                "France",
                "Montpellier",
                "Avenue historique",
                5,
                "Hôtel confortable en centre-ville",
                200
        );

        hotel = new Hotel("Hotel Pullman", adresse2, 3);

        hotel.ajouterChambre(new Chambre(
                "201",
                3,
                new BigDecimal("90.00"),
                LocalDate.of(2030, 1, 1),
                LocalDate.of(2030, 12, 31),
                "chambre2.jpg"
        ));

        Agence a1 = new Agence("AG001", "agence1", "secret");
        hotel.ajouterAgence(a1);
    }


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

        LocalDate debut = dateArrivee.toGregorianCalendar()
                .toZonedDateTime()
                .toLocalDate();

        LocalDate fin = dateDepart.toGregorianCalendar()
                .toZonedDateTime()
                .toLocalDate();

        if (nbPersonnes <= 0 || !debut.isBefore(fin)) {
            LOGGER.warn(
                    "Invalid consultation criteria: arrival={}, departure={}, guests={}",
                    debut,
                    fin,
                    nbPersonnes
            );
            return new ArrayList<>();
        }

        List<OffreType> offres = new ArrayList<>();

        for (Chambre c : hotel.getChambres()) {

            if (!hotel.getAdresse().getVille().equalsIgnoreCase(ville)) continue;
            if (c.getNombreLits() < nbPersonnes) continue;
            if (debut.isBefore(c.getDateDebutDisponible())) continue;
            if (fin.isAfter(c.getDateFinDisponible())) continue;

            String offerId = "Pullman-" + c.getNumero();
            if (reservedOfferIds.contains(offerId)) continue;

            OffreType o = new OffreType();
            o.setIdOffre(offerId);
            o.setPrix(c.getPrixParNuit());
            o.setDateDebut(toXML(c.getDateDebutDisponible()));
            o.setDateFin(toXML(c.getDateFinDisponible()));
            o.setNbLits(c.getNombreLits());
            o.setHotel(hotel.getNom());

            byte[] imgBytes = loadImage(c.getImagePath());
            o.setImage(imgBytes);

            offres.add(o);
        }

        return offres;
    }


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
                .map(chambre -> "Pullman-" + chambre.getNumero())
                .anyMatch(idoffre::equals);

        if (!offerExists) {
            return false;
        }

        return reservedOfferIds.add(idoffre);
    }

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