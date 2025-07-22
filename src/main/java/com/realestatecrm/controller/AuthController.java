package com.realestatecrm.controller;

import com.realestatecrm.config.JwtUtil;
import com.realestatecrm.entity.User;
import com.realestatecrm.service.CustomUserDetailsService.UserPrincipal;
import com.realestatecrm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    "Bearer",
                    userPrincipal.getId(),
                    userPrincipal.getUsername(),
                    userPrincipal.getEmail(),
                    userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""),
                    jwtUtil.getExpirationDateFromToken(jwt).toInstant()
            ));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.badRequest()
                    .body(new JwtResponse("Invalid credentials", null, null, null, null, null, null));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @RequestHeader("Authorization") String authHeader) {

        String currentToken = authHeader.substring(7); // Remove "Bearer " prefix

        // Check if token will expire soon (within 30 minutes)
        if (!jwtUtil.willExpireSoon(currentToken, 30)) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Token refresh not needed yet"));
        }

        String newToken = jwtUtil.generateTokenFromUserId(userPrincipal.getId());

        return ResponseEntity.ok(new JwtResponse(
                newToken,
                "Bearer",
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                userPrincipal.getEmail(),
                userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""),
                jwtUtil.getExpirationDateFromToken(newToken).toInstant()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.getUserById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedDate()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = userService.getUserById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        User updatedUser = userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getRole().name(),
                updatedUser.getStatus().name(),
                updatedUser.getCreatedDate()
        ));
    }

    @GetMapping("/token-info")
    public ResponseEntity<TokenInfoResponse> getTokenInfo(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest()
                    .body(new TokenInfoResponse(false, null, null, 0));
        }

        return ResponseEntity.ok(new TokenInfoResponse(
                true,
                jwtUtil.getExpirationDateFromToken(token).toInstant(),
                jwtUtil.isTokenExpired(token),
                jwtUtil.getTimeUntilExpiration(token)
        ));
    }

    // Enhanced DTOs with modern Java features
    public record LoginRequest(
            @NotBlank @Size(min = 3, max = 20) String username,
            @NotBlank @Size(min = 6, max = 40) String password
    ) {}

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            @Email @NotBlank String email
    ) {}

    public record JwtResponse(
            String accessToken,
            String tokenType,
            Long id,
            String username,
            String email,
            String role,
            java.time.Instant expiresAt
    ) {}

    public record UserProfileResponse(
            Long id,
            String username,
            String email,
            String firstName,
            String lastName,
            String role,
            String status,
            LocalDateTime createdDate
    ) {}

    public record TokenInfoResponse(
            boolean valid,
            java.time.Instant expiresAt,
            Boolean expired,
            long timeUntilExpirationMs
    ) {}

    public record MessageResponse(String message) {}
}