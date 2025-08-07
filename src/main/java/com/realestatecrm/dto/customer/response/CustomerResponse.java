package com.realestatecrm.dto.customer.response;

import com.realestatecrm.enums.CustomerStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String notes;
    private String leadSource;
    private CustomerStatus status;
    private Long agentId;
    private String agentName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public CustomerResponse(Long id, String firstName, String lastName, String phone, String email,
                            BigDecimal budgetMin, BigDecimal budgetMax, String notes, String leadSource,
                            CustomerStatus status, Long agentId, String agentName,
                            LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.budgetMin = budgetMin;
        this.budgetMax = budgetMax;
        this.notes = notes;
        this.leadSource = leadSource;
        this.status = status;
        this.agentId = agentId;
        this.agentName = agentName;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public BigDecimal getBudgetMin() { return budgetMin; }
    public BigDecimal getBudgetMax() { return budgetMax; }
    public String getNotes() { return notes; }
    public String getLeadSource() { return leadSource; }
    public CustomerStatus getStatus() { return status; }
    public Long getAgentId() { return agentId; }
    public String getAgentName() { return agentName; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
}