package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "property_sharing",
        uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "shared_with_user_id"}))
public class PropertySharing extends CreatedDateEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedByUser;

    // Constructors
    public PropertySharing() {}

    public PropertySharing(Property property, User sharedWithUser, User sharedByUser) {
        this.property = property;
        this.sharedWithUser = sharedWithUser;
        this.sharedByUser = sharedByUser;
    }

    // Getters and Setters
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public User getSharedWithUser() { return sharedWithUser; }
    public void setSharedWithUser(User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public User getSharedByUser() { return sharedByUser; }
    public void setSharedByUser(User sharedByUser) { this.sharedByUser = sharedByUser; }
}