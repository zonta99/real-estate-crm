package com.realestatecrm.dto.auth.response;

import java.util.Date;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        Date expiresAt
) {}