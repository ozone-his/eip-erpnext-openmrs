package com.ozonehis.eip.mappers;

import org.hl7.fhir.r4.model.Resource;

/**
 * An Interface for mapping from FHIR Resources to ERPNext Documents
 *
 * @param <F> FHIR Resource
 * @param <E> ERPNext Document
 */
public interface ToFhirMapping<F extends Resource, E> {

    /**
     * Maps an ERPNext Document to a FHIR Resource
     *
     * @param erpnextDocument ERPNext Document
     * @return FHIR Resource
     */
    F toFhir(E erpnextDocument);
}
