package com.realestatecrm.dto.savedsearch;

import com.realestatecrm.enums.PropertyDataType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SearchFilterDTO {
    @NotNull
    private Long attributeId;

    @NotNull
    private PropertyDataType dataType;

    // For NUMBER ranges
    private BigDecimal minValue;
    private BigDecimal maxValue;

    // For DATE ranges
    private LocalDate minDate;
    private LocalDate maxDate;

    // For SINGLE_SELECT and MULTI_SELECT (array of option values)
    private List<String> selectedValues;

    // For TEXT contains search
    private String textValue;

    // For BOOLEAN
    private Boolean booleanValue;

    // Getters and setters
    public Long getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public PropertyDataType getDataType() {
        return dataType;
    }

    public void setDataType(PropertyDataType dataType) {
        this.dataType = dataType;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public LocalDate getMinDate() {
        return minDate;
    }

    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
    }

    public LocalDate getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(LocalDate maxDate) {
        this.maxDate = maxDate;
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
