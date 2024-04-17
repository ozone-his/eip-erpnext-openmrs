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
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.eip.erpnext.openmrs.handlers.CustomerHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.ItemHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.erpnext.openmrs.processors.ServiceRequestProcessor;
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
import com.ozonehis.eip.model.erpnext.Quotation;
import java.util.HashMap;
import org.apache.camel.CamelContext;
import org.apache.camel.test.infra.core.annotations.RouteFixture;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ServiceRequestToQuotationIntegrationTest extends BaseRouteIntegrationTest {

    private Bundle serviceRequestBundle;

    private static final String ENCOUNTER_PART_OF_UUID = "7c164b93-83fa-41a9-95fe-4630231a8ff1";

    private static final String PATIENT_UUID = "79355a93-3a4f-4490-98aa-278f922fa87c";

    @BeforeEach
    public void initializeData() {
        serviceRequestBundle = loadResource("fhir.bundle/service-request-bundle.json", new Bundle());
    }

    @RouteFixture
    public void createRouteBuilder(CamelContext context) throws Exception {
        context.addRoutes(getPatientRouting());
        // Add customer routes
        context.addRoutes(new CreateCustomerRoute());
        context.addRoutes(new UpdateCustomerRoute());
        context.addRoutes(new DeleteCustomerRoute());

        ServiceRequestProcessor serviceRequestProcessor = new ServiceRequestProcessor();
        serviceRequestProcessor.setQuotationMapper(new QuotationMapper());
        serviceRequestProcessor.setCustomerMapper(new CustomerMapper());

        QuotationHandler quotationHandler = new QuotationHandler();
        quotationHandler.setFrappeClient(getFrappeClient());
        serviceRequestProcessor.setQuotationHandler(quotationHandler);

        CustomerHandler customerHandler = new CustomerHandler();
        customerHandler.setFrappeClient(getFrappeClient());
        serviceRequestProcessor.setCustomerHandler(customerHandler);

        ItemHandler itemHandler = new ItemHandler();
        QuotationItemMapper<Resource> quotationItemMapper = new QuotationItemMapper<>();
        itemHandler.setQuotationItemMapper(quotationItemMapper);
        itemHandler.setFrappeClient(getFrappeClient());
        serviceRequestProcessor.setItemHandler(itemHandler);

        ServiceRequestRouting serviceRequestRouting = new ServiceRequestRouting();
        serviceRequestRouting.setServiceRequestProcessor(serviceRequestProcessor);

        context.addRoutes(serviceRequestRouting);
        context.addRoutes(new CreateQuotationRoute());
        context.addRoutes(new UpdateQuotationRoute());
    }

    @Test
    @DisplayName("Should verify has quotation routes.")
    public void shouldVerifyQuotationRoutes() {
        assertTrue(hasRoute(contextExtension.getContext(), "service-request-to-quotation-router"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-create-quotation-route"));
        assertTrue(hasRoute(contextExtension.getContext(), "erpnext-update-quotation-route"));
    }

    @Test
    @DisplayName("Should create quotation in ERPNext given service request bundle.")
    public void shouldCreateQuotationInERPNextGivenServiceRequestBundle() {
        // Act
        var headers = new HashMap<String, Object>();
        headers.put(HEADER_FHIR_EVENT_TYPE, "c");
        sendBodyAndHeaders("direct:service-request-to-quotation-processor", serviceRequestBundle, headers);

        // Verify
        FrappeResponse result = read("Quotation", ENCOUNTER_PART_OF_UUID);
        assertNotNull(result);
        assertEquals(200, result.code());

        Quotation quotation = result.returnAs(new TypeReference<FrappeSingularDataWrapper<Quotation>>() {})
                .getData();
        assertNotNull(quotation);
        assertEquals(ENCOUNTER_PART_OF_UUID, quotation.getQuotationId());
        assertEquals("Sales", quotation.getOrderType());

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
