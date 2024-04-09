package com.ozonehis.eip.erpnext.openmrs.processors;

import static org.openmrs.eip.fhir.Constants.HEADER_FHIR_EVENT_TYPE;

import java.util.HashMap;
import java.util.Map;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

public abstract class BaseProcessorTest extends CamelSpringTestSupport {

    protected Exchange createExchange(Resource resource, String eventType) {
        Message message = new DefaultMessage(new DefaultCamelContext());
        message.setBody(resource);
        Map<String, Object> headers = new HashMap<>();
        headers.put(HEADER_FHIR_EVENT_TYPE, eventType);
        message.setHeaders(headers);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.setMessage(message);
        return exchange;
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new StaticApplicationContext();
    }
}
