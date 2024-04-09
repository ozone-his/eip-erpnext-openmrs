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
            mapGender(ERPNextGender.valueOf(erpnextDocument.getGender())).ifPresent(patient::setGender);
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
