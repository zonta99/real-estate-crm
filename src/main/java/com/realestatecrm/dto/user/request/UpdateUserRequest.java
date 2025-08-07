package com.realestatecrm.dto.user.request;

import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        @Email @NotBlank String email,
        @NotNull Role role,
        @NotNull UserStatus status
) {}