package com.ozonehis.eip.mappers.erpnext;

import com.ozonehis.eip.mappers.ToERPNextMapping;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.CustomerType;
import com.ozonehis.eip.model.erpnext.ERPNextGender;
import java.util.Optional;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper implements ToERPNextMapping<Patient, Customer> {

    @Override
    public Customer toERPNext(Patient patient) {
        if (patient == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setCustomerId(patient.getIdPart());
        if (patient.hasGender()) {
            mapGender(patient.getGender()).ifPresent(erpNextGender -> customer.setGender(erpNextGender.getValue()));
        }
        String patientName = getPatientName(patient).orElse("");
        String patientIdentifier = getPreferredPatientIdentifier(patient).orElse("");

        customer.setCustomerName(patientName + " - " + patientIdentifier);
        customer.setCustomerType(CustomerType.INDIVIDUAL.getValue());

        return customer;
    }

    protected Optional<ERPNextGender> mapGender(Enumerations.AdministrativeGender gender) {
        switch (gender) {
            case MALE -> {
                return Optional.of(ERPNextGender.MALE);
            }
            case FEMALE -> {
                return Optional.of(ERPNextGender.FEMALE);
            }
            case OTHER -> {
                return Optional.of(ERPNextGender.OTHER);
            }
            default -> {
                return Optional.empty();
            }
        }
    }

    protected Optional<String> getPreferredPatientIdentifier(Patient patient) {
        return patient.getIdentifier().stream()
                .filter(identifier -> identifier.getUse() == Identifier.IdentifierUse.OFFICIAL)
                .findFirst()
                .map(Identifier::getValue);
    }

    protected Optional<String> getPatientName(Patient patient) {
        return patient.getName().stream()
                .findFirst()
                .map(humanName -> humanName.getFamily() + ", " + humanName.getGivenAsSingleString());
    }
}
