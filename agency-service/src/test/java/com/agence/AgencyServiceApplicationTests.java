package com.agence;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.agence.config.WsConfig;
import com.agence.endpoint.AgenceEndpoint;
import com.agence.service.AgenceMetierService;

@SpringBootTest
class AgencyServiceApplicationTests {

    @Autowired
    private WsConfig wsConfig;

    @Autowired
    private AgenceEndpoint agenceEndpoint;

    @Autowired
    private AgenceMetierService agenceMetierService;

    @Test
    void contextLoadsRequiredSoapBeans() {
        assertNotNull(wsConfig);
        assertNotNull(agenceEndpoint);
        assertNotNull(agenceMetierService);
    }
}
