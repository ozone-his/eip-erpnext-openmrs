package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuotationItem implements ERPNextDocument {

    @JsonProperty("item_code")
    private String itemCode;

    @JsonProperty("qty")
    private float quantity;

    @JsonProperty("description")
    private String description;

    @JsonProperty("additional_notes")
    private String notes;
}
