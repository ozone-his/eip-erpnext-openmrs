/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes.customer;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS;
import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_RESOURCE;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class UpdateCustomerRoute extends RouteBuilder {

    @Override
    public void configure() {
        // spotless:off
		from("direct:erpnext-update-customer-route")
			.routeId("erpnext-update-customer-route")
			.to("frappe://put/resource?doctype=Customer")
			.choice()
				.when(exchangeProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS).isNotNull())
					.log(LoggingLevel.DEBUG, "Updating customer address ...")
					.process(exchange -> {
					exchange.getMessage()
							.setHeader(HEADER_FRAPPE_NAME, exchange.getProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME));
					exchange.getMessage()
							.setHeader(HEADER_FRAPPE_RESOURCE, exchange.getProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS));
				})
					.toD("frappe://put/resource?doctype=Address")
				.endChoice()
			.end();
		// spotless:on
    }
}
