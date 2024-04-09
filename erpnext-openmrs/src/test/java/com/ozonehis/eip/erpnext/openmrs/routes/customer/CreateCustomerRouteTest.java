/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes.customer;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_RESOURCE;
import static org.apache.camel.builder.AdviceWith.adviceWith;

import com.ozonehis.eip.model.erpnext.Address;
import com.ozonehis.eip.model.erpnext.Customer;
import java.util.HashMap;
import org.apache.camel.Endpoint;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

@UseAdviceWith
public class CreateCustomerRouteTest extends CamelSpringTestSupport {

    private static final String CREATE_CUSTOMER_ROUTE = "direct:erpnext-create-customer-route";

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new CreateCustomerRoute();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("erpnext-create-customer-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe://post/resource?doctype=Customer")
                        .replace()
                        .to("mock:create-customer");
                weaveByToUri("frappe://post/resource?doctype=Address").replace().to("mock:create-address");
            }
        });

        Endpoint defaultEndpoint = context.getEndpoint(CREATE_CUSTOMER_ROUTE);
        template.setDefaultEndpoint(defaultEndpoint);
    }

    @Test
    public void shouldCreateCustomerWithAddress() throws Exception {
        Customer customer = new Customer();
        customer.setCustomerId("12345");
        customer.setCustomerName("John Doe");
        Address address = new Address();
        address.setAddressName("12345");
        address.setCity("City");
        address.setAddressLine1("Address Line 1");

        var createHeaders = new HashMap<String, Object>();
        createHeaders.put(HEADER_FRAPPE_DOCTYPE, "Customer");
        createHeaders.put(HEADER_FRAPPE_RESOURCE, customer);

        // Expectations
        MockEndpoint mockCreateCustomerEndpoint = getMockEndpoint("mock:create-customer");
        mockCreateCustomerEndpoint.expectedMessageCount(1);
        mockCreateCustomerEndpoint.expectedHeaderReceived(HEADER_FRAPPE_DOCTYPE, "Customer");
        mockCreateCustomerEndpoint.expectedHeaderReceived(HEADER_FRAPPE_RESOURCE, customer);
        mockCreateCustomerEndpoint.setResultWaitTime(100);

        MockEndpoint mockCreateAddressEndpoint = getMockEndpoint("mock:create-address");
        mockCreateAddressEndpoint.expectedMessageCount(1);
        mockCreateAddressEndpoint.expectedPropertyReceived(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, address);
        mockCreateAddressEndpoint.setResultWaitTime(100);

        // Act
        template.send(CREATE_CUSTOMER_ROUTE, exchange -> {
            exchange.getMessage().setHeaders(createHeaders);
            exchange.getMessage().setBody(customer);
            exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, address);
        });

        // Verify
        mockCreateCustomerEndpoint.assertIsSatisfied();
        mockCreateAddressEndpoint.assertIsSatisfied();
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }
}
