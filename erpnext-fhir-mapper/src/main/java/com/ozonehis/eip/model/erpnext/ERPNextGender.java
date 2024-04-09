/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.model.erpnext;

import lombok.Getter;

@Getter
public enum ERPNextGender implements ERPNextDocument {
    FEMALE("Female"),
    GENDER_QUEER("Genderqueer"),
    MALE("Male"),
    OTHER("Other"),
    NON_CONFORMING("Non-conforming"),
    PREFER_NOT_TO_SAY("Prefer not to say"),
    TRANSGENDER("Transgender");

    private final String value;

    ERPNextGender(String value) {
        this.value = value;
    }
}
