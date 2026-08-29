package com.agence.service;

import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;

// Types exposés par le web service Agence
import com.agence.wsdl.OffreType;

// Types des web services Hôtel
import com.hotel.wsdl.ConsultationRequest;
import com.hotel.wsdl.ConsultationResponse;
import com.hotel.wsdl.ReservationRequest;
import com.hotel.wsdl.ReservationResponse;

@Service
public class AgenceMetierService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AgenceMetierService.class);

    private final WebServiceTemplate webServiceTemplate;
    private final String hotelImperatorUri;
    private final String hotelPullmanUri;

    public AgenceMetierService(
            WebServiceTemplate webServiceTemplate,
            @Value("${hotels.imperator.uri}") String hotelImperatorUri,
            @Value("${hotels.pullman.uri}") String hotelPullmanUri) {

        this.webServiceTemplate = webServiceTemplate;
        this.hotelImperatorUri = hotelImperatorUri;
        this.hotelPullmanUri = hotelPullmanUri;
    }


    // Tarifs
    private static final Map<String, BigDecimal> TARIFS_AGENCES = Map.of(
            "AG001", new BigDecimal("0.90") // -10%
    );

    private BigDecimal appliquerTarif(
            String idAgence,
            BigDecimal prix) {

        BigDecimal coefficient = TARIFS_AGENCES.getOrDefault(
                idAgence,
                BigDecimal.ONE
        );

        return prix.multiply(coefficient)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * ===============================
     *     CONSULTATION AGENCE
     * ===============================
     * Appelle les 2 hôtels
     * Agrège les offres
     * Filtre selon les critères agence
     *️Applique tarif partenaire
     */
    public List<OffreType> consulter(String ville,
                                     XMLGregorianCalendar dateArrivee,
                                     XMLGregorianCalendar dateDepart,
                                     int nbPersonnes,
                                     String idAgence,
                                     String login,
                                     String mdp) {

        List<OffreType> resultats = new ArrayList<>();

        Instant arrivee = dateArrivee.toGregorianCalendar().toInstant();
        Instant depart = dateDepart.toGregorianCalendar().toInstant();

        if (nbPersonnes <= 0 || !arrivee.isBefore(depart)) {
            LOGGER.warn(
                    "Invalid consultation criteria: arrival={}, departure={}, guests={}",
                    dateArrivee,
                    dateDepart,
                    nbPersonnes
            );
            return resultats;
        }

        // Requête envoyée aux hôtels
        ConsultationRequest req = new ConsultationRequest();
        req.setVille(ville);
        req.setDateArrivee(dateArrivee);
        req.setDateDepart(dateDepart);
        req.setNbPersonnes(nbPersonnes);
        req.setIdAgence(idAgence);
        req.setLogin(login);
        req.setPassword(mdp);

        // Hôtel 1
        try {
            ConsultationResponse rep1 =
                (ConsultationResponse) webServiceTemplate.marshalSendAndReceive(hotelImperatorUri, req);

            if (rep1 != null && rep1.getOffre() != null) {
                rep1.getOffre().forEach(o -> resultats.add(mapOffre(o)));
            }
        } catch (WebServiceClientException exception) {
            LOGGER.warn(
                    "Unable to contact Hotel Imperator during consultation: {}",
                    exception.getMessage()
            );
        }

        // Hôtel 2
        try {
            ConsultationResponse rep2 =
                (ConsultationResponse) webServiceTemplate.marshalSendAndReceive(hotelPullmanUri, req);

            if (rep2 != null && rep2.getOffre() != null) {
                rep2.getOffre().forEach(o -> resultats.add(mapOffre(o)));
            }
        } catch (WebServiceClientException exception) {
            LOGGER.warn(
                    "Unable to contact Hotel Pullman during consultation: {}",
                    exception.getMessage()
            );
        }

     // filtrage
     List<OffreType> filtres = resultats.stream()
             .filter(o -> o.getNbLits() >= nbPersonnes)
             .filter(o -> !o.getDateDebut().toGregorianCalendar().toInstant().isAfter(arrivee))
             .filter(o -> !o.getDateFin().toGregorianCalendar().toInstant().isBefore(depart))
             .collect(Collectors.toList());

     // tarification agence
     for (OffreType o : filtres) {
         BigDecimal nouveauPrix = appliquerTarif(idAgence, o.getPrix());
         o.setPrix(nouveauPrix);
     }

     return filtres;
    }

    // OffreType HOTEL  ->  OffreType AGENCE
    
    private OffreType mapOffre(com.hotel.wsdl.OffreType src) {
        OffreType cible = new OffreType();
        cible.setIdOffre(src.getIdOffre());
        cible.setPrix(src.getPrix());
        cible.setDateDebut(src.getDateDebut());
        cible.setDateFin(src.getDateFin());
        cible.setNbLits(src.getNbLits());
        cible.setHotel(src.getHotel());
        cible.setImage(src.getImage());
        return cible;
    }

    // Reservation tentative hôtel 1 puis hôtel 2
    public boolean reserver(String idAgence,
                            String login,
                            String mdp,
                            String idOffre,
                            String nomClient,
                            String prenomClient,
                            String carte) {

        ReservationRequest req = new ReservationRequest();
        req.setIdAgence(idAgence);
        req.setLogin(login);
        req.setPassword(mdp);
        req.setIdOffre(idOffre);
        req.setNomClient(nomClient);
        req.setPrenomClient(prenomClient);
        req.setCarte(carte);

        boolean ok = false;

        // Hôtel 1
        try {
            ReservationResponse rep1 =
                (ReservationResponse) webServiceTemplate.marshalSendAndReceive(hotelImperatorUri, req);

            if (rep1 != null && "OK".equalsIgnoreCase(rep1.getStatus())) {
                ok = true;
            }
        } catch (WebServiceClientException exception) {
            LOGGER.warn(
                    "Unable to contact Hotel Imperator during reservation: {}",
                    exception.getMessage()
            );
        }

        // Hôtel 2
        if (!ok) {
            try {
                ReservationResponse rep2 =
                    (ReservationResponse) webServiceTemplate.marshalSendAndReceive(hotelPullmanUri, req);

                if (rep2 != null && "OK".equalsIgnoreCase(rep2.getStatus())) {
                    ok = true;
                }
            } catch (WebServiceClientException exception) {
                LOGGER.warn(
                        "Unable to contact Hotel Pullman during reservation: {}",
                        exception.getMessage()
                );
            }
        }

        return ok;
    }
}