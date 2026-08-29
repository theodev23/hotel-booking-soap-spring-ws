package com.hotel.hotelservice;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.hotel.hotelservice.service.HotelMetierService;
import com.hotel.wsdl.ConsultationRequest;
import com.hotel.wsdl.ConsultationResponse;
import com.hotel.wsdl.OffreType;
import com.hotel.wsdl.ReservationRequest;
import com.hotel.wsdl.ReservationResponse;

@Endpoint
public class HotelEndpoint {

    private static final String NAMESPACE = "http://hotel.com/soap";

    private final HotelMetierService service;

    public HotelEndpoint(HotelMetierService service) {
        this.service = service;
    }

    // Consultation 
    @PayloadRoot(namespace = NAMESPACE, localPart = "ConsultationRequest")
    @ResponsePayload
    public ConsultationResponse consulter(@RequestPayload ConsultationRequest request) {

        ConsultationResponse response = new ConsultationResponse();
        for (OffreType off : service.consulter(
                request.getVille(),
                request.getDateArrivee(),
                request.getDateDepart(),
                request.getNbPersonnes(),
                request.getIdAgence(),
                request.getLogin(),
                request.getPassword()
        )) {
            response.getOffre().add(off);
        }

        return response;
    }

    // Réservation
    @PayloadRoot(namespace = NAMESPACE, localPart = "ReservationRequest")
    @ResponsePayload
    public ReservationResponse reserver(@RequestPayload ReservationRequest request) {

        ReservationResponse response = new ReservationResponse();

        boolean ok = service.reserver(
                request.getIdOffre(),
                request.getNomClient(),
                request.getPrenomClient(),
                request.getCarte(),
                request.getIdAgence(),
                request.getLogin(),
                request.getPassword()
        );

        response.setStatus(ok ? "OK" : "ECHEC");
        response.setMessage(ok ? "Réservation confirmée" : "Problème lors de la réservation");

        return response;
    }
}
