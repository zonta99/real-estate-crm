package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "customer_search_criteria",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "attribute_id"}))
public class CustomerSearchCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private PropertyAttribute attribute;

    // Typed value columns - mirrors PropertyValue structure
    @Column(name = "text_value")
    private String textValue;

    @Column(name = "number_min_value", precision = 15, scale = 2)
    private BigDecimal numberMinValue;

    @Column(name = "number_max_value", precision = 15, scale = 2)
    private BigDecimal numberMaxValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "multi_select_value", columnDefinition = "TEXT")
    private String multiSelectValue; // JSON array as string

    // Constructors
    public CustomerSearchCriteria() {}

    public CustomerSearchCriteria(Customer customer, PropertyAttribute attribute) {
        this.customer = customer;
        this.attribute = attribute;
    }

    // Static factory methods for type-safe criteria creation
    public static CustomerSearchCriteria createTextCriteria(Customer customer, PropertyAttribute attribute, String value) {
        CustomerSearchCriteria criteria = new CustomerSearchCriteria(customer, attribute);
        criteria.setTextValue(value);
        return criteria;
    }

    public static CustomerSearchCriteria createNumberRangeCriteria(Customer customer, PropertyAttribute attribute,
                                                                   BigDecimal minValue, BigDecimal maxValue) {
        CustomerSearchCriteria criteria = new CustomerSearchCriteria(customer, attribute);
        criteria.setNumberMinValue(minValue);
        criteria.setNumberMaxValue(maxValue);
        return criteria;
    }

    public static CustomerSearchCriteria createBooleanCriteria(Customer customer, PropertyAttribute attribute, Boolean value) {
        CustomerSearchCriteria criteria = new CustomerSearchCriteria(customer, attribute);
        criteria.setBooleanValue(value);
        return criteria;
    }

    public static CustomerSearchCriteria createMultiSelectCriteria(Customer customer, PropertyAttribute attribute, String jsonValue) {
        CustomerSearchCriteria criteria = new CustomerSearchCriteria(customer, attribute);
        criteria.setMultiSelectValue(jsonValue);
        return criteria;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public PropertyAttribute getAttribute() { return attribute; }
    public void setAttribute(PropertyAttribute attribute) { this.attribute = attribute; }

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

    // Helper method to check if number range is specified
    public boolean hasNumberRange() {
        return numberMinValue != null || numberMaxValue != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerSearchCriteria that)) return false;
        return customer != null ? customer.equals(that.customer) : that.customer == null &&
                attribute != null ? attribute.equals(that.attribute) : that.attribute == null;
    }

    @Override
    public int hashCode() {
        return 31 * (customer != null ? customer.hashCode() : 0) +
                (attribute != null ? attribute.hashCode() : 0);
    }
}