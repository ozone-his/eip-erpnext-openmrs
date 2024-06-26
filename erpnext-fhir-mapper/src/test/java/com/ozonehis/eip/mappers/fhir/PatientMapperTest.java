/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
        customer.setGender(ERPNextGender.MALE);

        Patient patient = patientMapper.toFhir(customer);

        assertEquals(Enumerations.AdministrativeGender.MALE, patient.getGender());
    }

    @Test
    @DisplayName("Should map FEMALE gender correctly.")
    public void shouldMapFemaleGenderCorrectly() {
        Customer customer = new Customer();
        customer.setGender(ERPNextGender.FEMALE);

        Patient patient = patientMapper.toFhir(customer);

        assertEquals(Enumerations.AdministrativeGender.FEMALE, patient.getGender());
    }

    @Test
    @DisplayName("Should map OTHER gender correctly.")
    public void shouldMapOtherGenderCorrectly() {
        Customer customer = new Customer();
        customer.setGender(ERPNextGender.OTHER);

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
