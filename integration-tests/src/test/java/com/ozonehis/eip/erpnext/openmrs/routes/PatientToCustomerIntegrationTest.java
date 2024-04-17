/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_ENABLE_PATIENT_SYNC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.CreateCustomerRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.DeleteCustomerRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.UpdateCustomerRoute;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.CustomerType;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.test.infra.core.annotations.RouteFixture;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PatientToCustomerIntegrationTest extends BaseRouteIntegrationTest {

    private Patient patient1;

    private static final String PATIENT_1_UUID = "df7182cb-eb6e-4160-9f70-2efb0b6d5d74";

    private static final String ADDRESS_1_UUID = "50c6b3fd-65aa-4628-aa56-6b02287f77ca";

    private static final String PATIENT_IDENTIFIER_1_VALUE = "10000GX";

    private Patient patient2;

    private static final String PATIENT_2_UUID = "d238321f-40ba-4dea-b307-3fa95336bc9f";

    private static final String ADDRESS_2_UUID = "c09315e5-a8d0-4325-98ea-cd6730cb9944";

    private static final String PATIENT_IDENTIFIER_2_VALUE = "100008E";

    @RouteFixture
    public void createRouteBuilder(CamelContext context) throws Exception {
        context.addRoutes(getPatientRouting());
        // Add customer routes
        context.addRoutes(new CreateCustomerRoute());
        context.addRoutes(new UpdateCustomerRoute());
        context.addRoutes(new DeleteCustomerRoute());
    }

    @BeforeEach
    public void initializeData() {
        patient1 = loadResource("fhir/patient/patient-1.json", new Patient());
        patient2 = loadResource("fhir/patient/patient-2.json", new Patient());
    }

    @Test
    @DisplayName("should create customer in ERPNext given FHIR patient.")
    public void shouldCreateCustomerInERPNextGivenFhirPatient() {
        // Act
        var headers = new HashMap<String, Object>();
        headers.put(HEADER_FHIR_EVENT_TYPE, "c");
        headers.put(HEADER_ENABLE_PATIENT_SYNC, true);
        sendBodyAndHeaders("direct:patient-to-customer-router", patient1, headers);

        // verify
        FrappeResponse result = read("Customer", PATIENT_1_UUID);

        assertNotNull(result);
        assertEquals(200, result.code());

        Customer createdCustomer = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Customer>>() {})
                .getData();
        assertNotNull(createdCustomer);
        assertEquals(createdCustomer.getCustomerId(), PATIENT_1_UUID);
        assertEquals("Jones, Richard - " + PATIENT_IDENTIFIER_1_VALUE, createdCustomer.getCustomerName());
        assertEquals(CustomerType.INDIVIDUAL, createdCustomer.getCustomerType());
    }

    @Test
    @DisplayName("should update customer in ERPNext given updated FHIR patient.")
    public void shouldUpdateCustomerInERPNextGivenFhirPatient() {
        // Act
        var headers = new HashMap<String, Object>();
        headers.put(HEADER_FHIR_EVENT_TYPE, "c");
        headers.put(HEADER_ENABLE_PATIENT_SYNC, true);
        sendBodyAndHeaders("direct:patient-to-customer-router", patient2, headers);

        // verify
        FrappeResponse result = read("Customer", PATIENT_2_UUID);

        assertNotNull(result);
        assertEquals(200, result.code());

        Customer createdCustomer = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Customer>>() {})
                .getData();
        assertNotNull(createdCustomer);
        assertEquals(createdCustomer.getCustomerId(), PATIENT_2_UUID);
        assertEquals("Johnson, Joshua - " + PATIENT_IDENTIFIER_2_VALUE, createdCustomer.getCustomerName());
        assertEquals(CustomerType.INDIVIDUAL, createdCustomer.getCustomerType());

        // Update patient
        patient2 = loadResource("fhir/patient/patient-2-updated.json", new Patient());
        headers.put(HEADER_FHIR_EVENT_TYPE, "u");
        sendBodyAndHeaders("direct:patient-to-customer-router", patient2, headers);

        // verify
        result = read("Customer", PATIENT_2_UUID);

        assertNotNull(result);
        assertEquals(200, result.code());

        Customer updatedCustomer = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Customer>>() {})
                .getData();
        assertNotNull(updatedCustomer);
        assertEquals(updatedCustomer.getCustomerId(), PATIENT_2_UUID);
        assertEquals("James, Test - " + PATIENT_IDENTIFIER_2_VALUE, updatedCustomer.getCustomerName());
    }

    @Test
    @DisplayName("should delete customer in ERPNext given deleted FHIR patient.")
    public void shouldDeleteCustomerInERPNextGivenDeletedFhirPatient() {
        // Act
        var headers = new HashMap<String, Object>();
        headers.put(HEADER_FHIR_EVENT_TYPE, "c");
        headers.put(HEADER_ENABLE_PATIENT_SYNC, true);
        sendBodyAndHeaders("direct:patient-to-customer-router", patient1, headers);

        // verify customer was created.
        FrappeResponse result = read("Customer", PATIENT_1_UUID);

        assertNotNull(result);
        assertEquals(200, result.code());

        Customer createdCustomer = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Customer>>() {})
                .getData();

        assertNotNull(createdCustomer);
        assertEquals(createdCustomer.getCustomerId(), PATIENT_1_UUID);
        assertEquals("Jones, Richard - " + PATIENT_IDENTIFIER_1_VALUE, createdCustomer.getCustomerName());
        assertEquals(CustomerType.INDIVIDUAL, createdCustomer.getCustomerType());

        // Delete patient
        headers.put(HEADER_FHIR_EVENT_TYPE, "d");
        sendBodyAndHeaders("direct:patient-to-customer-router", patient1, headers);

        // verify
        result = read("Customer", PATIENT_1_UUID);

        assertNotNull(result);
        assertEquals(404, result.code());
    }
}
