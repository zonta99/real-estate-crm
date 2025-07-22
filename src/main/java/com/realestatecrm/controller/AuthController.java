package com.realestatecrm.controller;

import com.realestatecrm.entity.User;
import com.realestatecrm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
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
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = userService.getUserByUsername(userDetails.getUsername())
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

    @GetMapping("/user-info")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getRole().name(),
                user.getStatus().name(),
                true // authenticated
        ));
    }

    // Enhanced DTOs
    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            @Email @NotBlank String email
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

    public record UserInfoResponse(
            Long id,
            String username,
            String role,
            String status,
            boolean authenticated
    ) {}

    public record MessageResponse(String message) {}
}