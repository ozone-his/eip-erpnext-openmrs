package com.ozonehis.eip.mappers.erpnext;

import com.ozonehis.eip.mappers.ToERPNextMapping;
import com.ozonehis.eip.model.erpnext.Address;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.Extension;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper implements ToERPNextMapping<org.hl7.fhir.r4.model.Address, Address> {

    private static final String ADDRESS_TYPE = "Personal";

    private static final String ADDRESS_EXTENSION_URL = "http://fhir.openmrs.org/ext/address";

    private static final String ADDRESS1_EXTENSION = "http://fhir.openmrs.org/ext/address#address1";

    private static final String ADDRESS2_EXTENSION = "http://fhir.openmrs.org/ext/address#address2";

    @Override
    public Address toERPNext(org.hl7.fhir.r4.model.Address fhirAddress) {
        Address address = new Address();
        address.setAddressName(fhirAddress.getIdElement().getValue());
        address.setCity(fhirAddress.getCity());
        address.setCountry(fhirAddress.getCountry());
        address.setPostalCode(fhirAddress.getPostalCode());
        address.setState(fhirAddress.getState());
        address.setPrimaryAddress(true);

        if (fhirAddress.hasExtension()) {
            List<Extension> extensions = fhirAddress.getExtension();
            List<Extension> addressExtensions = extensions.stream()
                    .filter(extension -> extension.getUrl().equals(ADDRESS_EXTENSION_URL))
                    .findFirst()
                    .map(Element::getExtension)
                    .orElse(new ArrayList<>());

            addressExtensions.stream()
                    .filter(extension -> extension.getUrl().equals(ADDRESS1_EXTENSION))
                    .findFirst()
                    .ifPresent(extension ->
                            address.setAddressLine1(extension.getValue().toString()));

            addressExtensions.stream()
                    .filter(extension -> extension.getUrl().equals(ADDRESS2_EXTENSION))
                    .findFirst()
                    .ifPresent(extension ->
                            address.setAddressLine2(extension.getValue().toString()));
        }

        if (fhirAddress.hasUse()) {
            // TODO: Check if this is the correct way to map the address type
            if (fhirAddress.getUse().equals(org.hl7.fhir.r4.model.Address.AddressUse.HOME))
                address.setAddressType(ADDRESS_TYPE);
        }

        return address;
    }
}
