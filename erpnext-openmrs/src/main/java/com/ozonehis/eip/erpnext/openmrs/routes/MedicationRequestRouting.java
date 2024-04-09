/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import com.ozonehis.eip.erpnext.openmrs.processors.MedicationRequestProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MedicationRequestRouting extends RouteBuilder {

    private static final String MEDICATION_REQUEST_ID = "medication.request.id";

    private static final String FHIR_RESOURCE_TYPE = "fhir.resource.type";

    private static final String MEDICATION_REQUEST_INCLUDE_PARAMS =
            "MedicationRequest:encounter,MedicationRequest:medication,MedicationRequest:patient";

    @Autowired
    private MedicationRequestProcessor medicationRequestProcessor;

    @Override
    public void configure() {
        // spotless:off
		from("direct:fhir-medicationrequest")
			.routeId("medication-request-to-quotation-router")
			.process(exchange -> {
					MedicationRequest medicationRequest = exchange.getMessage().getBody(MedicationRequest.class);
					exchange.setProperty(FHIR_RESOURCE_TYPE, medicationRequest.fhirType());
					exchange.setProperty(MEDICATION_REQUEST_ID, medicationRequest.getIdElement().getIdPart());
					exchange.getMessage().setBody(medicationRequest);
				})
			.toD("openmrs-fhir://?id=${exchangeProperty." + MEDICATION_REQUEST_ID + "}&resource=${exchangeProperty." + FHIR_RESOURCE_TYPE + "}&include=" + MEDICATION_REQUEST_INCLUDE_PARAMS)
			.process(medicationRequestProcessor)
			.log(LoggingLevel.DEBUG,
						"MedicationRequest with ID ${exchangeProperty." + MEDICATION_REQUEST_ID + "} processed.").end();
		// spotless:on
    }
}
