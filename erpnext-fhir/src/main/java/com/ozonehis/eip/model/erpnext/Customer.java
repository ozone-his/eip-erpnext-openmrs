package com.ozonehis.eip.model.erpnext;

import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@NoArgsConstructor
public class Customer implements ERPNextDocument {

    @Nonnull
    private String customerId;

    @Nonnull
    private String firstName;

    private String middleName;

    private String lastName;
}
