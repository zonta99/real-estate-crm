package com.realestatecrm.dto.customer.request;

import com.realestatecrm.enums.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateCustomerInteractionRequest {

    @NotNull(message = "Interaction type is required")
    private InteractionType type;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String notes;

    @NotNull(message = "Interaction date is required")
    private LocalDateTime interactionDate;

    private Integer durationMinutes;

    private Long relatedPropertyId;

    // Constructors
    public CreateCustomerInteractionRequest() {}

    public CreateCustomerInteractionRequest(InteractionType type, String subject,
                                            LocalDateTime interactionDate) {
        this.type = type;
        this.subject = subject;
        this.interactionDate = interactionDate;
    }

    // Getters and Setters
    public InteractionType getType() {
        return type;
    }

    public void setType(InteractionType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getInteractionDate() {
        return interactionDate;
    }

    public void setInteractionDate(LocalDateTime interactionDate) {
        this.interactionDate = interactionDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Long getRelatedPropertyId() {
        return relatedPropertyId;
    }

    public void setRelatedPropertyId(Long relatedPropertyId) {
        this.relatedPropertyId = relatedPropertyId;
    }
}
