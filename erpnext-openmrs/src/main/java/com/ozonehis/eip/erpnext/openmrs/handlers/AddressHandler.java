/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.erpnext.openmrs.handlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ozonehis.camel.frappe.sdk.api.FrappeClient;
import com.ozonehis.camel.frappe.sdk.api.FrappeClientException;
import com.ozonehis.camel.frappe.sdk.api.FrappeResponse;
import com.ozonehis.eip.erpnext.openmrs.Constants;
import com.ozonehis.eip.model.erpnext.Address;
import com.ozonehis.eip.model.erpnext.FrappeSingularDataWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.Setter;
import org.apache.camel.ProducerTemplate;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter
@Component
public class AddressHandler {

    @Autowired
    private FrappeClient frappeClient;

    /**
     *  Get an address by name
     *
     * @param addressName the name of the address
     * @return the address if it exists, empty otherwise
     */
    public Optional<Address> getAddress(String addressName) {
        try (FrappeResponse response = frappeClient.get("Address", addressName).execute()) {
            return switch (response.code()) {
                case HttpStatus.SC_NOT_FOUND -> Optional.empty();
                case HttpStatus.SC_OK -> {
                    TypeReference<FrappeSingularDataWrapper<Address>> typeReference = new TypeReference<>() {};

                    FrappeSingularDataWrapper<Address> addressWrapper = response.returnAs(typeReference);
                    yield Optional.of(addressWrapper.getData());
                }
                default -> throw new FrappeClientException("Error while fetching address with name: " + addressName
                        + " with error message:" + response.message());
            };
        } catch (FrappeClientException | IOException e) {
            throw new FrappeClientException("Error while fetching address", e);
        }
    }

    public void disableOldAddresses(ProducerTemplate producerTemplate, Address address) {
        if (getAddress(address.getAddressName()).isPresent()) {
            // Disable the address
            address.setDisabled(true);
            address.setPrimaryAddress(false);

            var headers = new HashMap<String, Object>();
            headers.put(Constants.HEADER_FRAPPE_NAME, address.getAddressName());
            headers.put(Constants.HEADER_FRAPPE_RESOURCE, address);
            headers.put(Constants.HEADER_FRAPPE_DOCTYPE, "Address");
            producerTemplate.sendBodyAndHeaders("direct:erpnext-update-address-route", address, headers);
        }
    }
}
