/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.camel.test.infra.erpnext.common;

import lombok.NoArgsConstructor;
import org.apache.camel.test.infra.common.services.ContainerEnvironmentUtil;

@NoArgsConstructor
public final class ERPNextProperties {

    public static final String ERPNEXT_HOST = "erpnext.host";

    public static final String ERPNEXT_PORT = "erpnext.port";

    public static final String ERPNEXT_CONTAINER = "erpnext.container";

    public static final String ERPNEXT_SERVER_URL = "erpnext.serverUrl";

    public static final int DEFAULT_SERVICE_PORT = 8080;

    public static final String ERPNEXT_CONTAINER_STARTUP =
            ERPNEXT_CONTAINER + ContainerEnvironmentUtil.STARTUP_ATTEMPTS_PROPERTY;
}
