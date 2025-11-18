package com.realestatecrm.dto.savedsearch;

import java.time.LocalDateTime;
import java.util.List;

public class SavedSearchResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String name;
    private String description;
    private List<SearchFilterDTO> filters;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public SavedSearchResponse(Long id, Long userId, String userName, String name,
                               String description, List<SearchFilterDTO> filters,
                               LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
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

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
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
