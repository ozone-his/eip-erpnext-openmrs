package com.ozonehis.eip.mappers;

import com.ozonehis.eip.model.erpnext.ERPNextDocument;
import org.hl7.fhir.r4.model.Resource;

/**
 * An Interface for mapping from FHIR Resources to ERPNext Documents
 *
 * @param <F> FHIR Resource
 * @param <E> ERPNext Document
 */
public interface ToERPNextMapping<F extends Resource, E extends ERPNextDocument> {

    /**
     * Maps a FHIR Resource to an ERPNext Document
     *
     * @param fhirResource FHIR Resource
     * @return ERPNext Document
     */
    E toERPNext(F fhirResource);
}
