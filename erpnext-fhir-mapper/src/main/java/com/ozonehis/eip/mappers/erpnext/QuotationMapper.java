/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.mappers.erpnext;

import com.ozonehis.eip.mappers.ToERPNextMapping;
import com.ozonehis.eip.model.erpnext.OrderType;
import com.ozonehis.eip.model.erpnext.Quotation;
import org.hl7.fhir.r4.model.Encounter;
import org.springframework.stereotype.Component;

@Component
public class QuotationMapper implements ToERPNextMapping<Encounter, Quotation> {

    @Override
    public Quotation toERPNext(Encounter encounter) {
        Quotation quotation = new Quotation();
        if (encounter == null) {
            return null;
        }
        if (encounter.hasPartOf()) {
            String encounterVisitUuid = encounter.getPartOf().getReference().split("/")[1];
            quotation.setQuotationId(encounterVisitUuid);
            quotation.setOrderType(OrderType.SALES);
            quotation.setQuotationTo("Customer");
        } else {
            throw new IllegalArgumentException(
                    "The Encounter does not have a partOf reference. Cannot map to Quotation.");
        }

        return quotation;
    }
}
