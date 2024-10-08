/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.processors;

import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.ozonehis.eip.erpnext.openmrs.handlers.CustomerHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.ItemHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.erpnext.openmrs.routes.quotation.CreateQuotationRoute;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationMapper;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.CustomerType;
import java.util.List;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@UseAdviceWith
class ServiceRequestProcessorTest extends BaseProcessorTest {

    @Mock
    private QuotationMapper quotationMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private QuotationHandler quotationHandler;

    @Mock
    private CustomerHandler customerHandler;

    @Mock
    private ItemHandler itemHandler;

    @Mock
    private ProducerTemplate producerTemplate;

    @InjectMocks
    private ServiceRequestProcessor serviceRequestProcessor;

    private static AutoCloseable mocksCloser;

    @BeforeEach
    void setup() throws Exception {
        mocksCloser = openMocks(this);

        adviceWith("erpnext-create-quotation-route", context, new AdviceWithRouteBuilder() {

            @Override
            public void configure() {
                weaveByToUri("frappe:*").replace().to("mock:result");
            }
        });
    }

    @AfterAll
    static void close() throws Exception {
        mocksCloser.close();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new CreateQuotationRoute();
    }

    @Test
    @DisplayName("Should process service request with create event type")
    void shouldProcessServiceRequestWithCreateEventType() {
        // setup
        Bundle bundle = mock(Bundle.class);
        Patient patient = mock(Patient.class);
        Encounter encounter = mock(Encounter.class);
        ServiceRequest serviceRequest = mock(ServiceRequest.class);
        Customer customer = new Customer();
        customer.setCustomerId("12345");
        customer.setCustomerName("John Doe");
        customer.setCustomerType(CustomerType.INDIVIDUAL);

        Exchange exchange = createExchange(bundle, "c");

        // Mock behavior
        when(bundle.getEntry())
                .thenReturn(List.of(
                        new Bundle.BundleEntryComponent().setResource(patient),
                        new Bundle.BundleEntryComponent().setResource(encounter),
                        new Bundle.BundleEntryComponent().setResource(serviceRequest)));
        when(serviceRequest.getStatus()).thenReturn(ServiceRequest.ServiceRequestStatus.ACTIVE);
        when(serviceRequest.getIntent()).thenReturn(ServiceRequest.ServiceRequestIntent.ORDER);

        Reference visitReference = mock(Reference.class);
        when(visitReference.getReference()).thenReturn("Encounter/123");
        when(encounter.getPartOf()).thenReturn(visitReference);
        when(quotationMapper.toERPNext(encounter)).thenReturn(new com.ozonehis.eip.model.erpnext.Quotation());
        when(customerMapper.toERPNext(patient)).thenReturn(customer);
        when(itemHandler.createQuotationItemIfItemExists(serviceRequest))
                .thenReturn(Optional.of(new com.ozonehis.eip.model.erpnext.QuotationItem()));
        // when(quotationHandler.quotationExists(anyString())).thenReturn(false);
        when(patient.getIdPart()).thenReturn("12345");
        when(customerHandler.getCustomer(anyString())).thenReturn(Optional.of(customer));

        // Act
        serviceRequestProcessor.process(exchange);

        // Verify
        verify(customerHandler, times(1)).syncCustomerWithPatient(any(), any());
        verify(quotationHandler, times(1)).sendQuotation(any(), anyString(), any());
        verify(quotationHandler, times(1)).getQuotation(anyString());
        verify(itemHandler, times(1)).createQuotationItemIfItemExists(serviceRequest);
        verify(customerMapper, times(1)).toERPNext(patient);
    }

    @Test
    @DisplayName("Should throw exception when event type is not found")
    void shouldThrowExceptionWhenEventTypeIsNotFound() {
        // setup
        Bundle bundle = mock(Bundle.class);
        Exchange exchange = createExchange(bundle, null);

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> serviceRequestProcessor.process(exchange));
    }

    @Test
    @DisplayName("Should process service request with delete event type")
    void shouldProcessServiceRequestWithDeleteEventType() {
        // setup
        Bundle bundle = mock(Bundle.class);
        Patient patient = mock(Patient.class);
        Encounter encounter = mock(Encounter.class);
        ServiceRequest serviceRequest = mock(ServiceRequest.class);
        Customer customer = new Customer();
        customer.setCustomerId("12345");
        customer.setCustomerName("John Doe");
        customer.setCustomerType(CustomerType.INDIVIDUAL);

        Exchange exchange = createExchange(bundle, "d");

        // Mock behavior
        when(bundle.getEntry())
                .thenReturn(List.of(
                        new Bundle.BundleEntryComponent().setResource(patient),
                        new Bundle.BundleEntryComponent().setResource(encounter),
                        new Bundle.BundleEntryComponent().setResource(serviceRequest)));

        Reference visitReference = mock(Reference.class);
        when(visitReference.getReference()).thenReturn("Encounter/123");
        when(encounter.getPartOf()).thenReturn(visitReference);
        when(quotationMapper.toERPNext(encounter)).thenReturn(new com.ozonehis.eip.model.erpnext.Quotation());
        when(customerMapper.toERPNext(patient)).thenReturn(customer);
        when(itemHandler.createQuotationItemIfItemExists(serviceRequest))
                .thenReturn(Optional.of(new com.ozonehis.eip.model.erpnext.QuotationItem()));
        when(patient.getIdPart()).thenReturn("12345");
        when(customerHandler.getCustomer(anyString())).thenReturn(Optional.of(customer));
        when(quotationHandler.getQuotation(anyString())).thenReturn(new com.ozonehis.eip.model.erpnext.Quotation());

        // Act
        serviceRequestProcessor.process(exchange);

        // Verify
        verify(quotationHandler, times(1)).sendQuotation(any(), anyString(), any());
        verify(quotationHandler, times(1)).getQuotation(anyString());
        verify(itemHandler, times(1)).createQuotationItemIfItemExists(serviceRequest);
    }

    @Test
    @DisplayName("Should throw exception for unsupported event type")
    void shouldThrowExceptionForUnsupportedEventType() {
        // setup
        Bundle bundle = mock(Bundle.class);
        Exchange exchange = createExchange(bundle, "x");

        // Act and Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceRequestProcessor.process(exchange),
                "Unsupported event type");
    }
}
