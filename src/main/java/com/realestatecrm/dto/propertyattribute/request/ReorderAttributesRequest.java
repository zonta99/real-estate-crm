package com.realestatecrm.dto.propertyattribute.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ReorderAttributesRequest {
    @NotNull
    private List<Long> attributeIds;

    // Getters and setters
    public List<Long> getAttributeIds() { return attributeIds; }
    public void setAttributeIds(List<Long> attributeIds) { this.attributeIds = attributeIds; }
}