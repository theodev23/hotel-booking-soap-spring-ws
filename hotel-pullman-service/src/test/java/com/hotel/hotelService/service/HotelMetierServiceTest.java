package com.hotel.hotelService.service;

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
                date("2026-06-01"),
                date("2026-06-05"),
                3,
                "AG001",
                "agence1",
                "secret"
        );

        assertEquals(1, offers.size());
        assertEquals("Pullman-201", offers.getFirst().getIdOffre());
    }

    @Test
    void shouldReturnNoOfferWhenArrivalIsBeforeAvailability() {
        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2025-12-31"),
                date("2026-01-05"),
                3,
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
                date("2026-12-30"),
                date("2027-01-01"),
                3,
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
                date("2026-06-01"),
                date("2026-06-05"),
                3,
                "UNKNOWN",
                "invalid",
                "invalid"
        );

        assertTrue(offers.isEmpty());
    }


    @Test
    void shouldReserveExistingOffer() {
        boolean reserved = service.reserver(
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(reserved);
    }

    @Test
    void shouldRejectUnknownOffer() {
        boolean reserved = service.reserver(
                "Pullman-999",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(!reserved);
    }

    @Test
    void shouldRejectDuplicateReservation() {
        boolean firstReservation = service.reserver(
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        );

        boolean secondReservation = service.reserver(
                "Pullman-201",
                "Smith",
                "John",
                "5555555555554444",
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
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111",
                "UNKNOWN",
                "invalid",
                "invalid"
        );

        assertTrue(!reserved);
    }

    @Test
    void shouldHideReservedOfferFromConsultation() {
        boolean reserved = service.reserver(
                "Pullman-201",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        );

        assertTrue(reserved);

        List<OffreType> offers = service.consulter(
                "Montpellier",
                date("2026-06-01"),
                date("2026-06-05"),
                3,
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
