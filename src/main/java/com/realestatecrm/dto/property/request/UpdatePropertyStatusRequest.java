package com.realestatecrm.dto.property.request;

import com.realestatecrm.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePropertyStatusRequest(
    @NotNull(message = "Status is required")
    PropertyStatus status
) {}
