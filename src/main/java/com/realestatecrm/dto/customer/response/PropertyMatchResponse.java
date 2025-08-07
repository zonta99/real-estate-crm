package com.realestatecrm.dto.customer.response;

import java.math.BigDecimal;

public class PropertyMatchResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String agentName;
    private String status;

    public PropertyMatchResponse(Long id, String title, String description, BigDecimal price,
                                 String agentName, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.agentName = agentName;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getAgentName() { return agentName; }
    public String getStatus() { return status; }
}