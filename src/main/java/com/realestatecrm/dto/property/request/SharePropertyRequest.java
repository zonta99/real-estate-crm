package com.realestatecrm.dto.property.request;

import jakarta.validation.constraints.NotNull;

public class SharePropertyRequest {
    @NotNull
    private Long sharedWithUserId;

    // Getters and setters
    public Long getSharedWithUserId() { return sharedWithUserId; }
    public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
}