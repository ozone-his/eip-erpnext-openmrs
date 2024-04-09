package com.ozonehis.eip.mappers;

import com.ozonehis.eip.model.erpnext.ERPNextDocument;

/**
 * An Interface for mapping from FHIR Resources to ERPNext Documents
 *
 * @param <F> FHIR Resource
 * @param <E> ERPNext Document
 */
public interface ToERPNextMapping<F, E extends ERPNextDocument> {

    /**
     * Maps a FHIR Resource to an ERPNext Document
     *
     * @param fhirResource FHIR Resource
     * @return ERPNext Document
     */
    E toERPNext(F fhirResource);
}
