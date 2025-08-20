package com.realestatecrm.dto.property.response;

public class AttributeValueResponse {
    private Long id;
    private Long propertyId;
    private Long attributeId;
    private String attributeName;
    private String dataType;
    private Object value;

    public AttributeValueResponse(Long id, Long propertyId, Long attributeId, String attributeName,
                                  Object dataType, Object value) {
        this.id = id;
        this.propertyId = propertyId;
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.dataType = dataType.toString();
        this.value = value;
    }

    // Getters
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getAttributeId() { return attributeId; }
    public String getAttributeName() { return attributeName; }
    public String getDataType() { return dataType; }
    public Object getValue() { return value; }
}
