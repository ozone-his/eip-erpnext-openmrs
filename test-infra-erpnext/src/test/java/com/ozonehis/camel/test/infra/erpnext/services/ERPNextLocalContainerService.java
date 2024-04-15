/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.camel.test.infra.erpnext.services;

import com.ozonehis.camel.test.infra.erpnext.common.ERPNextProperties;
import java.io.File;
import java.net.URL;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
public class ERPNextLocalContainerService implements ERPNextService {

    private final ComposeContainer container;

    private final String SERVICE_NAME = "frontend";

    public ERPNextLocalContainerService() {
        this.container = initContainer();
    }

    @Override
    public int getPort() {
        return container.getServicePort(SERVICE_NAME, ERPNextProperties.DEFAULT_SERVICE_PORT);
    }

    @Override
    public String getHost() {
        return container.getServiceHost(SERVICE_NAME, ERPNextProperties.DEFAULT_SERVICE_PORT);
    }

    @Override
    public void registerProperties() {
        System.setProperty(ERPNextProperties.ERPNEXT_HOST, getHost());
        System.setProperty(ERPNextProperties.ERPNEXT_PORT, String.valueOf(getPort()));
        var apiUrl = "http://" + getHost() + ":" + getPort() + "/api/resource";
        System.setProperty(ERPNextProperties.ERPNEXT_SERVER_URL, apiUrl);
    }

    @Override
    public void initialize() {
        log.info("Starting ERPNext container...");
        container.start();

        registerProperties();
        log.info("ERPNext container started");
    }

    @Override
    public void shutdown() {
        log.info("Stopping the ERPNext container.");
        container.stop();
        log.info("ERPNext container stopped.");
    }

    protected ComposeContainer initContainer() {
        try (var container = new ComposeContainer(getFile("docker/compose-erpnext.yaml"))
                .withLocalCompose(true)
                .withStartupTimeout(java.time.Duration.ofMinutes(5))
                .withExposedService(SERVICE_NAME, ERPNextProperties.DEFAULT_SERVICE_PORT, Wait.forListeningPort())
                .withTailChildContainers(true)) {

            return container;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected File getFile(String fileName) {
        URL url = getClass().getClassLoader().getResource(fileName);
        return new File(Objects.requireNonNull(url).getPath());
    }
}
