package com.ozonehis.eip.erpnext.openmrs.processors;

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
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationMapper;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.CustomerType;
import java.util.List;
import java.util.Optional;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class MedicationRequestProcessorTest extends BaseProcessorTest {

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

    private static AutoCloseable mocksCloser;

    @InjectMocks
    private MedicationRequestProcessor medicationRequestProcessor;

    @BeforeEach
    void setup() {
        mocksCloser = openMocks(this);
    }

    @AfterAll
    static void close() throws Exception {
        mocksCloser.close();
    }

    @Test
    @DisplayName("Should process medication request with create event type.")
    void shouldProcessMedicationRequestWithCreateEventType() {
        // setup
        Bundle bundle = mock(Bundle.class);
        Patient patient = mock(Patient.class);
        Encounter encounter = mock(Encounter.class);
        Medication medication = mock(Medication.class);
        MedicationRequest medicationRequest = mock(MedicationRequest.class);
        Customer customer = new Customer();
        customer.setCustomerId("12345");
        customer.setCustomerName("John Doe");
        customer.setCustomerType(CustomerType.INDIVIDUAL.getValue());

        Exchange exchange = createExchange(bundle, "c");

        // Mock behavior
        when(bundle.getEntry())
                .thenReturn(List.of(
                        new Bundle.BundleEntryComponent().setResource(patient),
                        new Bundle.BundleEntryComponent().setResource(encounter),
                        new Bundle.BundleEntryComponent().setResource(medication),
                        new Bundle.BundleEntryComponent().setResource(medicationRequest)));
        when(medicationRequest.getStatus()).thenReturn(MedicationRequest.MedicationRequestStatus.ACTIVE);
        when(medicationRequest.getIntent()).thenReturn(MedicationRequest.MedicationRequestIntent.ORDER);

        Reference visitReference = mock(Reference.class);
        when(visitReference.getReference()).thenReturn("Encounter/123");
        when(encounter.getPartOf()).thenReturn(visitReference);
        when(quotationMapper.toERPNext(encounter)).thenReturn(new com.ozonehis.eip.model.erpnext.Quotation());
        when(customerMapper.toERPNext(patient)).thenReturn(customer);
        when(itemHandler.createQuotationItemIfItemExists(medicationRequest))
                .thenReturn(Optional.of(new com.ozonehis.eip.model.erpnext.QuotationItem()));
        when(quotationHandler.quotationExists(anyString())).thenReturn(false);
        when(patient.getIdPart()).thenReturn("12345");
        when(customerHandler.customerExists(anyString())).thenReturn(false);

        // Act
        medicationRequestProcessor.process(exchange);

        // Verify
        verify(customerHandler, times(1)).ensureCustomerExistsAndUpdate(any(), any());
        verify(quotationHandler, times(1)).quotationExists(anyString());
        verify(quotationHandler, times(1)).sendQuotation(any(), anyString(), any());
        verify(itemHandler, times(1)).createQuotationItemIfItemExists(medicationRequest);
        verify(quotationMapper, times(1)).toERPNext(encounter);
        verify(customerMapper, times(1)).toERPNext(patient);
    }

    @Test
    @DisplayName("Should throw exception when event type is not found.")
    void shouldThrowExceptionWhenEventTypeIsNotFound() {
        // setup
        Bundle bundle = mock(Bundle.class);

        when(bundle.getEntry())
                .thenReturn(List.of(
                        new Bundle.BundleEntryComponent().setResource(new Patient()),
                        new Bundle.BundleEntryComponent().setResource(new Encounter()),
                        new Bundle.BundleEntryComponent().setResource(new Medication()),
                        new Bundle.BundleEntryComponent().setResource(new MedicationRequest())));

        Exchange exchange = createExchange(bundle, null);

        // Act and Assert
        assertThrows(CamelExecutionException.class, () -> medicationRequestProcessor.process(exchange));
    }
}
