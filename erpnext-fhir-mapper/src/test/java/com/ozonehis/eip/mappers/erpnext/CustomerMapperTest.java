/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.mappers.erpnext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.CustomerType;
import com.ozonehis.eip.model.erpnext.ERPNextGender;
import java.util.Collections;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerMapperTest {

    private CustomerMapper customerMapper;

    @BeforeEach
    void setUp() {
        customerMapper = new CustomerMapper();
    }

    @Test
    @DisplayName("Should map patient to customer")
    void shouldMapPatientToCustomer() {
        Patient patient = new Patient();
        patient.setId("123");
        patient.setGender(Enumerations.AdministrativeGender.MALE);
        patient.setName(
                Collections.singletonList(new HumanName().setFamily("Doe").addGiven("John")));
        patient.setIdentifier(Collections.singletonList(
                new Identifier().setUse(Identifier.IdentifierUse.OFFICIAL).setValue("ID123")));

        Customer customer = customerMapper.toERPNext(patient);

        assertEquals("123", customer.getCustomerId());
        assertEquals(ERPNextGender.MALE, customer.getGender());
        assertEquals("Doe, John - ID123", customer.getCustomerName());
        assertEquals(CustomerType.INDIVIDUAL, customer.getCustomerType());
    }

    @Test
    @DisplayName("Should return null when patient is null")
    void shouldReturnNullWhenPatientIsNull() {
        assertNull(customerMapper.toERPNext(null));
    }

    @Test
    @DisplayName("Should return customer without gender when patient gender is not set.")
    void shouldHandlePatientWithoutGender() {
        Patient patient = new Patient();
        patient.setId("123");
        patient.setName(
                Collections.singletonList(new HumanName().setFamily("Doe").addGiven("John")));
        patient.setIdentifier(Collections.singletonList(
                new Identifier().setUse(Identifier.IdentifierUse.OFFICIAL).setValue("ID123")));

        Customer customer = customerMapper.toERPNext(patient);

        assertNull(customer.getGender());
    }

    @Test
    @DisplayName("Should return customer with identifier when patient preferred identifier is set.")
    void shouldAddSuffixPreferredIdentifierWhenPatientHasIdentifierForOfficialUse() {
        Patient patient = new Patient();
        patient.setId("123");
        patient.setGender(Enumerations.AdministrativeGender.MALE);
        patient.setIdentifier(Collections.singletonList(
                new Identifier().setUse(Identifier.IdentifierUse.OFFICIAL).setValue("ID123")));

        Customer customer = customerMapper.toERPNext(patient);

        assertEquals(" - ID123", customer.getCustomerName());
    }
}
