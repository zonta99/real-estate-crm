package com.realestatecrm.dto.user.request;

import com.realestatecrm.enums.Role;
import com.realestatecrm.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 3, max = 20) String username,
        @NotBlank @ValidPassword(minLength = 8) String password,  // SECURITY: Strong password validation
        @Email @NotBlank String email,
        String firstName,
        String lastName,
        @NotNull Role role
) {}