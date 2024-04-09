package com.ozonehis.eip.mappers.erpnext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.ozonehis.eip.model.erpnext.Address;
import org.hl7.fhir.r4.model.Address.AddressUse;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddressMapperTest {

    private static final String ADDRESS_EXTENSION_URL = "http://fhir.openmrs.org/ext/address";

    private static final String ADDRESS_ID = "ba775d1d-daca-4058-ab8b-9eaaf45b0918";

    private AddressMapper addressMapper;

    @BeforeEach
    public void setup() {
        addressMapper = new AddressMapper();
    }

    @Test
    public void shouldMapFhirAddressToErpNextAddress() {
        org.hl7.fhir.r4.model.Address fhirAddress = getFhirAddress();
        Address erpNextAddress = addressMapper.toERPNext(fhirAddress);

        assertEquals(ADDRESS_ID, erpNextAddress.getAddressName());
        assertEquals("Test City", erpNextAddress.getCity());
        assertEquals("Test Country", erpNextAddress.getCountry());
        assertEquals("12345", erpNextAddress.getPostalCode());
        assertEquals("Test State", erpNextAddress.getState());
        assertEquals("Personal", erpNextAddress.getAddressType());
        assertEquals("Test Address Line 1", erpNextAddress.getAddressLine1());
    }

    private static org.hl7.fhir.r4.model.Address getFhirAddress() {
        org.hl7.fhir.r4.model.Address fhirAddress = new org.hl7.fhir.r4.model.Address();
        fhirAddress.setId(ADDRESS_ID);
        fhirAddress.setCity("Test City");
        fhirAddress.setCountry("Test Country");
        fhirAddress.setPostalCode("12345");
        fhirAddress.setState("Test State");
        fhirAddress.setUse(AddressUse.HOME);
        fhirAddress
                .addExtension()
                .setUrl(ADDRESS_EXTENSION_URL)
                .addExtension(new Extension(
                        "http://fhir.openmrs.org/ext/address#address1", new StringType("Test Address Line 1")))
                .addExtension(new Extension(
                        "http://fhir.openmrs.org/ext/address#address2", new StringType("Test Address Line 2")));
        return fhirAddress;
    }

    @Test
    public void shouldReturnNullAddressTypeWhenFhirAddressUseIsNotHome() {
        org.hl7.fhir.r4.model.Address fhirAddress = getFhirAddress();
        fhirAddress.setUse(AddressUse.WORK);

        Address erpNextAddress = addressMapper.toERPNext(fhirAddress);

        assertNull(erpNextAddress.getAddressType());
    }

    @Test
    public void shouldReturnEmptyAddressLinesWhenNoAddressExtensions() {
        org.hl7.fhir.r4.model.Address fhirAddress = new org.hl7.fhir.r4.model.Address();
        fhirAddress.setId(ADDRESS_ID);

        Address erpNextAddress = addressMapper.toERPNext(fhirAddress);

        assertNull(erpNextAddress.getAddressLine1());
        assertNull(erpNextAddress.getAddressLine2());
    }
}
