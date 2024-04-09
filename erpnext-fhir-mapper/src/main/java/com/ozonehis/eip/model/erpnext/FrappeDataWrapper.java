package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * A Wrapper for Frappe Response
 *
 * @param <T> Wrapped Data
 */
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FrappeDataWrapper<T extends ERPNextDocument> {

    @JsonProperty("data")
    private List<T> data;
}
