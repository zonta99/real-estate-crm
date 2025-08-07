package com.realestatecrm.dto.auth.response;

public record LoginResponse(
        String token,
        UserInfo user,
        int expiresIn
) {}