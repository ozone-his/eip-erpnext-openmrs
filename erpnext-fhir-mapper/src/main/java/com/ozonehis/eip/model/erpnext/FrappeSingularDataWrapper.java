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
import lombok.Getter;
import lombok.Setter;

/**
 * A Wrapper for Frappé Singular Response
 *
 * @param <T> Wrapped Data
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrappeSingularDataWrapper<T> {

    @JsonProperty("data")
    private T data;
}
