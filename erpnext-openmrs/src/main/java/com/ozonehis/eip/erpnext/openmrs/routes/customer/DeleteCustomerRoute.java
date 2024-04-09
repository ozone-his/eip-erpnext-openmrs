/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes.customer;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class DeleteCustomerRoute extends RouteBuilder {

    @Override
    public void configure() {
        // spotless:off
		from("direct:erpnext-delete-customer-route")
			.routeId("erpnext-delete-customer-route")
			.to("frappe://delete/resource?doctype=Customer")
			.choice()
				.when(exchangeProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME).isNotNull())
					.setHeader(HEADER_FRAPPE_DOCTYPE, constant("Address"))
					.setHeader(HEADER_FRAPPE_NAME, simple("${exchangeProperty." + EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME + "}"))
					.to("frappe://delete/resource?doctype=Address")
				.end()
			.end();
		// spotless:on
    }
}
