package com.agence.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.stereotype.Service;
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

    private final WebServiceTemplate webServiceTemplate;

    public AgenceMetierService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    // URLs des 2 hôtels
    private static final String HOTEL1_URL = "http://localhost:8081/ws";
    private static final String HOTEL2_URL = "http://localhost:8082/ws";

    // Tarifs
    private static final Map<String, Double> TARIFS_AGENCES = Map.of(
            "AG001", 0.9,      // -10%
            "AG002", 1.05      // +5%
    );

    private double appliquerTarif(String idAgence, double prix) {
        double coef = TARIFS_AGENCES.getOrDefault(idAgence, 1.0);
        return prix * coef;
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
                (ConsultationResponse) webServiceTemplate.marshalSendAndReceive(HOTEL1_URL, req);

            if (rep1 != null && rep1.getOffre() != null) {
                rep1.getOffre().forEach(o -> resultats.add(mapOffre(o)));
            }
        } catch (Exception e) {
            System.out.println("Impossible de joindre Hotel 1 : " + e.getMessage());
        }

        // Hôtel 2
        try {
            ConsultationResponse rep2 =
                (ConsultationResponse) webServiceTemplate.marshalSendAndReceive(HOTEL2_URL, req);

            if (rep2 != null && rep2.getOffre() != null) {
                rep2.getOffre().forEach(o -> resultats.add(mapOffre(o)));
            }
        } catch (Exception e) {
            System.out.println("Impossible de joindre Hotel 2 : " + e.getMessage());
        }

     // filtrage
     Instant arrivee = dateArrivee.toGregorianCalendar().toInstant();
     Instant depart  = dateDepart.toGregorianCalendar().toInstant();

     List<OffreType> filtres = resultats.stream()
             .filter(o -> o.getNbLits() >= nbPersonnes)
             .filter(o -> !o.getDateDebut().toGregorianCalendar().toInstant().isAfter(arrivee))
             .filter(o -> !o.getDateFin().toGregorianCalendar().toInstant().isBefore(depart))
             .collect(Collectors.toList());

     // tarification agence
     for (OffreType o : filtres) {
         double nouveauPrix = appliquerTarif(idAgence, o.getPrix());
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
                (ReservationResponse) webServiceTemplate.marshalSendAndReceive(HOTEL1_URL, req);

            if (rep1 != null && "OK".equalsIgnoreCase(rep1.getStatus())) {
                ok = true;
            }
        } catch (Exception e) {
            System.out.println("Impossible de joindre Hotel 1 (reservation) : " + e.getMessage());
        }

        // Hôtel 2
        if (!ok) {
            try {
                ReservationResponse rep2 =
                    (ReservationResponse) webServiceTemplate.marshalSendAndReceive(HOTEL2_URL, req);

                if (rep2 != null && "OK".equalsIgnoreCase(rep2.getStatus())) {
                    ok = true;
                }
            } catch (Exception e) {
                System.out.println("Impossible de joindre Hotel 2 (reservation) : " + e.getMessage());
            }
        }

        return ok;
    }
}