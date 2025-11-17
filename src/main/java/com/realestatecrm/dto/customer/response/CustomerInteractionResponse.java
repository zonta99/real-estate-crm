package com.realestatecrm.dto.customer.response;

import com.realestatecrm.enums.InteractionType;

import java.time.LocalDateTime;

public class CustomerInteractionResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long userId;
    private String userName;
    private InteractionType type;
    private String subject;
    private String notes;
    private LocalDateTime interactionDate;
    private Integer durationMinutes;
    private Long relatedPropertyId;
    private String relatedPropertyTitle;
    private LocalDateTime createdDate;

    // Constructors
    public CustomerInteractionResponse() {}

    public CustomerInteractionResponse(Long id, Long customerId, String customerName,
                                       Long userId, String userName, InteractionType type,
                                       String subject, String notes, LocalDateTime interactionDate,
                                       Integer durationMinutes, Long relatedPropertyId,
                                       String relatedPropertyTitle, LocalDateTime createdDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.subject = subject;
        this.notes = notes;
        this.interactionDate = interactionDate;
        this.durationMinutes = durationMinutes;
        this.relatedPropertyId = relatedPropertyId;
        this.relatedPropertyTitle = relatedPropertyTitle;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

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

    public String getRelatedPropertyTitle() {
        return relatedPropertyTitle;
    }

    public void setRelatedPropertyTitle(String relatedPropertyTitle) {
        this.relatedPropertyTitle = relatedPropertyTitle;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
