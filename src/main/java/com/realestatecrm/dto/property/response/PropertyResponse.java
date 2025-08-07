package com.realestatecrm.dto.property.response;

import com.realestatecrm.enums.PropertyStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PropertyResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Long agentId;
    private String agentName;
    private PropertyStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public PropertyResponse(Long id, String title, String description, BigDecimal price,
                            Long agentId, String agentName, PropertyStatus status,
                            LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.agentId = agentId;
        this.agentName = agentName;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Long getAgentId() { return agentId; }
    public String getAgentName() { return agentName; }
    public PropertyStatus getStatus() { return status; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
}