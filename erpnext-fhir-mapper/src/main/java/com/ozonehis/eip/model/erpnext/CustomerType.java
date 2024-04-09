package com.ozonehis.eip.model.erpnext;

import lombok.Getter;

@Getter
public enum CustomerType {
    INDIVIDUAL("Individual"),
    COMPANY("Company"),
    PARTNERSHIP("Partnership"),
    SOLE_PROPRIETORSHIP("Sole Proprietorship");

    private final String value;

    CustomerType(String value) {
        this.value = value;
    }
}
