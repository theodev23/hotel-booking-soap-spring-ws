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
                date("2025-12-31"),
                date("2026-01-05"),
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
                date("2026-12-30"),
                date("2027-01-01"),
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
                date("2026-06-01"),
                date("2026-06-05"),
                2,
                "UNKNOWN",
                "invalid",
                "invalid"
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
