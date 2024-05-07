/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.processors;

import static com.ozonehis.eip.erpnext.openmrs.Constants.EXCHANGE_PROPERTY_SKIP_ENCOUNTER;

import com.ozonehis.eip.erpnext.openmrs.Constants;
import com.ozonehis.eip.erpnext.openmrs.handlers.QuotationHandler;
import com.ozonehis.eip.model.erpnext.Quotation;
import java.util.HashMap;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EncounterProcessor implements Processor {

    @Autowired
    private QuotationHandler quotationHandler;

    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getMessage();
        Encounter encounter = message.getBody(Encounter.class);
        if (encounter != null && encounter.hasPeriod() && encounter.getPeriod().hasEnd()) {
            Quotation quotation = quotationHandler.getQuotation(encounter.getIdPart());
            if (quotation != null) {
                quotation.setSubmitted(true);

                var headers = new HashMap<String, Object>();
                headers.put(Constants.HEADER_FRAPPE_DOCTYPE, "Quotation");
                headers.put(Constants.HEADER_FRAPPE_RESOURCE, quotation);
                headers.put(Constants.HEADER_FRAPPE_NAME, quotation.getQuotationId());

                exchange.getMessage().setHeaders(headers);
                exchange.getMessage().setBody(quotation);
                exchange.setProperty(EXCHANGE_PROPERTY_SKIP_ENCOUNTER, false);
            } else {
                exchange.setProperty(EXCHANGE_PROPERTY_SKIP_ENCOUNTER, true);
            }
        } else {
            // skipping the processing of the encounter
            exchange.setProperty(EXCHANGE_PROPERTY_SKIP_ENCOUNTER, true);
        }
    }
}
