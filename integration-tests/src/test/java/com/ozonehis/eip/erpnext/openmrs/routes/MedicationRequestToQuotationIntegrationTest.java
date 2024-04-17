/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.routes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ozonehis.eip.erpnext.openmrs.handlers.CustomerHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.ItemHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.erpnext.openmrs.processors.MedicationRequestProcessor;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.CreateCustomerRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.DeleteCustomerRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.customer.UpdateCustomerRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.quotation.CreateQuotationRoute;
import com.ozonehis.eip.erpnext.openmrs.routes.quotation.UpdateQuotationRoute;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationItemMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationMapper;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import com.ozonehis.eip.model.erpnext.OrderType;
import com.ozonehis.eip.model.erpnext.Quotation;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.test.infra.core.annotations.RouteFixture;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MedicationRequestToQuotationIntegrationTest extends BaseRouteIntegrationTest {

    private Bundle medicationRequestBundle;

    private static final String ENCOUNTER_PART_OF_UUID = "26616e46-2cfe-4563-afaa-c243ca94f4c7";

    private static final String PATIENT_UUID = "79355a93-3a4f-4490-98aa-278f922fa87c";

    @BeforeEach
    public void initializeData() {
        medicationRequestBundle = loadResource("fhir.bundle/medication-request-bundle.json", new Bundle());
    }

    @RouteFixture
    public void createRouteBuilder(CamelContext context) throws Exception {
        context.addRoutes(getPatientRouting());
        // Add customer routes
        context.addRoutes(new CreateCustomerRoute());
        context.addRoutes(new UpdateCustomerRoute());
        context.addRoutes(new DeleteCustomerRoute());

        MedicationRequestProcessor medicationRequestProcessor = new MedicationRequestProcessor();
        medicationRequestProcessor.setQuotationMapper(new QuotationMapper());
        medicationRequestProcessor.setCustomerMapper(new CustomerMapper());

        QuotationHandler quotationHandler = new QuotationHandler();
        quotationHandler.setFrappeClient(getFrappeClient());
        medicationRequestProcessor.setQuotationHandler(quotationHandler);

        CustomerHandler customerHandler = new CustomerHandler();
        customerHandler.setFrappeClient(getFrappeClient());
        medicationRequestProcessor.setCustomerHandler(customerHandler);

        ItemHandler itemHandler = new ItemHandler();
        QuotationItemMapper<Resource> quotationItemMapper = new QuotationItemMapper<>();
        itemHandler.setQuotationItemMapper(quotationItemMapper);
        itemHandler.setFrappeClient(getFrappeClient());
        medicationRequestProcessor.setItemHandler(itemHandler);

        MedicationRequestRouting medicationRequestRouting = new MedicationRequestRouting();
        medicationRequestRouting.setMedicationRequestProcessor(medicationRequestProcessor);

        context.addRoutes(medicationRequestRouting);
        context.addRoutes(new CreateQuotationRoute());
        context.addRoutes(new UpdateQuotationRoute());
    }

    @Test
    @DisplayName("Should verify has quotation routes.")
    public void shouldVerifyQuotationRoutes() {
        assertTrue(hasRoute(contextExtension.getContext(), "medication-request-to-quotation-router"));
        assertTrue(hasRoute(contextExtension.getContext(), "medication-request-to-quotation-processor"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-create-quotation-route"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-update-quotation-route"));
    }

    @Test
    @DisplayName("Should create quotation in ERPNext given medication request bundle.")
    public void shouldCreateQuotationInERPNextGivenMedicationRequestBundle() {
        // Act
        var headers = new HashMap<String, Object>();
        headers.put(HEADER_FHIR_EVENT_TYPE, "c");
        sendBodyAndHeaders("direct:medication-request-to-quotation-processor", medicationRequestBundle, headers);

        // Verify quotation created
        var result = read("Quotation", ENCOUNTER_PART_OF_UUID);
        assertNotNull(result);
        assertEquals(200, result.code());

        var quotation = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Quotation>>() {})
                .getData();
        assertNotNull(quotation);
        assertEquals(ENCOUNTER_PART_OF_UUID, quotation.getQuotationId());
        assertEquals(OrderType.SALES, quotation.getOrderType());

        // verify quotation has items
        var items = quotation.getItems();
        assertFalse(items.isEmpty());
        assertThat(items).hasSize(1);

        // Verify customer created
        result = read("Customer", PATIENT_UUID);
        assertNotNull(result);
        assertEquals(200, result.code());

        var customer = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Customer>>() {})
                .getData();
        assertNotNull(customer);
        assertEquals(PATIENT_UUID, customer.getCustomerId());
    }
}
