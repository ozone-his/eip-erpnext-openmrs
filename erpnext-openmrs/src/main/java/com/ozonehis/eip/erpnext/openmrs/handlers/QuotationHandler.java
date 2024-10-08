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
     * Check if quotation exists
     *
     * @param name the name of the quotation document
     * @return true if quotation exists, false otherwise
     */
    public boolean quotationExists(String name) {
        try (FrappeResponse response = frappeClient.get("Quotation", name).execute()) {
            if (response.code() == HttpStatus.SC_OK) {
                TypeReference<FrappeSingularDataWrapper<Quotation>> typeReference = new TypeReference<>() {};

                FrappeSingularDataWrapper<Quotation> customerWrapper = response.returnAs(typeReference);
                Quotation quotation = customerWrapper.getData();
                return quotation.getQuotationId().equals(name);
            } else {
                return false;
            }
        } catch (FrappeClientException | IOException e) {
            log.error("Error while checking if quotation exists", e);
            return false;
        }
    }

    public Quotation getQuotation(String quotationId) {
        try (FrappeResponse response =
                frappeClient.get("Quotation", quotationId).execute()) {
            if (response.code() == HttpStatus.SC_OK) {
                TypeReference<FrappeSingularDataWrapper<Quotation>> typeReference = new TypeReference<>() {};

                FrappeSingularDataWrapper<Quotation> quotationWrapper = response.returnAs(typeReference);
                return quotationWrapper.getData();
            } else {
                log.debug("Quotation with UUID: {} not found", quotationId);
                return null;
            }
        } catch (FrappeClientException | IOException e) {
            log.error("Error while fetching quotation with ID {}", quotationId, e);
            return null;
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
