/*
 * Copyright © 2024, Ozone HIS <info@ozone-his.com>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.ozonehis.eip.mappers.erpnext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ozonehis.eip.model.erpnext.QuotationItem;
import java.util.List;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuotationItemMapperTest {

    public static final String MEDICATION_REQUEST_ID = "1cdda1ce-7f98-4bc7-9d20-8b4e953d972a";

    public static final String MEDICATION_ID = "c95ab33a-8558-4705-8a7a-3b4e580270c7";

    private static final String SERVICE_REQUEST_ID = "4ed050e1-c1be-4b4c-b407-c48d2db49b87";

    private static final String TABLET_UNIT = "Tablet";

    private static final String COMPLETE_BLOOD_COUNT_CODE = "446ac22c-24f2-40ad-98f4-65f026f434e9";

    private static final String COMPLETE_BLOOD_COUNT_DISPLAY = "Complete Blood Count";

    private static final String COMPLETE_BLOOD_COUNT_ID = "3d597a15-b08f-4c35-8a42-0c15f9a19fa6";

    private static final String PRACTITIONER_ID = "e5ca6578-fb37-4900-a054-c68db82a551c";

    private QuotationItemMapper<Resource> quotationItemMapper;

    @BeforeEach
    void setUp() {
        quotationItemMapper = new QuotationItemMapper<>();
    }

    @Test
    @DisplayName("Should map service request to quotation item")
    void shouldMapServiceRequestToQuotationItem() {
        // setup
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setId(SERVICE_REQUEST_ID);

        // code
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(COMPLETE_BLOOD_COUNT_CODE);
        coding.setDisplay(COMPLETE_BLOOD_COUNT_DISPLAY);
        coding.setId(COMPLETE_BLOOD_COUNT_ID);
        codeableConcept.addCoding(coding);
        codeableConcept.setText(COMPLETE_BLOOD_COUNT_DISPLAY);
        serviceRequest.setCode(codeableConcept);

        Reference requester = new Reference();
        requester.setId("1cdda1ce-7f98-4bc7-9d20-8b4e953d972a");
        requester.setReference("Practitioner/" + PRACTITIONER_ID);
        requester.setDisplay("John Doe");
        serviceRequest.setRequester(requester);

        // Act
        QuotationItem quotationItem = quotationItemMapper.toERPNext(serviceRequest);

        // verify
        assertNotNull(quotationItem);
        assertEquals(COMPLETE_BLOOD_COUNT_CODE, quotationItem.getItemCode());
        assertEquals(1, quotationItem.getQuantity());
        assertEquals(COMPLETE_BLOOD_COUNT_DISPLAY + " | Requester: John Doe", quotationItem.getDescription());
    }

    @Test
    @DisplayName("Should map medication request to quotation item")
    void shouldMapMedicationRequestToQuotationItem() {
        // setup
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setId(MEDICATION_REQUEST_ID);

        // dispense request
        medicationRequest.setDispenseRequest(new MedicationRequest.MedicationRequestDispenseRequestComponent()
                .setQuantity(new Quantity().setValue(10)));
        medicationRequest.setMedication(new Reference("Medication/" + MEDICATION_ID).setDisplay("medication"));
        medicationRequest.setRequester(new Reference().setDisplay("requester"));
        MedicationRequest.MedicationRequestDispenseRequestComponent dispenseRequest =
                new MedicationRequest.MedicationRequestDispenseRequestComponent();

        // quantity
        Quantity quantity = new Quantity();
        quantity.setValue(10);
        quantity.setUnit(TABLET_UNIT);
        dispenseRequest.setQuantity(quantity);
        medicationRequest.setDispenseRequest(dispenseRequest);

        // dosage instruction
        Dosage.DosageDoseAndRateComponent doseAndRate = new Dosage.DosageDoseAndRateComponent();
        Quantity doseQuantity = new Quantity();
        doseQuantity.setValue(10);
        doseQuantity.setUnit(TABLET_UNIT);
        doseAndRate.setDose(doseQuantity);
        Dosage dosage = new Dosage();
        dosage.setDoseAndRate(List.of(doseAndRate));

        Timing timing = new Timing();
        timing.setCode(new CodeableConcept().setText("thrice daily"));
        Timing.TimingRepeatComponent repeat = new Timing.TimingRepeatComponent();
        repeat.setFrequency(3);
        repeat.setDuration(10);
        repeat.setDurationUnit(Timing.UnitsOfTime.D);
        timing.setRepeat(repeat);
        dosage.setTiming(timing);
        medicationRequest.setDosageInstruction(List.of(dosage));

        // Act
        QuotationItem quotationItem = quotationItemMapper.toERPNext(medicationRequest);

        // verify
        assertNotNull(quotationItem);
        assertEquals(MEDICATION_ID, quotationItem.getItemCode());
        assertEquals(10, quotationItem.getQuantity());
        assertEquals("medication | Requester: requester", quotationItem.getDescription());
        assertEquals(
                "DOSE 10 Tablet — thrice daily — for 10 day — REFILLS 0 - QUANTITY 10 Tablet",
                quotationItem.getNotes());
    }

    @Test
    @DisplayName("Should throw exception for unsupported resource type")
    void shouldThrowExceptionForUnsupportedResourceType() {
        Resource unsupportedResource = new Patient();
        assertThrows(IllegalArgumentException.class, () -> quotationItemMapper.toERPNext(unsupportedResource));
    }
}
