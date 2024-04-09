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
