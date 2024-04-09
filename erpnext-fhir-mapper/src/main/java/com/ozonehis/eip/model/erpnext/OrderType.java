package com.ozonehis.eip.model.erpnext;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderType {
    SALES("Sales"),
    MAINTENANCE("Maintenance"),
    SHOPPING_CART("Shopping Cart");

    private final String name;
}
