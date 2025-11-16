package com.realestatecrm.service;

import com.realestatecrm.entity.RefreshToken;
import com.realestatecrm.entity.User;
import com.realestatecrm.repository.RefreshTokenRepository;
import com.realestatecrm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new refresh token for the user
     * Revokes any existing refresh tokens for the user
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete any existing refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setRevoked(false);

        refreshToken = refreshTokenRepository.save(refreshToken);
        logger.info("Created refresh token for user: {}", user.getUsername());

        return refreshToken;
    }

    /**
     * Find refresh token by token string
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify refresh token is valid (not expired and not revoked)
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please sign in again.");
        }

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked. Please sign in again.");
        }

        return token;
    }

    /**
     * Revoke a refresh token (logout)
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            logger.info("Revoked refresh token for user: {}", rt.getUser().getUsername());
        });
    }

    /**
     * Revoke all refresh tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.deleteByUser(user);
        logger.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }

    /**
     * Cleanup expired tokens (should be run periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        logger.info("Cleaned up expired refresh tokens");
    }

    /**
     * Rotate refresh token - create new one and revoke old one
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken) {
        RefreshToken existingToken = findByToken(oldToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        verifyExpiration(existingToken);

        // Revoke old token
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        // Create new token
        return createRefreshToken(existingToken.getUser().getId());
    }
}
