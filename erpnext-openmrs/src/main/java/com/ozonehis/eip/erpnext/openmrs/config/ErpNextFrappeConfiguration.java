/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.config;

import com.ozonehis.camel.FrappeComponent;
import com.ozonehis.camel.FrappeConfiguration;
import com.ozonehis.camel.frappe.sdk.FrappeClientBuilder;
import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ErpNextFrappeConfiguration {

    @Value("${erpnext.serverUrl}")
    private String serverUrl;

    @Value("${erpnext.username}")
    private String username;

    @Value("${erpnext.password}")
    private String password;

    @Bean
    CamelContextConfiguration registerFrappeConfiguration() {
        return new CamelContextConfiguration() {

            @Override
            public void beforeApplicationStart(CamelContext camelContext) {
                FrappeConfiguration frappeConfiguration = new FrappeConfiguration();

                if (serverUrl != null
                        && !serverUrl.isBlank()
                        && username != null
                        && !username.isBlank()
                        && password != null
                        && !password.isBlank()) {
                    frappeConfiguration.setFrappeClient(FrappeClientBuilder.newClient(serverUrl, username, password)
                            .build());
                } else {
                    throw new IllegalStateException("ERPNext server URL, username and password must be provided");
                }

                camelContext.getComponent("frappe", FrappeComponent.class).setConfiguration(frappeConfiguration);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {}
        };
    }

    @Bean
    FrappeClient frappeClient() {
        return FrappeClientBuilder.newClient(serverUrl, username, password).build();
    }
}
