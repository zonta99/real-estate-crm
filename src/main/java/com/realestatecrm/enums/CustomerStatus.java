package com.realestatecrm.enums;

/**
 * Customer lifecycle status in real estate CRM
 */
public enum CustomerStatus {
    LEAD,           // Initial contact/prospect
    QUALIFIED,      // Qualified lead ready for engagement
    NEGOTIATING,    // In active negotiation
    SOLD,           // Successfully closed deal
    LOST            // Lost opportunity
}