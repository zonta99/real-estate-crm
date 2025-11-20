package com.realestatecrm.dto.user.request;

import com.realestatecrm.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordRequest(
    @NotBlank(message = "New password is required")
    @ValidPassword
    String newPassword
) {}
