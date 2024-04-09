package com.ozonehis.eip.erpnext.openmrs.config;

import org.openmrs.eip.app.config.AppConfig;
import org.openmrs.eip.fhir.spring.OpenmrsFhirAppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Import the {@link AppConfig} class to ensure that the required beans are created.
 */
@Configuration
@Import({OpenmrsFhirAppConfig.class, ErpNextFrappeConfiguration.class})
public class EipAppConfig {}
