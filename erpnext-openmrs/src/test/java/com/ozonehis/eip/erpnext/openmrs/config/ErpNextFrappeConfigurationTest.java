/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ContextConfiguration(classes = ErpNextFrappeConfiguration.class)
@TestPropertySource("classpath:application-test.properties")
class ErpNextFrappeConfigurationTest {

    @Autowired
    Environment environment;

    @Autowired
    FrappeClient frappeClient;

    @Test
    @DisplayName("Should verify ERPNext Frappe configuration.")
    void verifyERPNextFrappeConfiguration() {
        assertEquals("http://localhost:8000", environment.getProperty("erpnext.serverUrl"));
        assertEquals("erpnext", environment.getProperty("erpnext.username"));
        assertEquals("password", environment.getProperty("erpnext.password"));
    }

    @Test
    @DisplayName("Should verify OpenMRS FHIR configuration.")
    void verifyOpenMRSFHIRConfiguration() {
        assertEquals("https://fhir.openmrs.org/openmrs/ws/fhir2/R4", environment.getProperty("eip.fhir.serverUrl"));
        assertEquals("fhirUser", environment.getProperty("eip.fhir.username"));
        assertEquals("fhirPassword", environment.getProperty("eip.fhir.password"));
    }

    @Test
    @DisplayName("Should verify Frappe client.")
    void verifyFrappeClient() {
        assertNotNull(frappeClient);
    }
}
