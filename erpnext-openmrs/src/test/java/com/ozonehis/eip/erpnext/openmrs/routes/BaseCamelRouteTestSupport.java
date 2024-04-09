package com.ozonehis.eip.erpnext.openmrs.routes;

import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;

public abstract class BaseCamelRouteTestSupport extends CamelSpringTestSupport {

    @Override
    public boolean isUseDebugger() {
        return true;
    }
}
