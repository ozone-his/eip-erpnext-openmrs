/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.camel.test.infra.erpnext.services;

import lombok.NoArgsConstructor;
import org.apache.camel.test.infra.common.services.SimpleTestServiceBuilder;
import org.apache.camel.test.infra.common.services.SingletonService;

@NoArgsConstructor
public class ERPNextServiceFactory {

    static class SingletonERPNextService extends SingletonService<ERPNextService> implements ERPNextService {

        public SingletonERPNextService(ERPNextService service, String name) {
            super(service, name);
        }

        @Override
        public int getPort() {
            return getService().getPort();
        }

        public String getHost() {
            return getService().getHost();
        }

        @Override
        public String getHttpHostAddress() {
            return getService().getHttpHostAddress();
        }
    }

    public static SimpleTestServiceBuilder<ERPNextService> builder() {
        return new SimpleTestServiceBuilder<>("erpnext");
    }

    public static ERPNextService createService() {
        return builder().addLocalMapping(ERPNextLocalContainerService::new).build();
    }

    public static ERPNextService createSingletonService() {
        return SingletonServiceHolder.INSTANCE;
    }

    private static class SingletonServiceHolder {

        static final ERPNextService INSTANCE;

        static {
            SimpleTestServiceBuilder<ERPNextService> instance = builder();
            instance.addLocalMapping(() -> new SingletonERPNextService(new ERPNextLocalContainerService(), "erpnext"));
            INSTANCE = instance.build();
        }
    }
}
