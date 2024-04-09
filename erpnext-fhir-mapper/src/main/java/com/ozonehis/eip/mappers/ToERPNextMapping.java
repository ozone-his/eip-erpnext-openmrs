/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
