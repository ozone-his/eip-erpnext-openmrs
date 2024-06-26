/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotationItem implements ERPNextDocument {

    @JsonProperty("custom_external_id")
    private String customExternalID;

    @JsonProperty("item_code")
    private String itemCode;

    @JsonProperty("qty")
    private float quantity;

    @JsonProperty("uom")
    private String unitOfMeasure;

    @JsonProperty("description")
    private String description;

    @JsonProperty("additional_notes")
    private String notes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuotationItem quotationItem = (QuotationItem) o;
        return Objects.equals(customExternalID, quotationItem.customExternalID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(customExternalID);
    }
}
