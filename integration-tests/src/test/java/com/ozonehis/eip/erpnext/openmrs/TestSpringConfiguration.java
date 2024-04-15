/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs;

import com.ozonehis.eip.erpnext.openmrs.config.ErpNextFrappeConfiguration;
import org.springframework.context.annotation.Import;

@Import({ErpNextFrappeConfiguration.class})
public class TestSpringConfiguration {}
