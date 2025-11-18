package com.realestatecrm.dto.savedsearch;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class PropertySearchCriteriaRequest {
    @NotEmpty(message = "At least one filter is required")
    @Valid
    private List<SearchFilterDTO> filters;

    @Min(value = 0, message = "Page number must be >= 0")
    private Integer page = 0;

    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    private Integer size = 20;

    private String sort = "createdDate,desc";

    // Getters and setters
    public List<SearchFilterDTO> getFilters() {
        return filters;
    }

    public void setFilters(List<SearchFilterDTO> filters) {
        this.filters = filters;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
