package com.realestatecrm.dto.property.request;

import jakarta.validation.constraints.NotNull;

public class SetPropertyValueRequest {
    @NotNull
    private Long attributeId;
    private Object value;

    // Getters and setters
    public Long getAttributeId() { return attributeId; }
    public void setAttributeId(Long attributeId) { this.attributeId = attributeId; }
    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }
}