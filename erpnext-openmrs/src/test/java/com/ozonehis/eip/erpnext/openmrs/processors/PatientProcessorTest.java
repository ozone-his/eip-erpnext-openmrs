package com.ozonehis.eip.erpnext.openmrs.processors;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_RESOURCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.ozonehis.eip.erpnext.openmrs.handlers.AddressHandler;
import com.ozonehis.eip.mappers.erpnext.AddressMapper;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.model.erpnext.Customer;
import java.util.Collections;
import org.apache.camel.Exchange;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class PatientProcessorTest extends BaseProcessorTest {

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private AddressMapper addressMapper;

    @Mock
    private AddressHandler addressHandler;

    @InjectMocks
    private PatientProcessor patientProcessor;

    private static AutoCloseable mocksCloser;

    private static final String ADDRESS_ID = "12377e18-a051-487b-8dd3-4cffcddb2a9c";

    private static final String PATIENT_ID = "866f25bf-d930-4886-9332-75443047e38e";

    @BeforeEach
    void setup() {
        mocksCloser = openMocks(this);
    }

    @AfterAll
    static void close() throws Exception {
        mocksCloser.close();
    }

    @Test
    @DisplayName("Should process patient with address and create event type.")
    void shouldProcessPatientWithAddressAndCreateEventType() {
        // Arrange
        Patient patient = new Patient();
        Address address = new Address();
        Customer customer = new Customer();
        com.ozonehis.eip.model.erpnext.Address erpNextAddress = new com.ozonehis.eip.model.erpnext.Address();
        address.setId(ADDRESS_ID);
        address.setUse(Address.AddressUse.HOME);
        patient.setAddress(Collections.singletonList(address));
        Exchange exchange = createExchange(patient, "c");

        // Mock behavior
        when(customerMapper.toERPNext(patient)).thenReturn(customer);
        when(addressMapper.toERPNext(address)).thenReturn(erpNextAddress);
        when(addressHandler.addressExists(ADDRESS_ID)).thenReturn(true);

        // Act
        patientProcessor.process(exchange);

        // Assert
        assertEquals(exchange.getMessage().getHeader(HEADER_FHIR_EVENT_TYPE), "c");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_DOCTYPE), "Customer");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_RESOURCE), customer);

        assertEquals(exchange.getProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS), erpNextAddress);

        verify(addressHandler, times(1)).addressExists(ADDRESS_ID);
        verify(addressMapper, times(1)).toERPNext(address);
        verify(customerMapper, times(1)).toERPNext(patient);
    }

    @Test
    @DisplayName("Should process patient with update event type.")
    void shouldProcessPatientWithUpdateEventType() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);

        Customer customer = new Customer();
        customer.setCustomerId(PATIENT_ID);

        Exchange exchange = createExchange(patient, "u");

        // Mock behavior
        when(customerMapper.toERPNext(patient)).thenReturn(customer);

        // Act
        patientProcessor.process(exchange);

        // Assert
        assertEquals(exchange.getMessage().getHeader(HEADER_FHIR_EVENT_TYPE), "u");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_DOCTYPE), "Customer");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_RESOURCE), customer);
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_NAME), customer.getCustomerId());

        verify(customerMapper, times(1)).toERPNext(patient);
        verify(addressHandler, times(0)).addressExists(ADDRESS_ID);
    }

    @Test
    @DisplayName("Should process patient with delete event type.")
    void shouldProcessPatientWithDeleteEventType() {
        // Arrange
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);

        Customer customer = new Customer();
        customer.setCustomerId(PATIENT_ID);

        Exchange exchange = createExchange(patient, "d");

        // Mock behavior
        when(customerMapper.toERPNext(patient)).thenReturn(customer);

        // Act
        patientProcessor.process(exchange);

        // Assert
        assertEquals(exchange.getMessage().getHeader(HEADER_FHIR_EVENT_TYPE), "d");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_DOCTYPE), "Customer");
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_RESOURCE), customer);
        assertEquals(exchange.getMessage().getHeader(HEADER_FRAPPE_NAME), customer.getCustomerId());

        verify(customerMapper, times(1)).toERPNext(patient);
        verify(addressHandler, times(0)).addressExists(ADDRESS_ID);
    }
}
