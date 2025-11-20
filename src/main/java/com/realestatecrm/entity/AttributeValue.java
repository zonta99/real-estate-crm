package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "attribute_value",
        uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "attribute_id"}))
public class AttributeValue extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private PropertyAttribute attribute;

    // Typed value columns - only one should be populated based on attribute data type
    @Column(name = "text_value")
    private String textValue;

    @Column(name = "number_value", precision = 15, scale = 2)
    private BigDecimal numberValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "multi_select_value", columnDefinition = "TEXT")
    private String multiSelectValue; // JSON array as string

    @Column(name = "date_value")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateValue;

    // Constructors
    public AttributeValue() {}

    public AttributeValue(Property property, PropertyAttribute attribute) {
        this.property = property;
        this.attribute = attribute;
    }

    // Static factory methods for type-safe value creation
    public static AttributeValue createTextValue(Property property, PropertyAttribute attribute, String value) {
        AttributeValue pv = new AttributeValue(property, attribute);
        pv.setTextValue(value);
        return pv;
    }

    public static AttributeValue createNumberValue(Property property, PropertyAttribute attribute, BigDecimal value) {
        AttributeValue pv = new AttributeValue(property, attribute);
        pv.setNumberValue(value);
        return pv;
    }

    public static AttributeValue createBooleanValue(Property property, PropertyAttribute attribute, Boolean value) {
        AttributeValue pv = new AttributeValue(property, attribute);
        pv.setBooleanValue(value);
        return pv;
    }

    public static AttributeValue createMultiSelectValue(Property property, PropertyAttribute attribute, String jsonValue) {
        AttributeValue pv = new AttributeValue(property, attribute);
        pv.setMultiSelectValue(jsonValue);
        return pv;
    }

    // Getters and Setters
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public PropertyAttribute getAttribute() { return attribute; }
    public void setAttribute(PropertyAttribute attribute) { this.attribute = attribute; }

    public String getTextValue() { return textValue; }
    public void setTextValue(String textValue) { this.textValue = textValue; }

    public BigDecimal getNumberValue() { return numberValue; }
    public void setNumberValue(BigDecimal numberValue) { this.numberValue = numberValue; }

    public Boolean getBooleanValue() { return booleanValue; }
    public void setBooleanValue(Boolean booleanValue) { this.booleanValue = booleanValue; }

    public String getMultiSelectValue() { return multiSelectValue; }
    public void setMultiSelectValue(String multiSelectValue) { this.multiSelectValue = multiSelectValue; }

    public Date getDateValue() { return dateValue; }
    public void setDateValue(Date dateValue) { this.dateValue = dateValue; }

    // Helper method to get the actual value based on attribute type
    public Object getValue() {
        return switch (attribute.getDataType()) {
            case TEXT, SINGLE_SELECT -> textValue;
            case NUMBER -> numberValue;
            case BOOLEAN -> booleanValue;
            case MULTI_SELECT -> multiSelectValue;
            case DATE -> dateValue;
        };
    }
}