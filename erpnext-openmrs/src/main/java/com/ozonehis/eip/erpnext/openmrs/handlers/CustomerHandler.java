/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.handlers;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_ENABLE_PATIENT_SYNC;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import com.ozonehis.camel.frappe.sdk.api.FrappeClientException;
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.hc.core5.http.HttpStatus;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
public class CustomerHandler {

    @Autowired
    private FrappeClient frappeClient;

    /**
     *  Get a customer by name(patient uuid)
     *  @param name the name of the customer
     *  @return the customer if it exists, empty otherwise
     */
    public Optional<Customer> getCustomer(String name) {
        try (FrappeResponse response =
                frappeClient.get("Customer", name).withField("name").execute()) {
            return switch (response.code()) {
                case HttpStatus.SC_NOT_FOUND -> Optional.empty();
                case HttpStatus.SC_OK -> {
                    TypeReference<FrappeSingularDataWrapper<Customer>> typeReference = new TypeReference<>() {};

                    FrappeSingularDataWrapper<Customer> customerWrapper = response.returnAs(typeReference);
                    yield Optional.of(customerWrapper.getData());
                }
                default -> throw new FrappeClientException("Error while fetching customer with name " + name
                        + " with error message: " + response.message());
            };
        } catch (FrappeClientException | IOException e) {
            throw new FrappeClientException("Error while fetching customer", e);
        }
    }

    public void syncCustomerWithPatient(ProducerTemplate producerTemplate, Patient patient) {
        if (getCustomer(patient.getIdPart()).isPresent()) {
            log.info("Customer with UUID {} already exists, updating...", patient.getIdPart());
            var headers = new HashMap<String, Object>();
            headers.put(HEADER_FRAPPE_DOCTYPE, "Customer");
            headers.put(HEADER_FHIR_EVENT_TYPE, "u");
            headers.put(HEADER_ENABLE_PATIENT_SYNC, true);
            producerTemplate.sendBodyAndHeaders("direct:patient-to-customer-router", patient, headers);
        } else {
            log.info("Customer with UUID {} does not exist, creating...", patient.getIdPart());
            var headers = new HashMap<String, Object>();
            headers.put(HEADER_FRAPPE_DOCTYPE, "Customer");
            headers.put(HEADER_FHIR_EVENT_TYPE, "c");
            headers.put(HEADER_ENABLE_PATIENT_SYNC, true);
            producerTemplate.sendBodyAndHeaders("direct:patient-to-customer-router", patient, headers);
        }
    }
}
