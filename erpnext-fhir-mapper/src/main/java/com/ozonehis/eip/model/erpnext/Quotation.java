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
    private OrderType orderType;

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

    /**
     * Add items to the quotation
     *
     * @param quotationItems the items to add
     */
    public void addItems(HashSet<QuotationItem> quotationItems) {
        items.addAll(quotationItems);
    }

    /**
     * Add an item to the quotation
     *
     * @param quotationItem the item to add
     */
    public void addItem(QuotationItem quotationItem) {
        items.add(quotationItem);
    }

    /**
     * Remove an item from the quotation
     *
     * @param quotationItem the item to remove
     */
    public void removeItem(QuotationItem quotationItem) {
        items.removeIf(item -> item.getCustomExternalID().equals(quotationItem.getCustomExternalID()));
    }

    /**
     * Remove an item from the quotation
     *
     * @param customExternalId the ID of the item to remove
     */
    public void removeItem(String customExternalId) {
        items.removeIf(item -> item.getCustomExternalID().equals(customExternalId));
    }

    /**
     * Check if the quotation has the given item
     *
     * @param quotationItem the item to check
     * @return true if the item exists, false otherwise
     */
    public boolean hasItem(QuotationItem quotationItem) {
        return items.stream().anyMatch(item -> item.getCustomExternalID().equals(quotationItem.getCustomExternalID()));
    }

    /**
     * Check if the quotation has items
     *
     * @return true if the quotation has items, false otherwise
     */
    public boolean hasItems() {
        return !items.isEmpty();
    }
}
