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
import com.ozonehis.eip.erpnext.openmrs.Constants;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import com.ozonehis.eip.model.erpnext.Quotation;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Setter
@Component
public class QuotationHandler {

    @Autowired
    private FrappeClient frappeClient;

    /**
     * Get a quotation by ID
     * @param quotationId the ID of the quotation
     * @return the quotation if it exists, empty otherwise
     */
    public Optional<Quotation> getQuotation(String quotationId) {
        try (FrappeResponse response =
                frappeClient.get("Quotation", quotationId).execute()) {
            return switch (response.code()) {
                case HttpStatus.SC_NOT_FOUND -> Optional.empty();
                case HttpStatus.SC_OK -> {
                    TypeReference<FrappeSingularDataWrapper<Quotation>> typeReference = new TypeReference<>() {};
                    FrappeSingularDataWrapper<Quotation> quotationWrapper = response.returnAs(typeReference);
                    yield Optional.ofNullable(quotationWrapper.getData());
                }
                default -> throw new FrappeClientException("Error while fetching quotation with ID " + quotationId);
            };
        } catch (FrappeClientException | IOException e) {
            throw new FrappeClientException("Error while fetching quotation with ID " + quotationId, e);
        }
    }

    public void sendQuotation(ProducerTemplate producerTemplate, String endpointUri, Quotation quotation) {
        var quotationHeaders = new HashMap<String, Object>();
        quotationHeaders.put(Constants.HEADER_FRAPPE_DOCTYPE, "Quotation");
        quotationHeaders.put(Constants.HEADER_FRAPPE_RESOURCE, quotation);
        quotationHeaders.put(Constants.HEADER_FRAPPE_NAME, quotation.getQuotationId());

        producerTemplate.sendBodyAndHeaders(endpointUri, quotation, quotationHeaders);
    }
}
