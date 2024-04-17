/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CustomerType {
    INDIVIDUAL("Individual"),
    COMPANY("Company"),
    PARTNERSHIP("Partnership"),
    SOLE_PROPRIETORSHIP("Sole Proprietorship");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}
