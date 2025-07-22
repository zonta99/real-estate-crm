package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "property_attribute_options",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attribute_id", "option_value"}))
public class PropertyAttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private PropertyAttribute attribute;

    @NotBlank
    @Column(name = "option_value", nullable = false)
    private String optionValue;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Constructors
    public PropertyAttributeOption() {}

    public PropertyAttributeOption(PropertyAttribute attribute, String optionValue) {
        this.attribute = attribute;
        this.optionValue = optionValue;
    }

    public PropertyAttributeOption(PropertyAttribute attribute, String optionValue, Integer displayOrder) {
        this.attribute = attribute;
        this.optionValue = optionValue;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PropertyAttribute getAttribute() { return attribute; }
    public void setAttribute(PropertyAttribute attribute) { this.attribute = attribute; }

    public String getOptionValue() { return optionValue; }
    public void setOptionValue(String optionValue) { this.optionValue = optionValue; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyAttributeOption that)) return false;
        return attribute != null ? attribute.equals(that.attribute) : that.attribute == null &&
                optionValue != null ? optionValue.equals(that.optionValue) : that.optionValue == null;
    }

    @Override
    public int hashCode() {
        return 31 * (attribute != null ? attribute.hashCode() : 0) +
                (optionValue != null ? optionValue.hashCode() : 0);
    }
}