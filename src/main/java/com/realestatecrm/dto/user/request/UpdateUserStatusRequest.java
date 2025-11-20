package com.realestatecrm.dto.user.request;

import com.realestatecrm.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull(message = "Status is required")
    UserStatus status
) {}
