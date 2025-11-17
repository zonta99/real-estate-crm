package com.realestatecrm.dto.customer.request;

import jakarta.validation.constraints.NotBlank;

public class CreateCustomerNoteRequest {

    @NotBlank(message = "Note content is required")
    private String content;

    // Constructors
    public CreateCustomerNoteRequest() {}

    public CreateCustomerNoteRequest(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
