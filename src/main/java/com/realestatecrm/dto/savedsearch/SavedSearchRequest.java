package com.realestatecrm.dto.savedsearch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SavedSearchRequest {
    @NotBlank(message = "Search name is required")
    @Size(max = 100, message = "Search name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotEmpty(message = "At least one filter is required")
    @Valid
    private List<SearchFilterDTO> filters;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SearchFilterDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilterDTO> filters) {
        this.filters = filters;
    }
}
