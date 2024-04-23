/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.mappers.fhir;

import com.ozonehis.eip.mappers.ToFhirMapping;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.ERPNextGender;
import java.util.Optional;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper implements ToFhirMapping<Patient, Customer> {

    @Override
    public Patient toFhir(Customer erpnextDocument) {
        Patient patient = new Patient();
        patient.setId(erpnextDocument.getCustomerId()); // This is the patient's ID in the FHIR server
        if (erpnextDocument.getGender() != null) {
            mapGender(erpnextDocument.getGender()).ifPresent(patient::setGender);
        }
        return patient;
    }

    protected Optional<Enumerations.AdministrativeGender> mapGender(ERPNextGender erpNextGender) {
        switch (erpNextGender) {
            case MALE -> {
                return Optional.of(Enumerations.AdministrativeGender.MALE);
            }
            case FEMALE -> {
                return Optional.of(Enumerations.AdministrativeGender.FEMALE);
            }
            case OTHER -> {
                return Optional.of(Enumerations.AdministrativeGender.OTHER);
            }
            default -> {
                return Optional.empty();
            }
        }
    }
}
