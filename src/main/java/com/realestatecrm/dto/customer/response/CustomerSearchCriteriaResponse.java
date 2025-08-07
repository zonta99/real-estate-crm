package com.realestatecrm.dto.customer.response;

import java.math.BigDecimal;

public class CustomerSearchCriteriaResponse {
    private Long id;
    private Long customerId;
    private Long attributeId;
    private String attributeName;
    private String dataType;
    private String textValue;
    private BigDecimal numberMinValue;
    private BigDecimal numberMaxValue;
    private Boolean booleanValue;
    private String multiSelectValue;

    public CustomerSearchCriteriaResponse(Long id, Long customerId, Long attributeId, String attributeName,
                                          String dataType, String textValue, BigDecimal numberMinValue,
                                          BigDecimal numberMaxValue, Boolean booleanValue, String multiSelectValue) {
        this.id = id;
        this.customerId = customerId;
        this.attributeId = attributeId;
        this.attributeName = attributeName;
        this.dataType = dataType;
        this.textValue = textValue;
        this.numberMinValue = numberMinValue;
        this.numberMaxValue = numberMaxValue;
        this.booleanValue = booleanValue;
        this.multiSelectValue = multiSelectValue;
    }

    // Getters
    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Long getAttributeId() { return attributeId; }
    public String getAttributeName() { return attributeName; }
    public String getDataType() { return dataType; }
    public String getTextValue() { return textValue; }
    public BigDecimal getNumberMinValue() { return numberMinValue; }
    public BigDecimal getNumberMaxValue() { return numberMaxValue; }
    public Boolean getBooleanValue() { return booleanValue; }
    public String getMultiSelectValue() { return multiSelectValue; }
}