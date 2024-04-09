package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer implements ERPNextDocument {

    @Nonnull
    @JsonProperty("name")
    private String customerId;

    @Nonnull
    @JsonProperty("customer_name")
    private String customerName;

    @Nonnull
    @JsonProperty("customer_type")
    private String customerType;

    private String gender;
}
