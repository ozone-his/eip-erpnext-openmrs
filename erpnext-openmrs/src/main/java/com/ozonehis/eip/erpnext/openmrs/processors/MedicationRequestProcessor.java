/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.processors;

import static com.ozonehis.eip.erpnext.openmrs.Constants.HEADER_EVENT_PROCESSED;

import com.ozonehis.eip.erpnext.openmrs.handlers.CustomerHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.ItemHandler;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.mappers.erpnext.CustomerMapper;
import com.ozonehis.eip.mappers.erpnext.QuotationMapper;
import com.ozonehis.eip.model.erpnext.Quotation;
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
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.openmrs.eip.fhir.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
public class MedicationRequestProcessor implements Processor {

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
            MedicationRequest medicationRequest = null;
            Medication medication = null;

            for (Bundle.BundleEntryComponent entry : entries) {
                Resource resource = entry.getResource();
                if (resource instanceof Patient) {
                    patient = (Patient) resource;
                } else if (resource instanceof Encounter) {
                    encounter = (Encounter) resource;
                } else if (resource instanceof MedicationRequest) {
                    medicationRequest = (MedicationRequest) resource;
                } else if (resource instanceof Medication) {
                    medication = (Medication) resource;
                }
            }

            if (patient == null || encounter == null || medicationRequest == null || medication == null) {
                throw new CamelExecutionException(
                        "Invalid Bundle. Bundle must contain Patient, Encounter, MedicationRequest and Medication",
                        exchange);
            } else {
                log.debug("Processing MedicationRequest for Patient with UUID {}", patient.getIdPart());
                String eventType = exchange.getMessage().getHeader(Constants.HEADER_FHIR_EVENT_TYPE, String.class);
                if (eventType == null) {
                    throw new IllegalArgumentException("Event type not found in the exchange headers.");
                }
                String encounterVisitUuid = encounter.getPartOf().getReference().split("/")[1];
                if ("c".equals(eventType) || "u".equals(eventType)) {
                    customerHandler.syncCustomerWithPatient(producerTemplate, patient);
                    var customer = customerMapper.toERPNext(patient);
                    // If the MedicationRequest is canceled, remove the item from the quotation
                    if (medicationRequest.getStatus().equals(MedicationRequest.MedicationRequestStatus.CANCELLED)) {
                        handleQuotationWithItems(encounterVisitUuid, medicationRequest, exchange, producerTemplate);
                    } else {
                        Optional<Quotation> quotationOptional = quotationHandler.getQuotation(encounterVisitUuid);
                        if (quotationOptional.isPresent()) {
                            // If the quotation exists, update it
                            Quotation quotation = quotationOptional.get();
                            Medication finalMedication = medication;
                            itemHandler
                                    .createQuotationItemIfItemExists(medicationRequest)
                                    .ifPresent(item -> {
                                        if (quotation.hasItem(item)) {
                                            quotation.removeItem(item);
                                        }
                                        if (item.getItemCode() == null) {
                                            item.setItemCode(finalMedication.getIdPart());
                                        }
                                        quotation.addItem(item);
                                    });
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-update-quotation-route", quotation);
                        } else {
                            // If the quotation does not exist, create it
                            Quotation newQuotation = quotationMapper.toERPNext(encounter);
                            newQuotation.setTitle(customer.getCustomerName());
                            newQuotation.setCustomer(customer.getCustomerId());
                            newQuotation.setCustomerName(customer.getCustomerName());
                            itemHandler
                                    .createQuotationItemIfItemExists(medicationRequest)
                                    .ifPresent(newQuotation::addItem);
                            quotationHandler.sendQuotation(
                                    producerTemplate, "direct:erpnext-create-quotation-route", newQuotation);
                        }
                    }
                } else if ("d".equals(eventType)) {
                    handleQuotationWithItems(encounterVisitUuid, medicationRequest, exchange, producerTemplate);
                } else {
                    throw new IllegalArgumentException("Unsupported event type: " + eventType);
                }
            }
        } catch (Exception e) {
            throw new CamelExecutionException("Error processing MedicationRequest", exchange, e);
        }
    }

    private void handleQuotationWithItems(
            String encounterVisitUuid,
            MedicationRequest medicationRequest,
            Exchange exchange,
            ProducerTemplate producerTemplate) {
        Optional<Quotation> quotationOptional = quotationHandler.getQuotation(encounterVisitUuid);
        if (quotationOptional.isPresent()) {
            Quotation quotation = quotationOptional.get();
            log.debug("Removing item from quotation with ID {}", medicationRequest.getIdPart());
            quotation.removeItem(medicationRequest.getIdPart());

            String route = quotation.hasItems()
                    ? "direct:erpnext-update-quotation-route"
                    : "direct:erpnext-delete-quotation-route";
            quotationHandler.sendQuotation(producerTemplate, route, quotation);
        } else {
            log.debug("Quotation with ID {} already deleted", encounterVisitUuid);
            exchange.getMessage().setHeader(HEADER_EVENT_PROCESSED, true);
        }
    }
}
