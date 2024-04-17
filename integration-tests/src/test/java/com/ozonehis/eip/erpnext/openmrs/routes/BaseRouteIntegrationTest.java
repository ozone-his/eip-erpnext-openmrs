/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import com.ozonehis.camel.FrappeComponent;
import com.ozonehis.camel.FrappeConfiguration;
import com.ozonehis.camel.frappe.sdk.FrappeClientBuilder;
import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.camel.test.infra.erpnext.services.ERPNextService;
import com.ozonehis.camel.test.infra.erpnext.services.ERPNextServiceFactory;
import com.ozonehis.eip.erpnext.openmrs.TestConstants;
import com.ozonehis.eip.erpnext.openmrs.TestSpringConfiguration;
import com.ozonehis.eip.erpnext.openmrs.handlers.AddressHandler;
import com.ozonehis.eip.erpnext.openmrs.processors.PatientProcessor;
import com.ozonehis.eip.mappers.erpnext.AddressMapper;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Getter;
import org.apache.camel.CamelContext;
import org.apache.camel.test.infra.core.CamelContextExtension;
import org.apache.camel.test.infra.core.DefaultCamelContextExtension;
import org.apache.camel.test.infra.core.annotations.ContextFixture;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ActiveProfiles;

@Getter
@ActiveProfiles("test")
@CamelSpringBootTest
@SpringBootTest(classes = {TestSpringConfiguration.class})
public abstract class BaseRouteIntegrationTest {

    private FrappeClient frappeClient;

    @RegisterExtension
    protected static CamelContextExtension contextExtension = new DefaultCamelContextExtension();

    @RegisterExtension
    protected static final ERPNextService erpNextService = ERPNextServiceFactory.createSingletonService();

    @ContextFixture
    public void configureContext(CamelContext context) {
        final FrappeConfiguration configuration = new FrappeConfiguration();
        configuration.setFrappeClient(createFrappeClient());
        context.getComponent("frappe", FrappeComponent.class).setConfiguration(configuration);
    }
    
    @Test
    @DisplayName("should verify customer routes.")
    public void verifyHasCustomerRoutes() {
        assertTrue(hasRoute(contextExtension.getContext(), "patient-to-customer-router"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-create-customer-route"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-update-customer-route"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-delete-customer-route"));
    }

    protected static FrappeClient createFrappeClient() {
        var apiUrl = "http://" + erpNextService.getHost() + ":" + erpNextService.getPort() + "/api/resource";
        return FrappeClientBuilder.newClient(apiUrl, TestConstants.ERPNEXT_USERNAME, TestConstants.ERPNEXT_PASSWORD)
                .build();
    }

    public FrappeClient getFrappeClient() {
        if (frappeClient == null) {
            frappeClient = createFrappeClient();
        }
        return frappeClient;
    }

    protected @Nonnull PatientRouting getPatientRouting() {
        AddressHandler addressHandler = new AddressHandler();
        addressHandler.setFrappeClient(getFrappeClient());
        PatientProcessor patientProcessor = new PatientProcessor();
        patientProcessor.setAddressHandler(addressHandler);
        patientProcessor.setAddressMapper(new AddressMapper());
        patientProcessor.setMapper(new CustomerMapper());

        PatientRouting patientRouting = new PatientRouting();
        patientRouting.setPatientProcessor(patientProcessor);
        return patientRouting;
    }

    protected boolean hasRoute(CamelContext context, String routeId) {
        return context.getRoute(routeId) != null;
    }

    /**
     * Send a body to an endpoint.
     *
     * @param endpoint the endpoint to send the body to.
     * @param body     the body to send.
     */
    protected void sendBody(String endpoint, Object body) {
        contextExtension.getProducerTemplate().sendBody(endpoint, body);
    }

    /**
     * Send a body and a header to an endpoint.
     *
     * @param endpoint    the endpoint to send the body to.
     * @param body        the body to send.
     * @param headerName  the name of the header to send.
     * @param headerValue the value of the header to send.
     */
    protected void sendBodyAndHeader(String endpoint, Object body, String headerName, Object headerValue) {
        contextExtension.getProducerTemplate().sendBodyAndHeader(endpoint, body, headerName, headerValue);
    }

    /**
     * Send a body and headers to an endpoint.
     *
     * @param endpoint the endpoint to send the body to.
     * @param body     the body to send.
     * @param headers  the headers to send.
     */
    protected void sendBodyAndHeaders(String endpoint, Object body, Map<String, Object> headers) {
        contextExtension
                .getProducerTemplate()
                .sendBodyAndHeaders(contextExtension.getContext().getEndpoint(endpoint), body, headers);
    }

    /**
     * Read a resource from ERPNext.
     *
     * @param doctype the doctype of the resource to read.
     * @param name    the name of the resource to read.
     * @return the response from the read request.
     */
    protected FrappeResponse read(String doctype, String name) {
        return getFrappeClient().get(doctype, name).execute();
    }

    /**
     * Read a resource from ERPNext.
     *
     * @param doctype the doctype of the resource to read.
     * @param fields  the fields to include in the response.
     * @param filters the filters to apply to the request.
     * @return the response from the read request.
     */
    protected FrappeResponse read(String doctype, List<String> fields, List<String> filters) {
        return getFrappeClient()
                .get(doctype)
                .withFields(fields)
                .withFilters(List.of(filters))
                .execute();
    }

    /**
     * Load resource from a file path.
     *
     * @param filePath the file path of the resource to load.
     * @param resource resource object
     * @param <T>      The type of the resource to load e.g., Patient, Encounter, etc.
     * @return resource object
     */
    @SuppressWarnings("unchecked")
    protected <T extends Resource> T loadResource(String filePath, T resource) {
        FhirContext ctx = FhirContext.forR4();
        return (T) ctx.newJsonParser().parseResource(resource.getClass(), readJSON(filePath));
    }

    /**
     * Read JSON file from the classpath.
     *
     * @param filePath the file path of the JSON file to read.
     * @return JSON content as a string
     */
    protected String readJSON(String filePath) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
        if (is == null) {
            throw new IllegalArgumentException("File not found! " + filePath);
        } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
