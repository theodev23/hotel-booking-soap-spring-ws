package com.agence.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.agence.wsdl.OffreType;
import com.hotel.wsdl.ConsultationRequest;
import com.hotel.wsdl.ConsultationResponse;
import com.hotel.wsdl.ReservationRequest;
import com.hotel.wsdl.ReservationResponse;

class AgenceMetierServiceTest {

    private static final String IMPERATOR_URI = "http://imperator.test/ws";
    private static final String PULLMAN_URI = "http://pullman.test/ws";

    private WebServiceTemplate webServiceTemplate;
    private AgenceMetierService service;

    @BeforeEach
    void setUp() {
        webServiceTemplate = mock(WebServiceTemplate.class);

        service = new AgenceMetierService(
                webServiceTemplate,
                IMPERATOR_URI,
                PULLMAN_URI
        );
    }

    @Test
    void shouldAggregateHotelOffersAndApplyPartnerRate() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ConsultationRequest.class)
        )).thenReturn(
                consultationResponse(
                        "Imperator-101",
                        "120.00",
                        2,
                        "Hotel de l'imperator"
                )
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ConsultationRequest.class)
        )).thenReturn(
                consultationResponse(
                        "Pullman-201",
                        "90.00",
                        3,
                        "Hotel Pullman"
                )
        );

        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2026-06-01"),
                date("2026-06-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertEquals(2, offers.size());

        OffreType imperator = offers.stream()
                .filter(offer -> "Imperator-101".equals(offer.getIdOffre()))
                .findFirst()
                .orElseThrow();

        OffreType pullman = offers.stream()
                .filter(offer -> "Pullman-201".equals(offer.getIdOffre()))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("108.00"), imperator.getPrix());
        assertEquals(new BigDecimal("81.00"), pullman.getPrix());
    }

    @Test
    void shouldContinueConsultationWhenImperatorIsUnavailable() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ConsultationRequest.class)
        )).thenThrow(
                new WebServiceIOException("Imperator unavailable")
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ConsultationRequest.class)
        )).thenReturn(
                consultationResponse(
                        "Pullman-201",
                        "90.00",
                        3,
                        "Hotel Pullman"
                )
        );

        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2026-06-01"),
                date("2026-06-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertEquals(1, offers.size());
        assertEquals("Pullman-201", offers.getFirst().getIdOffre());
        assertEquals(new BigDecimal("81.00"), offers.getFirst().getPrix());
    }

    @Test
    void shouldReturnNoOffersWhenBothHotelsAreUnavailable() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ConsultationRequest.class)
        )).thenThrow(
                new WebServiceIOException("Imperator unavailable")
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ConsultationRequest.class)
        )).thenThrow(
                new WebServiceIOException("Pullman unavailable")
        );

        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2026-06-01"),
                date("2026-06-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReserveWithImperatorWithoutCallingPullman() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("OK")
        );

        boolean reserved = service.reserver(
                "AG001",
                "agence1",
                "secret",
                "Imperator-101",
                "Doe",
                "Jane",
                "4111111111111111"
        );

        assertTrue(reserved);

        verify(
                webServiceTemplate,
                never()
        ).marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ReservationRequest.class)
        );
    }

    @Test
    void shouldFallbackToPullmanWhenImperatorRejectsReservation() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("ECHEC")
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("OK")
        );

        boolean reserved = service.reserver(
                "AG001",
                "agence1",
                "secret",
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111"
        );

        assertTrue(reserved);
    }

    @Test
    void shouldFallbackToPullmanWhenImperatorIsUnavailable() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ReservationRequest.class)
        )).thenThrow(
                new WebServiceIOException("Imperator unavailable")
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("OK")
        );

        boolean reserved = service.reserver(
                "AG001",
                "agence1",
                "secret",
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111"
        );

        assertTrue(reserved);
    }

    @Test
    void shouldReturnFalseWhenBothHotelsRejectReservation() {
        when(webServiceTemplate.marshalSendAndReceive(
                eq(IMPERATOR_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("ECHEC")
        );

        when(webServiceTemplate.marshalSendAndReceive(
                eq(PULLMAN_URI),
                any(ReservationRequest.class)
        )).thenReturn(
                reservationResponse("ECHEC")
        );

        boolean reserved = service.reserver(
                "AG001",
                "agence1",
                "secret",
                "UNKNOWN-999",
                "Doe",
                "Jane",
                "4111111111111111"
        );

        assertFalse(reserved);
    }

    private ConsultationResponse consultationResponse(
            String id,
            String price,
            int beds,
            String hotelName) {

        com.hotel.wsdl.OffreType offer =
                new com.hotel.wsdl.OffreType();

        offer.setIdOffre(id);
        offer.setPrix(new BigDecimal(price));
        offer.setDateDebut(date("2026-01-01"));
        offer.setDateFin(date("2026-12-31"));
        offer.setNbLits(beds);
        offer.setHotel(hotelName);

        ConsultationResponse response =
                new ConsultationResponse();

        response.getOffre().add(offer);

        return response;
    }

    private ReservationResponse reservationResponse(String status) {
        ReservationResponse response =
                new ReservationResponse();

        response.setStatus(status);
        response.setMessage(status);

        return response;
    }

    private XMLGregorianCalendar date(String value) {
        try {
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(value);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
