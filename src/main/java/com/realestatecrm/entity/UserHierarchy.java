package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_hierarchy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"supervisor_id", "subordinate_id"}))
public class UserHierarchy extends CreatedDateEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subordinate_id", nullable = false)
    private User subordinate;

    // Constructors
    public UserHierarchy() {}

    public UserHierarchy(User supervisor, User subordinate) {
        this.supervisor = supervisor;
        this.subordinate = subordinate;
    }

    // Getters and Setters
    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public User getSubordinate() { return subordinate; }
    public void setSubordinate(User subordinate) { this.subordinate = subordinate; }
}