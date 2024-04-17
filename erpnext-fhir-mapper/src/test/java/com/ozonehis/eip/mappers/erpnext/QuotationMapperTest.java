/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.mappers.erpnext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ozonehis.eip.model.erpnext.OrderType;
import com.ozonehis.eip.model.erpnext.Quotation;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuotationMapperTest {

    private QuotationMapper quotationMapper;

    @BeforeEach
    void setUp() {
        quotationMapper = new QuotationMapper();
    }

    @Test
    @DisplayName("Should map encounter partOf(Visit) to quotation")
    void shouldMapEncounterToQuotation() {
        // setup
        Encounter encounter = new Encounter();
        encounter.setPartOf(new Reference("Encounter/1234"));

        // Act
        Quotation quotation = quotationMapper.toERPNext(encounter);

        // verify
        assertNotNull(quotation);
        assertEquals("1234", quotation.getQuotationId());
        assertEquals(OrderType.SALES, quotation.getOrderType());
        assertEquals("Customer", quotation.getQuotationTo());
    }

    @Test
    @DisplayName("Should return null when encounter is null")
    void shouldReturnNullWhenEncounterIsNull() {
        // Act
        Quotation quotation = quotationMapper.toERPNext(null);

        // verify
        assertNull(quotation);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when encounter partOf reference is null")
    void shouldThrowIllegalArgumentExceptionWhenEncounterPartOfReferenceIsNull() {
        // setup
        Encounter encounter = new Encounter();

        // Act and verify
        assertThrows(IllegalArgumentException.class, () -> quotationMapper.toERPNext(encounter));
    }
}
