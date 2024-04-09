/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.processors;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS;
import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_DOCTYPE;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_NAME;
import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_FRAPPE_RESOURCE;
import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import com.ozonehis.eip.erpnext.openmrs.handlers.AddressHandler;
import com.ozonehis.eip.mappers.erpnext.AddressMapper;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.model.erpnext.Address;
import com.ozonehis.eip.model.erpnext.Customer;
import com.ozonehis.eip.model.erpnext.Link;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientProcessor implements Processor {

    @Autowired
    private CustomerMapper mapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private AddressHandler addressHandler;

    @Override
    public void process(Exchange exchange) {
        Message message = exchange.getMessage();
        Patient patient = message.getBody(Patient.class);
        Customer customer = mapper.toERPNext(patient);

        var headers = message.getHeaders();
        headers.put(HEADER_FRAPPE_RESOURCE, customer);
        headers.put(HEADER_FRAPPE_DOCTYPE, "Customer");

        String eventType = message.getHeader(HEADER_FHIR_EVENT_TYPE, String.class);
        if ("u".equals(eventType) || "d".equals(eventType)) {
            headers.put(HEADER_FRAPPE_NAME, customer.getCustomerId());
        }

        if (patient.hasAddress()) {
            patient.getAddress().stream()
                    .filter(a -> a.getUse() == org.hl7.fhir.r4.model.Address.AddressUse.HOME)
                    .findFirst()
                    .ifPresent(address -> {
                        addressHandler.addressExists(address.getIdElement().getValue());
                        Address erpNextAddress = addressMapper.toERPNext(address);
                        if (patient.hasTelecom()) {
                            erpNextAddress.setPhone(patient.getTelecomFirstRep().getValue());
                        }
                        erpNextAddress.setAddressTitle(patient.getNameFirstRep().getNameAsSingleString()
                                + " Home's Address"); // TODO: confirm this
                        Set<Link> links = this.createLinks(erpNextAddress.getAddressName(), patient.getIdPart());
                        erpNextAddress.setLinks(links);

                        exchange.setProperty(EXCHANGE_PROPERTY_ERPNEXT_ADDRESS, erpNextAddress);
                        if (eventType.equals("u") || eventType.equals("d")) {
                            exchange.setProperty(
                                    EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME, erpNextAddress.getAddressName());
                        }
                    });

            /*
             * When updating a patient, we need to ensure that only the home address is enabled in ERPNext.
             * Remove addresses that are not the home address.
             */
            if (patient.hasAddress()) {
                patient.getAddress().stream()
                        .filter(a -> a.getUse() != org.hl7.fhir.r4.model.Address.AddressUse.HOME)
                        .forEach(address -> addressHandler.disableOldAddresses(
                                exchange.getContext().createProducerTemplate(), addressMapper.toERPNext(address)));
            }
        }
        exchange.getMessage().setHeaders(headers);
        exchange.getMessage().setBody(customer);
    }

    protected Set<Link> createLinks(String addressName, String customerId) {
        Link link = new Link();
        link.setLinkName(customerId);
        link.setLinkDoctype("Customer");
        link.setLinkTitle(addressName);

        return new HashSet<>(List.of(link));
    }
}
