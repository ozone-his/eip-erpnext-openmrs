package com.ozonehis.eip.erpnext.openmrs;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Constants {

    public static final String HEADER_FRAPPE_DOCTYPE = "CamelFrappe.doctype";

    public static final String HEADER_FRAPPE_RESOURCE = "CamelFrappe.resource";

    public static final String HEADER_FRAPPE_NAME = "CamelFrappe.name";

    public static final String HEADER_DOCUMENT_EXISTS = "erpnext.document.exists";

    public static final String HEADER_DOCUMENT_SKIP = "erpnext.document.skip";

    public static final String HEADER_DOCUMENT_ID = "erpnext.document.id";
    public static final String HEADER_ENABLE_PATIENT_SYNC = "enable.patient.sync";

    public static final String EXCHANGE_PROPERTY_ERPNEXT_ADDRESS = "erpnext.document.address";

    public static final String EXCHANGE_PROPERTY_ERPNEXT_ADDRESS_NAME = "erpnext.document.address.name";

    public static final String FHIR_RESOURCE_TYPE = "fhir.resource.type";
}
