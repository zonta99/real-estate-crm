package com.realestatecrm.dto.customer.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SetSearchCriteriaRequest {
    @NotNull
    private Long attributeId;
    private String textValue;
    private BigDecimal numberMinValue;
    private BigDecimal numberMaxValue;
    private Boolean booleanValue;
    private String multiSelectValue;

    // Getters and setters
    public Long getAttributeId() { return attributeId; }
    public void setAttributeId(Long attributeId) { this.attributeId = attributeId; }
    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }
    public BigDecimal getNumberMinValue() { return numberMinValue; }
    public void setNumberMinValue(BigDecimal numberMinValue) { this.numberMinValue = numberMinValue; }
    public BigDecimal getNumberMaxValue() { return numberMaxValue; }
    public void setNumberMaxValue(BigDecimal numberMaxValue) { this.numberMaxValue = numberMaxValue; }
    public Boolean getBooleanValue() { return booleanValue; }
    public void setBooleanValue(Boolean booleanValue) { this.booleanValue = booleanValue; }
    public String getMultiSelectValue() { return multiSelectValue; }
    public void setMultiSelectValue(String multiSelectValue) { this.multiSelectValue = multiSelectValue; }
}