package com.agence.endpoint;

import com.agence.service.AgenceMetierService;
import com.agence.wsdl.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;

@Endpoint
public class AgenceEndpoint {

    private static final String NAMESPACE = "http://agence.com/soap";

    @Autowired
    private AgenceMetierService service;

    // Consultation

    @PayloadRoot(namespace = NAMESPACE, localPart = "ConsultationAgenceRequest")
    @ResponsePayload
    public ConsultationAgenceResponse consulter(@RequestPayload ConsultationAgenceRequest req) {

        List<OffreType> offres = service.consulter(
                req.getVille(),
                req.getDateArrivee(),
                req.getDateDepart(),
                req.getNbPersonnes(),
                req.getIdAgence(),
                req.getLogin(),
                req.getPassword()
        );

        ConsultationAgenceResponse rep = new ConsultationAgenceResponse();
        rep.getOffre().addAll(offres);

        return rep;
    }

    // Réservation

    @PayloadRoot(namespace = NAMESPACE, localPart = "ReservationAgenceRequest")
    @ResponsePayload
    public ReservationAgenceResponse reserver(@RequestPayload ReservationAgenceRequest req) {

        boolean ok = service.reserver(
                req.getIdAgence(),
                req.getLogin(),
                req.getPassword(),
                req.getIdOffre(),
                req.getNomClient(),
                req.getPrenomClient(),
                req.getCarte()
        );

        ReservationAgenceResponse rep = new ReservationAgenceResponse();
        rep.setStatus(ok ? "OK" : "ECHEC");
        rep.setMessage(ok ? "Réservation confirmée" : "Echec réservation");

        return rep;
    }
}
