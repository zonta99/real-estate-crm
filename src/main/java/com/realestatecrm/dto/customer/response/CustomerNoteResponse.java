package com.realestatecrm.dto.customer.response;

import java.time.LocalDateTime;

public class CustomerNoteResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long createdByUserId;
    private String createdByUserName;
    private String content;
    private LocalDateTime createdDate;

    // Constructors
    public CustomerNoteResponse() {}

    public CustomerNoteResponse(Long id, Long customerId, String customerName,
                                Long createdByUserId, String createdByUserName,
                                String content, LocalDateTime createdDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.createdByUserId = createdByUserId;
        this.createdByUserName = createdByUserName;
        this.content = content;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
