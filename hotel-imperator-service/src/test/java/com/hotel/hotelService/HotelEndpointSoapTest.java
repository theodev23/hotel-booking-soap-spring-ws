package com.hotel.hotelService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import com.hotel.hotelService.service.HotelMetierService;
import com.hotel.wsdl.OffreType;

@SpringBootTest
class HotelEndpointSoapTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private HotelMetierService service;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void shouldExposeConsultationThroughSoapPayload() {
        OffreType offer = new OffreType();
        offer.setIdOffre("Imperator-101");
        offer.setPrix(new BigDecimal("120.00"));
        offer.setDateDebut(date("2030-01-01"));
        offer.setDateFin(date("2030-12-31"));
        offer.setNbLits(2);
        offer.setHotel("Hotel de l'imperator");

        when(service.consulter(
                eq("Montpellier"),
                any(XMLGregorianCalendar.class),
                any(XMLGregorianCalendar.class),
                eq(2),
                eq("AG001"),
                eq("agence1"),
                eq("secret")
        )).thenReturn(List.of(offer));

        StringSource request = new StringSource("""
                <h:ConsultationRequest xmlns:h="http://hotel.com/soap">
                    <h:idAgence>AG001</h:idAgence>
                    <h:login>agence1</h:login>
                    <h:password>secret</h:password>
                    <h:ville>Montpellier</h:ville>
                    <h:dateArrivee>2030-06-01</h:dateArrivee>
                    <h:dateDepart>2030-06-05</h:dateDepart>
                    <h:nbPersonnes>2</h:nbPersonnes>
                </h:ConsultationRequest>
                """);

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(
                        xpath(
                                "count(/*[local-name()='ConsultationResponse']"
                                + "/*[local-name()='offre'])"
                        ).evaluatesTo(1)
                )
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ConsultationResponse']"
                                + "/*[local-name()='offre']"
                                + "/*[local-name()='idOffre'])"
                        ).evaluatesTo("Imperator-101")
                )
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ConsultationResponse']"
                                + "/*[local-name()='offre']"
                                + "/*[local-name()='prix'])"
                        ).evaluatesTo("120.00")
                );

        verify(service).consulter(
                eq("Montpellier"),
                any(XMLGregorianCalendar.class),
                any(XMLGregorianCalendar.class),
                eq(2),
                eq("AG001"),
                eq("agence1"),
                eq("secret")
        );
    }

    @Test
    void shouldExposeSuccessfulReservationThroughSoapPayload() {
        when(service.reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        )).thenReturn(true);

        StringSource request = new StringSource("""
                <h:ReservationRequest xmlns:h="http://hotel.com/soap">
                    <h:idAgence>AG001</h:idAgence>
                    <h:login>agence1</h:login>
                    <h:password>secret</h:password>
                    <h:idOffre>Imperator-101</h:idOffre>
                    <h:nomClient>Doe</h:nomClient>
                    <h:prenomClient>Jane</h:prenomClient>
                    <h:carte>4111111111111111</h:carte>
                </h:ReservationRequest>
                """);

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ReservationResponse']"
                                + "/*[local-name()='status'])"
                        ).evaluatesTo("OK")
                );

        verify(service).reserver(
                "Imperator-101",
                "Doe",
                "Jane",
                "4111111111111111",
                "AG001",
                "agence1",
                "secret"
        );
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
