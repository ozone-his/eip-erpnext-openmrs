package com.ozonehis.eip.erpnext.openmrs.routes.quotation;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
import static org.apache.camel.builder.AdviceWith.adviceWith;

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
class CreateQuotationRouteTest extends CamelSpringTestSupport {

    private static final String CREATE_QUOTATION_ROUTE = "direct:erpnext-create-quotation-route";

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new CreateQuotationRoute();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("erpnext-create-quotation-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe://post/resource?inBody=resource").replace().to("mock:create-quotation");
            }
        });

        Endpoint endpoint = context.getEndpoint(CREATE_QUOTATION_ROUTE);
        template.setDefaultEndpoint(endpoint);
    }

    @Test
    public void shouldCreateQuotation() throws Exception {
        // setup
        var createQuotationHeaders = new HashMap<String, Object>();
        createQuotationHeaders.put(HEADER_FRAPPE_DOCTYPE, "Quotation");
        createQuotationHeaders.put(HEADER_FRAPPE_NAME, "12345");

        // Expectations
        MockEndpoint mockCreateQuotationEndpoint = getMockEndpoint("mock:create-quotation");
        mockCreateQuotationEndpoint.expectedMessageCount(1);
        mockCreateQuotationEndpoint.expectedHeaderReceived(HEADER_FRAPPE_DOCTYPE, "Quotation");
        mockCreateQuotationEndpoint.expectedHeaderReceived(HEADER_FRAPPE_NAME, "12345");

        // Act
        template.send(CREATE_QUOTATION_ROUTE, exchange -> {
            exchange.getMessage().setHeaders(createQuotationHeaders);
        });

        // Assert
        mockCreateQuotationEndpoint.assertIsSatisfied();
    }
}
