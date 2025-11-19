package com.realestatecrm.dto.savedsearch;

import java.time.LocalDateTime;
import java.util.List;

public class SavedSearchResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long agentId;
    private String agentName;
    private String name;
    private String description;
    private List<SearchFilterDTO> filters;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public SavedSearchResponse(Long id, Long customerId, String customerName, Long agentId, String agentName,
                               String name, String description, List<SearchFilterDTO> filters,
                               LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.agentId = agentId;
        this.agentName = agentName;
        this.name = name;
        this.description = description;
        this.filters = filters;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters only
    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public Long getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<SearchFilterDTO> getFilters() {
        return filters;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
}
