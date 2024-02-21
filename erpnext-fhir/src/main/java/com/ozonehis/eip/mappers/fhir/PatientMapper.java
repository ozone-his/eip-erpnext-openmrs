package com.ozonehis.eip.mappers.fhir;

import com.ozonehis.eip.mappers.ToFhirMapping;
import com.ozonehis.eip.model.erpnext.Customer;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper implements ToFhirMapping<Patient, Customer> {

    @Override
    public Patient toFhir(Customer erpnextDocument) {
        Patient patient = new Patient();
        patient.setId(erpnextDocument.getCustomerId());
        patient.addName()
                .setFamily(erpnextDocument.getLastName())
                .addGiven(erpnextDocument.getFirstName())
                .addGiven(erpnextDocument.getMiddleName());

        return patient;
    }
}
