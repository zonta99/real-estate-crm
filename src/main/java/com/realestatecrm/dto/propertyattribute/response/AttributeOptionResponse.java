package com.realestatecrm.dto.propertyattribute.response;

public class AttributeOptionResponse {
    private Long id;
    private Long attributeId;
    private String optionValue;
    private Integer displayOrder;

    public AttributeOptionResponse(Long id, Long attributeId, String optionValue, Integer displayOrder) {
        this.id = id;
        this.attributeId = attributeId;
        this.optionValue = optionValue;
        this.displayOrder = displayOrder;
    }

    // Getters
    public Long getId() { return id; }
    public Long getAttributeId() { return attributeId; }
    public String getOptionValue() { return optionValue; }
    public Integer getDisplayOrder() { return displayOrder; }
}