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
import jakarta.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address implements ERPNextDocument {

    @Nonnull
    @JsonProperty("name")
    private String addressName;

    @JsonProperty("address_title")
    private String addressTitle;

    @JsonProperty("address_type")
    private String addressType;

    @JsonProperty("address_line1")
    private String addressLine1;

    @JsonProperty("address_line2")
    private String addressLine2;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("country")
    private String country;

    @JsonProperty("pincode")
    private String postalCode;

    @JsonProperty("email_id")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("disabled")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private boolean disabled;

    @JsonProperty("is_primary_address")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT)
    private boolean primaryAddress;

    @JsonProperty("links")
    private Set<Link> links = new HashSet<>();

    public void addLink(Link link) {
        if (this.links == null) {
            this.links = new HashSet<>();
        }
        this.links.add(link);
    }

    public void removeLink(Link link) {
        links.remove(link);
    }

    public void addLinks(Set<Link> links) {
        this.links.addAll(links);
    }
}
