package com.agence.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.springframework.core.io.ClassPathResource;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@EnableWs
@Configuration
public class WsConfig {

    //  Partie exposition WSDL
    

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean
    public XsdSchema agenceSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/agence.xsd"));
    }

    @Bean(name = "agence")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema agenceSchema) {
        DefaultWsdl11Definition wsdl = new DefaultWsdl11Definition();
        wsdl.setPortTypeName("AgencePort");
        wsdl.setLocationUri("/ws");
        wsdl.setTargetNamespace("http://agence.com/soap");
        wsdl.setSchema(agenceSchema);
        return wsdl;
    }

    // Partie client hôtel

    /** Marshaller pour les classes générées des HÔTELS */
    @Bean
    public Jaxb2Marshaller hotelMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        // package des classes générées à partir de hotel.xsd
        marshaller.setContextPath("com.hotel.wsdl");
        return marshaller;
    }

    /** WebServiceTemplate utilisé par l'agence pour appeler les hôtels */
    @Bean
    public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller hotelMarshaller) {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(hotelMarshaller);
        template.setUnmarshaller(hotelMarshaller);
        return template;
    }
}