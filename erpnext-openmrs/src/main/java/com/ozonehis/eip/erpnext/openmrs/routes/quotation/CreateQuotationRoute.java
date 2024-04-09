/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
