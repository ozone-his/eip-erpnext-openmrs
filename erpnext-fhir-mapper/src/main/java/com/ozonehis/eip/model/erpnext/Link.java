/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {

    @JsonProperty("name")
    private String name;

    @JsonProperty("link_doctype")
    private String linkDoctype;

    @JsonProperty("link_name")
    private String linkName;

    @JsonProperty("link_title")
    private String linkTitle;

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("parenttype")
    private String parentType;

    @JsonProperty("parentfield")
    private String parentField;

    @JsonProperty("doctype")
    private String doctype = "Dynamic Link";
}
