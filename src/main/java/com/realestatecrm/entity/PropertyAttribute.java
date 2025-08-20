package com.realestatecrm.entity;

import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "property_attributes")
@EntityListeners(AuditingEntityListener.class)
public class PropertyAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private PropertyDataType dataType;

    @NotNull
    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @NotNull
    @Column(name = "is_searchable", nullable = false)
    private Boolean isSearchable = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyCategory category;

    @Column(name = "display_order")
    private Integer displayOrder;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Relationships
    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<PropertyAttributeOption> options;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AttributeValue> attributeValues;

    @OneToMany(mappedBy = "attribute", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CustomerSearchCriteria> customerSearchCriteria;

    // Constructors
    public PropertyAttribute() {}

    public PropertyAttribute(String name, PropertyDataType dataType, PropertyCategory category) {
        this.name = name;
        this.dataType = dataType;
        this.category = category;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public PropertyDataType getDataType() { return dataType; }
    public void setDataType(PropertyDataType dataType) { this.dataType = dataType; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public Boolean getIsSearchable() { return isSearchable; }
    public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }

    public PropertyCategory getCategory() { return category; }
    public void setCategory(PropertyCategory category) { this.category = category; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public List<PropertyAttributeOption> getOptions() { return options; }
    public void setOptions(List<PropertyAttributeOption> options) { this.options = options; }

    public List<AttributeValue> getAttributeValues() { return attributeValues; }
    public void setAttributeValues(List<AttributeValue> attributeValues) { this.attributeValues = attributeValues; }

    public List<CustomerSearchCriteria> getCustomerSearchCriteria() { return customerSearchCriteria; }
    public void setCustomerSearchCriteria(List<CustomerSearchCriteria> customerSearchCriteria) {
        this.customerSearchCriteria = customerSearchCriteria;
    }

    public boolean requiresOptions() {
        return dataType == PropertyDataType.SINGLE_SELECT || dataType == PropertyDataType.MULTI_SELECT;
    }
}