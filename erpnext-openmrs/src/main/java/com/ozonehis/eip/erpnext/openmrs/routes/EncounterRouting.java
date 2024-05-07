/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_SKIP_ENCOUNTER;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;

import com.ozonehis.eip.erpnext.openmrs.processors.EncounterProcessor;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncounterRouting extends RouteBuilder {

    @Autowired
    private EncounterProcessor encounterProcessor;

    @Override
    public void configure() throws Exception {
        // spotless:off
		from("direct:encounter-to-quotation-router")
			.routeId("encounter-to-quotation-router")
			.process(encounterProcessor)
			.choice()
				.when(simple("${exchangeProperty." + EXCHANGE_PROPERTY_SKIP_ENCOUNTER + "} == true"))
					.log(LoggingLevel.DEBUG, "Skip encounter processing, quotation not found or encounter(visit) is not closed.")
				.endChoice()
				.when(simple("${exchangeProperty." + EXCHANGE_PROPERTY_SKIP_ENCOUNTER + "} == false"))
					.log(LoggingLevel.INFO, "Processing encounter with ID ${header." + HEADER_FRAPPE_NAME + "}")
					.to("direct:erpnext-update-quotation-route")
				.endChoice()
			.end().end();
		
		from("direct:fhir-encounter")
			.routeId("fhir-encounter-to-quotation-router")
			.to("direct:encounter-to-quotation-router")
				.end();
		// spotless:on
    }
}
