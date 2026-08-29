package com.agence.endpoint;

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

import com.agence.service.AgenceMetierService;
import com.agence.wsdl.OffreType;

@SpringBootTest
class AgenceEndpointSoapTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private AgenceMetierService service;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void shouldExposeConsultationThroughSoapPayload() {
        OffreType offer = new OffreType();
        offer.setIdOffre("Imperator-101");
        offer.setPrix(new BigDecimal("108.00"));
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
                <a:ConsultationAgenceRequest xmlns:a="http://agence.com/soap">
                    <a:idAgence>AG001</a:idAgence>
                    <a:login>agence1</a:login>
                    <a:password>secret</a:password>
                    <a:ville>Montpellier</a:ville>
                    <a:dateArrivee>2030-06-01</a:dateArrivee>
                    <a:dateDepart>2030-06-05</a:dateDepart>
                    <a:nbPersonnes>2</a:nbPersonnes>
                </a:ConsultationAgenceRequest>
                """);

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(
                        xpath(
                                "count(/*[local-name()='ConsultationAgenceResponse']"
                                + "/*[local-name()='offre'])"
                        ).evaluatesTo(1)
                )
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ConsultationAgenceResponse']"
                                + "/*[local-name()='offre']"
                                + "/*[local-name()='idOffre'])"
                        ).evaluatesTo("Imperator-101")
                )
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ConsultationAgenceResponse']"
                                + "/*[local-name()='offre']"
                                + "/*[local-name()='prix'])"
                        ).evaluatesTo("108.00")
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
                "AG001",
                "agence1",
                "secret",
                "Imperator-101",
                "Doe",
                "Jane",
                "4111111111111111"
        )).thenReturn(true);

        StringSource request = new StringSource("""
                <a:ReservationAgenceRequest xmlns:a="http://agence.com/soap">
                    <a:idAgence>AG001</a:idAgence>
                    <a:login>agence1</a:login>
                    <a:password>secret</a:password>
                    <a:idOffre>Imperator-101</a:idOffre>
                    <a:nomClient>Doe</a:nomClient>
                    <a:prenomClient>Jane</a:prenomClient>
                    <a:carte>4111111111111111</a:carte>
                </a:ReservationAgenceRequest>
                """);

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ReservationAgenceResponse']"
                                + "/*[local-name()='status'])"
                        ).evaluatesTo("OK")
                )
                .andExpect(
                        xpath(
                                "string(/*[local-name()='ReservationAgenceResponse']"
                                + "/*[local-name()='message'])"
                        ).evaluatesTo("Réservation confirmée")
                );

        verify(service).reserver(
                "AG001",
                "agence1",
                "secret",
                "Imperator-101",
                "Doe",
                "Jane",
                "4111111111111111"
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
