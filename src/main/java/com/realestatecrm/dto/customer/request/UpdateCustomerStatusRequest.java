package com.realestatecrm.dto.customer.request;

import com.realestatecrm.enums.CustomerStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(
    @NotNull(message = "Status is required")
    CustomerStatus status
) {}
