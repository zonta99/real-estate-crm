package com.realestatecrm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Created date entity class extending BaseEntity with automatic timestamp management
 * for creation date only (for immutable entities that don't need updatedDate tracking).
 * <p>
 * Entities extending this class will automatically have their createdDate set on
 * creation via JPA auditing.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class CreatedDateEntity extends BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
