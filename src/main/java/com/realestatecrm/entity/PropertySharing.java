package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_sharing",
        uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "shared_with_user_id"}))
@EntityListeners(AuditingEntityListener.class)
public class PropertySharing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Constructors
    public PropertySharing() {}

    public PropertySharing(Property property, User sharedWithUser, User sharedByUser) {
        this.property = property;
        this.sharedWithUser = sharedWithUser;
        this.sharedByUser = sharedByUser;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public User getSharedWithUser() { return sharedWithUser; }
    public void setSharedWithUser(User sharedWithUser) { this.sharedWithUser = sharedWithUser; }

    public User getSharedByUser() { return sharedByUser; }
    public void setSharedByUser(User sharedByUser) { this.sharedByUser = sharedByUser; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertySharing that)) return false;
        return property != null ? property.equals(that.property) : that.property == null &&
                sharedWithUser != null ? sharedWithUser.equals(that.sharedWithUser) : that.sharedWithUser == null;
    }

    @Override
    public int hashCode() {
        return 31 * (property != null ? property.hashCode() : 0) +
                (sharedWithUser != null ? sharedWithUser.hashCode() : 0);
    }
}