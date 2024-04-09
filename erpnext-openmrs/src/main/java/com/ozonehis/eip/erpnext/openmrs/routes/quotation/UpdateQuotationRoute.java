package com.ozonehis.eip.erpnext.openmrs.routes.quotation;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateQuotationRoute extends RouteBuilder {

    @Override
    public void configure() {
        // spotless:off
		from("direct:erpnext-update-quotation-route")
			.routeId("erpnext-update-quotation-route")
			.to("frappe://put/resource?inBody=resource")
			.log(LoggingLevel.INFO, "Quotation with ID ${header." + HEADER_FRAPPE_NAME + "} updated successfully.")
				.end();
		// spotless:on

    }
}
