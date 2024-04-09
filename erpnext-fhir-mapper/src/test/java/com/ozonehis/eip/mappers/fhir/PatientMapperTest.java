package com.ozonehis.eip.mappers.fhir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.ERPNextGender;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PatientMapperTest {

    private PatientMapper patientMapper;

    @BeforeEach
    public void setup() {
        patientMapper = new PatientMapper();
    }

    @Test
    @DisplayName("Should map MALE gender correctly.")
    public void shouldMapMaleGenderCorrectly() {
        Customer customer = new Customer();
        customer.setGender(ERPNextGender.MALE.name());

        Patient patient = patientMapper.toFhir(customer);

        assertEquals(Enumerations.AdministrativeGender.MALE, patient.getGender());
    }

    @Test
    @DisplayName("Should map FEMALE gender correctly.")
    public void shouldMapFemaleGenderCorrectly() {
        Customer customer = new Customer();
        customer.setGender(ERPNextGender.FEMALE.name());

        Patient patient = patientMapper.toFhir(customer);

        assertEquals(Enumerations.AdministrativeGender.FEMALE, patient.getGender());
    }

    @Test
    @DisplayName("Should map OTHER gender correctly.")
    public void shouldMapOtherGenderCorrectly() {
        Customer customer = new Customer();
        customer.setGender(ERPNextGender.OTHER.name());

        Patient patient = patientMapper.toFhir(customer);

        assertEquals(Enumerations.AdministrativeGender.OTHER, patient.getGender());
    }

    @Test
    @DisplayName("Should map customerId to patientId.")
    public void shouldMapCustomerIdToPatientId() {
        Customer customer = new Customer();
        customer.setCustomerId("123");

        Patient patient = patientMapper.toFhir(customer);

        assertEquals("123", patient.getId());
    }
}
