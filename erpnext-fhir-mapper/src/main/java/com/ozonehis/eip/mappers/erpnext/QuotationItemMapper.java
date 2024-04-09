package com.ozonehis.eip.mappers.erpnext;

import com.ozonehis.eip.mappers.ToERPNextMapping;
import com.ozonehis.eip.model.erpnext.QuotationItem;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
public class QuotationItemMapper<R extends Resource> implements ToERPNextMapping<R, QuotationItem> {

    @Override
    public QuotationItem toERPNext(R resource) {
        QuotationItem quotationItem = new QuotationItem();
        if (resource instanceof ServiceRequest serviceRequest) {
            if (serviceRequest.hasCode()) {
                quotationItem.setItemCode(
                        serviceRequest.getCode().getCodingFirstRep().getCode());
            }
            quotationItem.setQuantity(1); // default quantity is 1 for serviceRequests.
            String requesterDisplay = serviceRequest.getRequester().getDisplay();
            String serviceDisplay = serviceRequest.getCode().getText();
            quotationItem.setDescription(serviceDisplay + " | Requester: " + requesterDisplay);

        } else if (resource instanceof MedicationRequest medicationRequest) {
            Quantity quantity = medicationRequest.getDispenseRequest().getQuantity();
            quotationItem.setQuantity(quantity.getValue().intValue());
            String medicationCode =
                    medicationRequest.getMedicationReference().getReference().split("/")[1];
            quotationItem.setItemCode(medicationCode);
            String requesterDisplay = medicationRequest.getRequester().getDisplay();
            String medicationDisplay =
                    medicationRequest.getMedicationReference().getDisplay();
            quotationItem.setDescription(medicationDisplay + " | Requester: " + requesterDisplay);
            // Add dosage instructions to the notes.
            quotationItem.setNotes(constructDosageInstructionsText(medicationRequest));

        } else {
            throw new IllegalArgumentException("Quotation Mapper Unsupported resource type: "
                    + resource.getClass().getName());
        }
        return quotationItem;
    }

    /**
     * Construct dosage instructions text from the medication request. Format: DOSE 10 tablet — oral — thrice daily — for 10
     * days — REFILLS 1 — QUANTITY 30 Tablet
     *
     * @param medicationRequest medication request
     * @return dosage instructions text
     */
    protected String constructDosageInstructionsText(MedicationRequest medicationRequest) {
        Dosage dosage = medicationRequest.getDosageInstructionFirstRep();
        StringBuilder dosageInstructions = new StringBuilder();

        if (dosage.hasDoseAndRate()) {
            dosageInstructions.append("DOSE ");
            dosageInstructions.append(
                    dosage.getDoseAndRateFirstRep().getDoseQuantity().getValue());
            dosageInstructions.append(" ");
            dosageInstructions.append(
                    dosage.getDoseAndRateFirstRep().getDoseQuantity().getUnit());
        }

        if (dosage.hasRoute()) {
            dosageInstructions.append(" — ");
            dosageInstructions.append(dosage.getRoute().getText());
        }

        if (dosage.hasTiming()) {
            dosageInstructions.append(" — ");
            dosageInstructions.append(dosage.getTiming().getCode().getText());
            dosageInstructions.append(" — for ");
            dosageInstructions.append(dosage.getTiming().getRepeat().getDuration());
            dosageInstructions.append(" ");
            dosageInstructions.append(
                    dosage.getTiming().getRepeat().getDurationUnit().getDisplay());
        }
        if (medicationRequest.hasDispenseRequest()) {
            dosageInstructions.append(" — REFILLS ");
            dosageInstructions.append(medicationRequest.getDispenseRequest().getNumberOfRepeatsAllowed());
            dosageInstructions.append(" - QUANTITY ");
            dosageInstructions.append(
                    medicationRequest.getDispenseRequest().getQuantity().getValue());
            dosageInstructions.append(" ");
            dosageInstructions.append(
                    medicationRequest.getDispenseRequest().getQuantity().getUnit());
        }
        return dosageInstructions.toString();
    }
}
