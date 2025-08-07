package com.realestatecrm.dto.propertyattribute.request;

import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAttributeRequest {
    @NotBlank
    private String name;

    @NotNull
    private PropertyDataType dataType;

    @NotNull
    private Boolean isRequired = false;

    @NotNull
    private Boolean isSearchable = true;

    @NotNull
    private PropertyCategory category;

    private Integer displayOrder;

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PropertyDataType getDataType() { return dataType; }
    public void setDataType(PropertyDataType dataType) { this.dataType = dataType; }
    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
    public PropertyCategory getCategory() { return category; }
    public void setCategory(PropertyCategory category) { this.category = category; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}