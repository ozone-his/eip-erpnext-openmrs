/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import com.ozonehis.camel.frappe.sdk.api.FrappeClientException;
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.eip.mappers.erpnext.QuotationItemMapper;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import com.ozonehis.eip.model.erpnext.Item;
import com.ozonehis.eip.model.erpnext.QuotationItem;
import java.io.IOException;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
public class ItemHandler {

    @Autowired
    private FrappeClient frappeClient;

    @Autowired
    private QuotationItemMapper<Resource> quotationItemMapper;

    public Optional<Item> getItem(String name) {
        try (FrappeResponse response = frappeClient.get("Item", name).execute()) {
            return switch (response.code()) {
                case HttpStatus.SC_NOT_FOUND -> Optional.empty();
                case HttpStatus.SC_OK -> {
                    TypeReference<FrappeSingularDataWrapper<Item>> typeReference = new TypeReference<>() {};
                    yield Optional.ofNullable(response.returnAs(typeReference).getData());
                }
                default -> throw new FrappeClientException(
                        "Error while checking if Item with uuid: " + name + " exists" + response.message());
            };
        } catch (FrappeClientException | IOException e) {
            throw new FrappeClientException("Error while checking if Item exists", e);
        }
    }

    public Optional<QuotationItem> createQuotationItemIfItemExists(Resource resource) {
        return getItemName(resource).flatMap(this::getItem).map(item -> quotationItemMapper.toERPNext(resource));
    }

    private Optional<String> getItemName(Resource resource) {
        if (resource instanceof ServiceRequest serviceRequest) {
            return Optional.of(serviceRequest.getCode().getCodingFirstRep().getCode());
        } else if (resource instanceof MedicationRequest medicationRequest) {
            return Optional.of(
                    medicationRequest.getMedicationReference().getReference().split("/")[1]);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported resource type: " + resource.getClass().getName());
        }
    }
}
