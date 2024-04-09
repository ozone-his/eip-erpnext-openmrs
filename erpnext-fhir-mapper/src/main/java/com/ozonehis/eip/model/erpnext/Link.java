package com.ozonehis.eip.model.erpnext;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {

    @JsonProperty("name")
    private String name;

    @JsonProperty("link_doctype")
    private String linkDoctype;

    @JsonProperty("link_name")
    private String linkName;

    @JsonProperty("link_title")
    private String linkTitle;

    @JsonProperty("parent")
    private String parent;

    @JsonProperty("parenttype")
    private String parentType;

    @JsonProperty("parentfield")
    private String parentField;

    @JsonProperty("doctype")
    private String doctype = "Dynamic Link";
}
