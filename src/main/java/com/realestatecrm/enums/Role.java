package com.realestatecrm.enums;

public enum Role {
    ADMIN("Administrator with full system access"),
    BROKER("Broker managing multiple agents"),
    AGENT("Real estate agent managing properties and customers"),
    ASSISTANT("Assistant with view-only access");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}