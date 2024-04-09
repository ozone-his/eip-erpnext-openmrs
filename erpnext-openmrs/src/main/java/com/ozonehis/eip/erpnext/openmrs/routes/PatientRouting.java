/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_ENABLE_PATIENT_SYNC;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.ozonehis.eip.erpnext.openmrs.processors.PatientProcessor;
import lombok.Setter;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Setter
@Component
public class PatientRouting extends RouteBuilder {

    @Value("${erpnext.openmrs.enable.patient.sync}")
    private boolean isPatientSyncEnabled;

    @Autowired
    private PatientProcessor patientProcessor;

    Predicate isPatientSyncEnabled() {
        return exchange -> isPatientSyncEnabled
                || Boolean.TRUE.equals(exchange.getIn().getHeader(HEADER_ENABLE_PATIENT_SYNC, Boolean.class));
    }

    @Override
    public void configure() {
        // spotless:off
		from("direct:patient-to-customer-router")
			.routeId("patient-to-customer-router")
			.filter(isPatientSyncEnabled())
			.log(LoggingLevel.INFO, "Patient sync is enabled")
			.process(patientProcessor)
			.choice()
				.when(header(HEADER_FHIR_EVENT_TYPE).isEqualTo("c"))
					.toD("direct:erpnext-create-customer-route")
				.when(header(HEADER_FHIR_EVENT_TYPE).isEqualTo("u"))
					.toD("direct:erpnext-update-customer-route")
				.when(header(HEADER_FHIR_EVENT_TYPE).isEqualTo("d"))
					.toD("direct:erpnext-delete-customer-route")
				.otherwise()
					.log(LoggingLevel.WARN, "Unsupported event type: ${header." + HEADER_FHIR_EVENT_TYPE + "}")
				.end()
			.end().end();
		
		from("direct:fhir-patient").routeId("fhir-patient-to-customer-router")
			.to("direct:patient-to-customer-router").end();
		// spotless:on
    }
}
