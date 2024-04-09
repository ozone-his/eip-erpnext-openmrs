package com.ozonehis.eip.erpnext.openmrs.routes.quotation;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CreateQuotationRoute extends RouteBuilder {

    @Override
    public void configure() {
        // spotless:off
		from("direct:erpnext-create-quotation-route")
			.routeId("erpnext-create-quotation-route")
			.to("frappe://post/resource?inBody=resource")
			.log(LoggingLevel.INFO, "Quotation with the name ${header." + HEADER_FRAPPE_NAME + "} created successfully.")
				.end();
		// spotless:on
    }
}
