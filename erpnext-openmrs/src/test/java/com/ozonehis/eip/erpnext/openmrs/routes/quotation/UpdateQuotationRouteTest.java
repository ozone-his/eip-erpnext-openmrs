/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes.quotation;

import static org.apache.camel.builder.AdviceWith.adviceWith;

import com.ozonehis.eip.model.erpnext.Quotation;
import java.util.HashMap;
import org.apache.camel.Endpoint;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

@UseAdviceWith
class UpdateQuotationRouteTest extends CamelSpringTestSupport {

    private static final String UPDATE_QUOTATION_ROUTE = "direct:erpnext-update-quotation-route";

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new UpdateQuotationRoute();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("erpnext-update-quotation-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe://put/resource?inBody=resource").replace().to("mock:update-quotation");
            }
        });

        Endpoint endpoint = context.getEndpoint(UPDATE_QUOTATION_ROUTE);
        template.setDefaultEndpoint(endpoint);
    }

    @Test
    @DisplayName("Should update quotation.")
    public void shouldUpdateQuotation() throws InterruptedException {
        // setup
        Quotation quotation = new Quotation();
        quotation.setQuotationId("12345");
        var updateQuotationHeaders = new HashMap<String, Object>();
        updateQuotationHeaders.put("frappe_doctype", "Quotation");
        updateQuotationHeaders.put("frappe_name", "12345");
        updateQuotationHeaders.put("resource", quotation);

        // Expectations
        MockEndpoint mockUpdateQuotationEndpoint = getMockEndpoint("mock:update-quotation");
        mockUpdateQuotationEndpoint.expectedMessageCount(1);
        mockUpdateQuotationEndpoint.expectedHeaderReceived("frappe_doctype", "Quotation");
        mockUpdateQuotationEndpoint.expectedHeaderReceived("frappe_name", "12345");
        mockUpdateQuotationEndpoint.expectedHeaderReceived("resource", quotation);

        // Act
        template.send(UPDATE_QUOTATION_ROUTE, exchange -> {
            exchange.getMessage().setHeaders(updateQuotationHeaders);
        });

        // Assert
        mockUpdateQuotationEndpoint.assertIsSatisfied();
    }
}
