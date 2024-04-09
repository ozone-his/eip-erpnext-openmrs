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
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
import static org.apache.camel.builder.AdviceWith.adviceWith;

import com.ozonehis.eip.model.erpnext.Address;
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
class DeleteCustomerRouteTest extends CamelSpringTestSupport {

    private static final String DELETE_CUSTOMER_ROUTE = "direct:erpnext-delete-customer-route";

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new DeleteCustomerRoute();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("erpnext-delete-customer-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe://delete/resource?doctype=Customer")
                        .replace()
                        .to("mock:delete-customer");
                weaveByToUri("frappe://delete/resource?doctype=Address")
                        .replace()
                        .to("mock:delete-address");
            }
        });

        Endpoint endpoint = context.getEndpoint(DELETE_CUSTOMER_ROUTE);
        template.setDefaultEndpoint(endpoint);
    }

    @Test
    @DisplayName("Should delete customer and the associated address.")
    void shouldDeleteCustomerAndAddress() throws Exception {
        // setup
        var deleteHeaders = new HashMap<String, Object>();
        deleteHeaders.put(HEADER_FRAPPE_DOCTYPE, "Customer");
        deleteHeaders.put(HEADER_FRAPPE_NAME, "customer-1");

        Address address = new Address();
        address.setAddressName("address-1");
        address.setCity("City");

        // Expectations
        MockEndpoint mockDeleteCustomer = getMockEndpoint("mock:delete-customer");
        mockDeleteCustomer.expectedMessageCount(1);
        mockDeleteCustomer.expectedHeaderReceived(HEADER_FRAPPE_DOCTYPE, "Customer");
        mockDeleteCustomer.expectedHeaderReceived(HEADER_FRAPPE_NAME, "customer-1");
        mockDeleteCustomer.setResultWaitTime(100);

        MockEndpoint mockDeleteAddress = getMockEndpoint("mock:delete-address");
        mockDeleteAddress.expectedMessageCount(1);

        // Act
        template.send(exchange -> {
            exchange.getMessage().setHeaders(deleteHeaders);
            exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME, address.getAddressName());
            exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, address);
        });

        // Assert
        mockDeleteCustomer.assertIsSatisfied();
        mockDeleteAddress.assertIsSatisfied();
    }
}
