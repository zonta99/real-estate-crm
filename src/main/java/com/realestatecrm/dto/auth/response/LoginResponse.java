package com.realestatecrm.dto.auth.response;

public record LoginResponse(
        String token,
        String refreshToken,  // SECURITY FIX: Include refresh token
        UserInfo user,
        int expiresIn
) {}