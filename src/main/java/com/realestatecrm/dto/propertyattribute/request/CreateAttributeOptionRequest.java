package com.realestatecrm.dto.propertyattribute.request;

import jakarta.validation.constraints.NotBlank;

public class CreateAttributeOptionRequest {
    @NotBlank
    private String optionValue;
    private Integer displayOrder;

    // Getters and setters
    public String getOptionValue() { return optionValue; }
    public void setOptionValue(String optionValue) { this.optionValue = optionValue; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}