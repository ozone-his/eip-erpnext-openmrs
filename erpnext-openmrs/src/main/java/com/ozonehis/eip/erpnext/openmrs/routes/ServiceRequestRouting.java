package com.ozonehis.eip.erpnext.openmrs.routes;

import static com.ozonehis.eip.erpnext.openmrs.Constants.FHIR_RESOURCE_TYPE;

import com.ozonehis.eip.erpnext.openmrs.processors.ServiceRequestProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceRequestRouting extends RouteBuilder {

    private static final String SERVICE_REQUEST_ID = "service.request.id";

    private static final String SERVICE_REQUEST_INCLUDE_PARAMS = "ServiceRequest:encounter,ServiceRequest:patient";

    @Autowired
    private ServiceRequestProcessor serviceRequestProcessor;

    @Override
    public void configure() {
        // spotless:off
		from("direct:fhir-servicerequest")
			.routeId("service-request-to-quotation-router")
			.process(exchange -> {
					ServiceRequest serviceRequest = exchange.getMessage().getBody(ServiceRequest.class);
					exchange.setProperty(FHIR_RESOURCE_TYPE, serviceRequest.fhirType());
					exchange.setProperty(SERVICE_REQUEST_ID, serviceRequest.getIdElement().getIdPart());
					exchange.getMessage().setBody(serviceRequest);
				})
			.toD("openmrs-fhir://?id=${exchangeProperty." + SERVICE_REQUEST_ID + "}&resource=${exchangeProperty." + FHIR_RESOURCE_TYPE + "}&include=" + SERVICE_REQUEST_INCLUDE_PARAMS)
			.process(serviceRequestProcessor)
			.log(LoggingLevel.DEBUG, "ServiceRequest with ID ${exchangeProperty." + SERVICE_REQUEST_ID + "} processed.")
				.end();
		// spotless:on
    }
}
