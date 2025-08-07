package com.realestatecrm.dto.user.response;

import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        Role role,
        UserStatus status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {}