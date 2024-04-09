package com.ozonehis.eip.erpnext.openmrs.routes.customer;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS;
import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openmrs.eip.fhir.Constants;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

@UseAdviceWith
class UpdateCustomerRouteTest extends CamelSpringTestSupport {

    private static final String UPDATE_CUSTOMER_ROUTE = "direct:erpnext-update-customer-route";

    private MockEndpoint mockUpdateCustomerEndpoint;

    private MockEndpoint mockUpdateAddressEndpoint;

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new UpdateCustomerRoute();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("erpnext-update-customer-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe://put/resource?doctype=Customer").replace().to("mock:update-customer");
                weaveByToUri("frappe://put/resource?doctype=Address").replace().to("mock:update-address");
            }
        });

        Endpoint endpoint = context.getEndpoint(UPDATE_CUSTOMER_ROUTE);
        template.setDefaultEndpoint(endpoint);
    }

    @AfterEach
    void resetMockEndpoints() {
        mockUpdateCustomerEndpoint.reset();
        mockUpdateAddressEndpoint.reset();
    }

    @Test
    @DisplayName("Should update customer and address.")
    void shouldUpdateCustomerAndAddress() throws Exception {
        // setup
        Customer customer = new Customer();
        customer.setCustomerId("customer-1");
        customer.setCustomerName("Customer 1");

        Address address = new Address();
        address.setAddressName("address-1");
        address.setCity("City");
        address.setAddressLine1("Address Line 1");

        var updateHeaders = new HashMap<String, Object>();
        updateHeaders.put(HEADER_FRAPPE_DOCTYPE, "Customer");
        updateHeaders.put(HEADER_FRAPPE_NAME, "customer-1");
        updateHeaders.put(HEADER_FRAPPE_RESOURCE, customer);
        updateHeaders.put(Constants.HEADER_FHIR_EVENT_TYPE, "u");

        // Expectations
        mockUpdateCustomerEndpoint = getMockEndpoint("mock:update-customer");
        mockUpdateCustomerEndpoint.expectedHeaderReceived(HEADER_FRAPPE_DOCTYPE, "Customer");
        mockUpdateCustomerEndpoint.expectedHeaderReceived(HEADER_FRAPPE_NAME, "customer-1");
        mockUpdateCustomerEndpoint.expectedHeaderReceived(HEADER_FRAPPE_RESOURCE, customer);
        mockUpdateCustomerEndpoint.expectedHeaderReceived(Constants.HEADER_FHIR_EVENT_TYPE, "u");
        mockUpdateCustomerEndpoint.setResultWaitTime(100);

        mockUpdateAddressEndpoint = getMockEndpoint("mock:update-address");
        mockUpdateAddressEndpoint.expectedPropertyReceived(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, address);
        mockUpdateAddressEndpoint.expectedPropertyReceived(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME, "address-1");
        mockUpdateAddressEndpoint.setResultWaitTime(100);

        // Act
        template.send(UPDATE_CUSTOMER_ROUTE, exchange -> {
            exchange.getMessage().setHeaders(updateHeaders);
            exchange.getMessage().setBody(customer);
            exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, address);
            exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME, address.getAddressName());
        });

        // Assert
        mockUpdateCustomerEndpoint.assertIsSatisfied();
        mockUpdateAddressEndpoint.assertIsSatisfied();
    }
}
