package com.realestatecrm.entity;

import com.realestatecrm.enums.PropertyStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "properties")
public class Property extends AuditableEntity {

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status = PropertyStatus.ACTIVE;

    // Relationships
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AttributeValue> attributeValues;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PropertySharing> propertySharing;

    // Constructors
    public Property() {}

    public Property(String title, BigDecimal price, User agent) {
        this.title = title;
        this.price = price;
        this.agent = agent;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }

    public PropertyStatus getStatus() { return status; }
    public void setStatus(PropertyStatus status) { this.status = status; }

    public List<AttributeValue> getAttributeValues() { return attributeValues; }
    public void setAttributeValues(List<AttributeValue> attributeValues) { this.attributeValues = attributeValues; }

    public List<PropertySharing> getPropertySharing() { return propertySharing; }
    public void setPropertySharing(List<PropertySharing> propertySharing) { this.propertySharing = propertySharing; }
}