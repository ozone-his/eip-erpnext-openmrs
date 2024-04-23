/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static com.ozonehis.eip.erpnext.openmrs.Constants.FHIR_RESOURCE_TYPE;

import com.ozonehis.eip.erpnext.openmrs.processors.ServiceRequestProcessor;
import lombok.Setter;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter
@Component
public class ServiceRequestRouting extends RouteBuilder implements Routing {

    private static final String SERVICE_REQUEST_TO_QUOTATION_ROUTER = "service-request-to-quotation-router";

    private static final String SERVICE_REQUEST_TO_QUOTATION_PROCESSOR = "service-request-to-quotation-processor";

    private static final String SERVICE_REQUEST_ID = "service.request.id";

    private static final String SERVICE_REQUEST_INCLUDE_PARAMS = "ServiceRequest:encounter,ServiceRequest:patient";

    private static final String SEARCH_PARAMS = "id=${exchangeProperty." + SERVICE_REQUEST_ID
            + "}&resource=${exchangeProperty." + FHIR_RESOURCE_TYPE + "}&include=" + SERVICE_REQUEST_INCLUDE_PARAMS;

    @Autowired
    private ServiceRequestProcessor serviceRequestProcessor;

    @Override
    public void configure() {
        // spotless:off
		from(incomingUri()).routeId(SERVICE_REQUEST_TO_QUOTATION_ROUTER)
				.process(exchange -> {
					ServiceRequest serviceRequest = exchange.getMessage().getBody(ServiceRequest.class);
					exchange.setProperty(FHIR_RESOURCE_TYPE, serviceRequest.fhirType());
					exchange.setProperty(SERVICE_REQUEST_ID, serviceRequest.getIdElement().getIdPart());
					exchange.getMessage().setBody(serviceRequest);
				})
				.toD("openmrs-fhir://?" + SEARCH_PARAMS)
				.to(outgoingUri()).end();
		
		from(outgoingUri()).routeId(SERVICE_REQUEST_TO_QUOTATION_PROCESSOR)
				.process(serviceRequestProcessor)
				.log(LoggingLevel.DEBUG, "ServiceRequest with ID ${exchangeProperty." + SERVICE_REQUEST_ID + "} processed.")
				.end();
		// spotless:on
    }

    @Override
    public String incomingUri() {
        return "direct:fhir-servicerequest";
    }

    @Override
    public String outgoingUri() {
        return "direct:" + SERVICE_REQUEST_TO_QUOTATION_PROCESSOR;
    }
}
