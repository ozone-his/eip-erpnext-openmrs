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
import lombok.Setter;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Setter
@Component
public class AddressHandler {

    @Autowired
    private FrappeClient frappeClient;

    public boolean addressExists(String addressName) {
        try (FrappeResponse response = frappeClient.get("Address", addressName).execute()) {
            if (response.code() == HttpStatus.SC_OK) {
                TypeReference<FrappeSingularDataWrapper<com.ozonehis.eip.model.erpnext.Address>> typeReference =
                        new TypeReference<>() {};

                FrappeSingularDataWrapper<com.ozonehis.eip.model.erpnext.Address> addressWrapper =
                        response.returnAs(typeReference);
                return addressWrapper.getData().getAddressName().equals(addressName);
            } else {
                return false;
            }
        } catch (FrappeClientException | IOException e) {
            throw new CamelExecutionException("Error while checking if address exists", null, e);
        }
    }

    public void disableOldAddresses(ProducerTemplate producerTemplate, Address address) {
        if (addressExists(address.getAddressName())) {
            // Disable the address
            address.setDisabled(true);
            address.setPrimaryAddress(false);

            var headers = new HashMap<String, Object>();
            headers.put(Constants.HEADER_FRAPPE_NAME, address.getAddressName());
            headers.put(Constants.HEADER_FRAPPE_RESOURCE, address);
            headers.put(Constants.HEADER_FRAPPE_DOCTYPE, "Address");
            producerTemplate.sendBodyAndHeaders("frappe://put/resource", address, headers);
        }
    }
}
