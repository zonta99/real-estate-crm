package com.realestatecrm.dto.auth.response;

import java.util.List;

public record UserInfo(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        String status,
        String createdDate,
        String updatedDate
) {}