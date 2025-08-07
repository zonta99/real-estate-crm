package com.realestatecrm.dto.property.response;

import java.time.LocalDateTime;

public class PropertySharingResponse {
    private Long id;
    private Long propertyId;
    private Long sharedWithUserId;
    private String sharedWithUserName;
    private Long sharedByUserId;
    private String sharedByUserName;
    private LocalDateTime createdDate;

    public PropertySharingResponse(Long id, Long propertyId, Long sharedWithUserId, String sharedWithUserName,
                                   Long sharedByUserId, String sharedByUserName, LocalDateTime createdDate) {
        this.id = id;
        this.propertyId = propertyId;
        this.sharedWithUserId = sharedWithUserId;
        this.sharedWithUserName = sharedWithUserName;
        this.sharedByUserId = sharedByUserId;
        this.sharedByUserName = sharedByUserName;
        this.createdDate = createdDate;
    }

    // Getters
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getSharedWithUserId() { return sharedWithUserId; }
    public String getSharedWithUserName() { return sharedWithUserName; }
    public Long getSharedByUserId() { return sharedByUserId; }
    public String getSharedByUserName() { return sharedByUserName; }
    public LocalDateTime getCreatedDate() { return createdDate; }
}