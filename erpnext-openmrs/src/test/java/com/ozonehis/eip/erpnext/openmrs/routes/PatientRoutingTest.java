package com.ozonehis.eip.erpnext.openmrs.routes;

import static org.apache.camel.builder.AdviceWith.adviceWith;

import com.ozonehis.eip.erpnext.openmrs.processors.PatientProcessor;
import java.util.List;
import org.apache.camel.Endpoint;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

@Disabled
@UseAdviceWith
class PatientRoutingTest extends CamelSpringTestSupport {

    private static final String PATIENT_TO_CUSTOMER_ROUTER = "direct:patient-to-customer-router";

    private static final String CREATE_CUSTOMER_ROUTE = "direct:erpnext-create-patient-route";

    private static final String UPDATE_CUSTOMER_ROUTE = "direct:erpnext-update-patient-route";

    private PatientProcessor patientProcessor;

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new PatientRouting();
    }

    @BeforeEach
    void setup() throws Exception {
        adviceWith("patient-to-customer-router", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("direct:erpnext-create-customer-route").replace().to("mock:create-customer");
            }
        });

        Endpoint endpoint = context.getEndpoint(PATIENT_TO_CUSTOMER_ROUTER);
        template.setDefaultEndpoint(endpoint);
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        Patient patient = new Patient();
        patient.setId("1");
        patient.setActive(true);
        patient.setName(List.of(new HumanName().setFamily("Doe").addGiven("John")));

        // Expectations
        MockEndpoint mockCreateCustomerEndpoint = getMockEndpoint("mock:create-customer");
        mockCreateCustomerEndpoint.expectedMessageCount(1);

        // Act
        template.send(PATIENT_TO_CUSTOMER_ROUTER, exchange -> {
            exchange.getIn().setHeader("fhirEvent", "c");
            exchange.getIn().setBody(patient);
            exchange.getMessage().setHeader("fhirEvent", "c");
            exchange.getMessage().setBody(patient);
        });

        // Assert
        mockCreateCustomerEndpoint.assertIsSatisfied();
    }
}
