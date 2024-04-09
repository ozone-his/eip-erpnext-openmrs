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
