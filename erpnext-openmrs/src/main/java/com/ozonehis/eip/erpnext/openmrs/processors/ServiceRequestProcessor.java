/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.processors;

import com.ozonehis.eip.erpnext.openmrs.handlers.CustomerHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.ItemHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationMapper;
import com.ozonehis.eip.model.erpnext.Quotation;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.openmrs.eip.fhir.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
public class ServiceRequestProcessor implements Processor {

    @Autowired
    private QuotationMapper quotationMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private QuotationHandler quotationHandler;

    @Autowired
    private CustomerHandler customerHandler;

    @Autowired
    private ItemHandler itemHandler;

    @Override
    public void process(Exchange exchange) {
        try (ProducerTemplate producerTemplate = exchange.getContext().createProducerTemplate()) {
            Bundle bundle = exchange.getMessage().getBody(Bundle.class);
            List<Bundle.BundleEntryComponent> entries = bundle.getEntry();

            Patient patient = null;
            Encounter encounter = null;
            ServiceRequest serviceRequest = null;
            for (Bundle.BundleEntryComponent entry : entries) {
                Resource resource = entry.getResource();
                if (resource instanceof Patient) {
                    patient = (Patient) resource;
                } else if (resource instanceof Encounter) {
                    encounter = (Encounter) resource;
                } else if (resource instanceof ServiceRequest) {
                    serviceRequest = (ServiceRequest) resource;
                }
            }

            if (patient == null || encounter == null || serviceRequest == null) {
                throw new IllegalArgumentException("Patient, Encounter or ServiceRequest not found in the bundle");
            } else {
                log.info("Processing ServiceRequest for patient with UUID {}", patient.getIdPart());
                String eventType = exchange.getMessage().getHeader(Constants.HEADER_FHIR_EVENT_TYPE, String.class);
                if (eventType == null) {
                    throw new IllegalArgumentException("Event type not found in the exchange headers");
                }
                if ("c".equals(eventType) || "u".equals(eventType)) {
                    if (serviceRequest.getStatus().equals(ServiceRequest.ServiceRequestStatus.ACTIVE)
                            && serviceRequest.getIntent().equals(ServiceRequest.ServiceRequestIntent.ORDER)) {
                        var customer = customerMapper.toERPNext(patient);
                        customerHandler.syncCustomerWithPatient(producerTemplate, patient);
                        String encounterVisitUuid =
                                encounter.getPartOf().getReference().split("/")[1];
                        Optional<Quotation> quotationOptional = quotationHandler.getQuotation(encounterVisitUuid);
                        // Quotation quotation = quotationOptional.orElseGet(null);
                        if (quotationOptional.isPresent()) {
                            // Quotation exists, update it with the new item
                            Quotation finalQuotation = quotationOptional.get();
                            this.itemHandler
                                    .createQuotationItemIfItemExists(serviceRequest)
                                    .ifPresent(quotationItem -> {
                                        if (finalQuotation.hasItem(quotationItem)) {
                                            log.debug("Quotation item already exists. Already processed skipping...");
                                        } else {
                                            finalQuotation.addItem(quotationItem);
                                        }
                                    });
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-update-quotation-route", finalQuotation);
                        } else {
                            Quotation quotation = quotationMapper.toERPNext(encounter);
                            quotation.setTitle(customer.getCustomerName());
                            quotation.setCustomer(customer.getCustomerId());
                            quotation.setCustomerName(customer.getCustomerName());
                            this.itemHandler
                                    .createQuotationItemIfItemExists(serviceRequest)
                                    .ifPresent(quotation::addItem);
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-create-quotation-route", quotation);
                        }
                    }
                } else if ("d".equals(eventType)) {
                    String encounterVisitUuid =
                            encounter.getPartOf().getReference().split("/")[1];
                    Optional<Quotation> quotationOptional = quotationHandler.getQuotation(encounterVisitUuid);
                    // If the Quotation exists and still a draft, remove the item
                    if (quotationOptional.isPresent()
                            && !quotationOptional.get().isSubmitted()) {
                        this.itemHandler
                                .createQuotationItemIfItemExists(serviceRequest)
                                .ifPresent(quotationItem -> {
                                    if (quotationOptional.get().hasItem(quotationItem)) {
                                        quotationOptional.get().removeItem(quotationItem);
                                    }
                                });
                        // If the quotation has no items, delete it
                        if (quotationOptional.get().getItems().isEmpty()) {
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-delete-quotation-route", quotationOptional.get());
                        } else {
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-update-quotation-route", quotationOptional.get());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported event type: " + eventType);
                }
            }
        } catch (IOException e) {
            throw new CamelExecutionException("Error processing ServiceRequest", exchange, e);
        }
    }
}
