package com.realestatecrm.dto.propertyattribute.response;

import java.time.LocalDateTime;
import java.util.List;

public class PropertyAttributeResponse {
    private Long id;
    private String name;
    private String dataType;
    private Boolean isRequired;
    private Boolean isSearchable;
    private String category;
    private Integer displayOrder;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<AttributeOptionResponse> options;

    public PropertyAttributeResponse(Long id, String name, String dataType, Boolean isRequired,
                                     Boolean isSearchable, String category, Integer displayOrder,
                                     LocalDateTime createdDate, LocalDateTime updatedDate,
                                     List<AttributeOptionResponse> options) {
        this.id = id;
        this.name = name;
        this.dataType = dataType;
        this.isRequired = isRequired;
        this.isSearchable = isSearchable;
        this.category = category;
        this.displayOrder = displayOrder;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.options = options;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDataType() { return dataType; }
    public Boolean getIsRequired() { return isRequired; }
    public Boolean getIsSearchable() { return isSearchable; }
    public String getCategory() { return category; }
    public Integer getDisplayOrder() { return displayOrder; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public List<AttributeOptionResponse> getOptions() { return options; }
}