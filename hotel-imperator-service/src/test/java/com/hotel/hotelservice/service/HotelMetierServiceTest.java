package com.hotel.hotelservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.Test;

import com.hotel.wsdl.OffreType;

class HotelMetierServiceTest {

    private final HotelMetierService service = new HotelMetierService();

    @Test
    void shouldReturnOfferWhenRequestedDatesAreWithinAvailability() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-01"),
                date("2030-06-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertEquals(1, offers.size());
        assertEquals("Imperator-101", offers.getFirst().getIdOffre());
    }

    @Test
    void shouldReturnNoOfferWhenArrivalIsBeforeAvailability() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2029-12-31"),
                date("2030-01-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReturnNoOfferWhenDepartureIsAfterAvailability() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-12-30"),
                date("2031-01-01"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReturnNoOfferForUnauthorizedAgency() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-01"),
                date("2030-06-05"),
                2,
                "UNKNOWN",
                "invalid",
                "invalid"
        );

        assertTrue(offers.isEmpty());
    }


    @Test
    void shouldReserveExistingOffer() {
        boolean reserved = service.reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "demo-card",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(reserved);
    }

    @Test
    void shouldRejectUnknownOffer() {
        boolean reserved = service.reserver(
                "Imperator-999",
                "Doe",
                "Jane",
                "demo-card",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(!reserved);
    }

    @Test
    void shouldRejectDuplicateReservation() {
        boolean firstReservation = service.reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "demo-card",
                "AG001",
                "agence1",
                "secret"
        );

        boolean secondReservation = service.reserver(
                "Imperator-101",
                "Smith",
                "John",
                "demo-card-2",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(firstReservation);
        assertTrue(!secondReservation);
    }

    @Test
    void shouldRejectReservationForUnauthorizedAgency() {
        boolean reserved = service.reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "demo-card",
                "UNKNOWN",
                "invalid",
                "invalid"
        );

        assertTrue(!reserved);
    }

    @Test
    void shouldHideReservedOfferFromConsultation() {
        boolean reserved = service.reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "demo-card",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(reserved);

        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-01"),
                date("2030-06-05"),
                2,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }


    @Test
    void shouldReturnNoOfferWhenArrivalEqualsDeparture() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-05"),
                date("2030-06-05"),
                1,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReturnNoOfferWhenArrivalIsAfterDeparture() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-10"),
                date("2030-06-05"),
                1,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReturnNoOfferWhenPersonCountIsZero() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-01"),
                date("2030-06-05"),
                0,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
    }

    @Test
    void shouldReturnNoOfferWhenPersonCountIsNegative() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2030-06-01"),
                date("2030-06-05"),
                -1,
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(offers.isEmpty());
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
