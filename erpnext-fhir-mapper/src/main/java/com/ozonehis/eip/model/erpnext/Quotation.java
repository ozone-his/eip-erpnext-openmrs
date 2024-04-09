/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Quotation implements ERPNextDocument {

    @Nonnull
    @JsonProperty("name")
    private String quotationId;

    @Nonnull
    @JsonProperty("quotation_to")
    private String quotationTo;

    @Nonnull
    @JsonProperty("order_type")
    private String orderType;

    @JsonProperty("title")
    private String title;

    @Nonnull
    @JsonProperty("party_name")
    private String customer;

    @Nonnull
    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("docstatus")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private boolean isSubmitted;

    @Nonnull
    @JsonProperty("items")
    private Set<QuotationItem> items = new HashSet<>();

    public void addItems(HashSet<QuotationItem> quotationItems) {
        if (items == null) {
            items = new HashSet<>();
        }
        items.addAll(quotationItems);
    }

    public void addItem(QuotationItem quotationItem) {
        if (items == null) {
            items = new HashSet<>();
        }
        items.add(quotationItem);
    }

    public void removeItem(QuotationItem quotationItem) {
        if (items == null) {
            items = new HashSet<>();
        }
        items.removeIf(item -> item.getItemCode().equals(quotationItem.getItemCode()));
    }

    public boolean hasItem(QuotationItem quotationItem) {
        if (items == null) {
            items = new HashSet<>();
        }
        return items.stream().anyMatch(item -> item.getItemCode().equals(quotationItem.getItemCode()));
    }

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }
}
