package com.realestatecrm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_hierarchy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"supervisor_id", "subordinate_id"}))
@EntityListeners(AuditingEntityListener.class)
public class UserHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subordinate_id", nullable = false)
    private User subordinate;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Constructors
    public UserHierarchy() {}

    public UserHierarchy(User supervisor, User subordinate) {
        this.supervisor = supervisor;
        this.subordinate = subordinate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public User getSubordinate() { return subordinate; }
    public void setSubordinate(User subordinate) { this.subordinate = subordinate; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserHierarchy that)) return false;
        return supervisor != null ? supervisor.equals(that.supervisor) : that.supervisor == null &&
                subordinate != null ? subordinate.equals(that.subordinate) : that.subordinate == null;
    }

    @Override
    public int hashCode() {
        return 31 * (supervisor != null ? supervisor.hashCode() : 0) +
                (subordinate != null ? subordinate.hashCode() : 0);
    }
}