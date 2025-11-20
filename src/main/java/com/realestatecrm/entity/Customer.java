package com.realestatecrm.entity;

import com.realestatecrm.enums.CustomerStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "customers")
public class Customer extends AuditableEntity {

    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NotBlank
    @Column(nullable = false)
    private String phone;

    @Email
    @Column(unique = true)
    private String email;

    @Column(name = "budget_min", precision = 15, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", precision = 15, scale = 2)
    private BigDecimal budgetMax;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "lead_source")
    private String leadSource;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.LEAD;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    // Relationships
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SavedSearch> savedSearches;

    // Constructors
    public Customer() {}

    public Customer(String firstName, String lastName, String phone, User agent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.agent = agent;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public BigDecimal getBudgetMin() { return budgetMin; }
    public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }

    public BigDecimal getBudgetMax() { return budgetMax; }
    public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLeadSource() { return leadSource; }
    public void setLeadSource(String leadSource) { this.leadSource = leadSource; }

    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }

    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }

    public List<SavedSearch> getSavedSearches() { return savedSearches; }
    public void setSavedSearches(List<SavedSearch> savedSearches) { this.savedSearches = savedSearches; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean hasBudgetRange() {
        return budgetMin != null || budgetMax != null;
    }
}