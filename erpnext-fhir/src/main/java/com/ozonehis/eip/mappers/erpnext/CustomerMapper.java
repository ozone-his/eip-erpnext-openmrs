package com.ozonehis.eip.mappers.erpnext;

import com.ozonehis.eip.mappers.ToERPNextMapping;
import com.ozonehis.eip.model.erpnext.Customer;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper implements ToERPNextMapping<Patient, Customer> {

    @Override
    public Customer toERPNext(Patient fhirResource) {
        Customer customer = new Customer();
        customer.setCustomerId(fhirResource.getId());
        customer.setFirstName(fhirResource.getNameFirstRep().getGivenAsSingleString());
        customer.setMiddleName(fhirResource.getNameFirstRep().getGivenAsSingleString());
        customer.setLastName(fhirResource.getNameFirstRep().getFamily());

        return customer;
    }
}
