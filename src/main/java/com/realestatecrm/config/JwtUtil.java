package com.realestatecrm.config;

import com.realestatecrm.service.CustomUserDetailsService.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private SecretKey jwtSecretKey;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpiration;

    @PostConstruct
    public void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate JWT token from authentication using modern API
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return buildToken(userPrincipal.getId(), userPrincipal.getUsername());
    }

    /**
     * Generate JWT token from user ID using modern API
     */
    public String generateTokenFromUserId(Long userId) {
        return buildToken(userId, null);
    }

    /**
     * Build JWT token with modern fluent API
     */
    private String buildToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtExpiration, ChronoUnit.MILLIS);

        JwtBuilder builder = Jwts.builder()
                .subject(Long.toString(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(jwtSecretKey, Jwts.SIG.HS256);

        // Add username as claim if available
        if (username != null) {
            builder.claim("username", username);
        }

        return builder.compact();
    }

    /**
     * Extract claim using modern parser API
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Get user ID from JWT token using modern API
     */
    public Long getUserIdFromToken(String token) {
        return Long.parseLong(extractClaim(token, Claims::getSubject));
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    /**
     * Extract all claims using modern parser API
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)  // Modern method
                .getPayload();
    }

    /**
     * Validate JWT token with enhanced error handling
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);  // Modern method
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            logJwtError(ex);
            return false;
        }
    }

    /**
     * Enhanced error logging
     */
    private void logJwtError(Exception ex) {
        switch (ex) {
            case SecurityException secEx ->
                    System.err.println("Invalid JWT signature: " + secEx.getMessage());
            case MalformedJwtException malEx ->
                    System.err.println("Invalid JWT token: " + malEx.getMessage());
            case ExpiredJwtException expEx ->
                    System.err.println("Expired JWT token: " + expEx.getMessage());
            case UnsupportedJwtException unsEx ->
                    System.err.println("Unsupported JWT token: " + unsEx.getMessage());
            case IllegalArgumentException illEx ->
                    System.err.println("JWT claims string is empty: " + illEx.getMessage());
            default ->
                    System.err.println("JWT processing error: " + ex.getMessage());
        }
    }

    /**
     * Get expiration date from token using modern API
     */
    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Get remaining time until token expires
     */
    public long getTimeUntilExpiration(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.getTime() - System.currentTimeMillis();
    }

    /**
     * Check if token will expire within specified minutes
     */
    public boolean willExpireSoon(String token, int minutes) {
        long timeUntilExpiration = getTimeUntilExpiration(token);
        long threshold = minutes * 60 * 1000L; // Convert minutes to milliseconds
        return timeUntilExpiration < threshold;
    }
}