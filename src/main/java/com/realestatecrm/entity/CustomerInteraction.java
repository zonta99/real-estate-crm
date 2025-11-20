package com.realestatecrm.entity;

import com.realestatecrm.enums.InteractionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_interactions")
public class CustomerInteraction extends CreatedDateEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    @NotBlank
    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @Column(name = "interaction_date", nullable = false)
    private LocalDateTime interactionDate;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_property_id")
    private Property relatedProperty;

    // Constructors
    public CustomerInteraction() {}

    public CustomerInteraction(Customer customer, User user, InteractionType type,
                               String subject, LocalDateTime interactionDate) {
        this.customer = customer;
        this.user = user;
        this.type = type;
        this.subject = subject;
        this.interactionDate = interactionDate;
    }

    // Getters and Setters
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public InteractionType getType() {
        return type;
    }

    public void setType(InteractionType type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getInteractionDate() {
        return interactionDate;
    }

    public void setInteractionDate(LocalDateTime interactionDate) {
        this.interactionDate = interactionDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Property getRelatedProperty() {
        return relatedProperty;
    }

    public void setRelatedProperty(Property relatedProperty) {
        this.relatedProperty = relatedProperty;
    }
}
