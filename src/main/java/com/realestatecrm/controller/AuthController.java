package com.realestatecrm.controller;

import com.realestatecrm.entity.Permission;
import com.realestatecrm.entity.User;
import com.realestatecrm.security.JwtUtils;
import com.realestatecrm.service.CustomUserDetailsService;
import com.realestatecrm.service.UserService;
import com.realestatecrm.service.PermissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.realestatecrm.dto.auth.request.LoginRequest;
import com.realestatecrm.dto.auth.response.*;
import com.realestatecrm.dto.common.MessageResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private int jwtExpirationMs;

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          PermissionService permissionService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.permissionService = permissionService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetailsService.UserPrincipal userDetails =
                (CustomUserDetailsService.UserPrincipal) authentication.getPrincipal();

        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = new UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                List.of("ROLE_" + user.getRole().name()),
                user.getStatus().name(),
                user.getCreatedDate().toString(),
                user.getUpdatedDate().toString()
        );

        return ResponseEntity.ok(new LoginResponse(
                jwt,
                userInfo,
                jwtExpirationMs / 1000
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String oldToken = authorizationHeader.substring(7);

        if (!jwtUtils.validateJwtToken(oldToken)) {
            throw new RuntimeException("Invalid token");
        }

        String username = jwtUtils.getUsernameFromJwtToken(oldToken);
        User user = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, List.of(() -> "ROLE_" + user.getRole().name()));

        String newToken = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new RefreshTokenResponse(
                newToken,
                null,
                new java.util.Date(System.currentTimeMillis() + jwtExpirationMs)
        ));
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = new UserInfo(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                List.of("ROLE_" + user.getRole().name()),
                user.getStatus().name(),
                user.getCreatedDate().toString(),
                user.getUpdatedDate().toString()
        );

        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getUserPermissions(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Permission> permissions = permissionService.getUserPermissions(user);
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/subordinates")
    public ResponseEntity<List<UserInfo>> getSubordinates(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> subordinates = userService.getDirectSubordinates(user.getId());
        List<UserInfo> subordinateInfos = subordinates.stream()
                .map(sub -> new UserInfo(
                        sub.getId().toString(),
                        sub.getUsername(),
                        sub.getEmail(),
                        sub.getFirstName(),
                        sub.getLastName(),
                        List.of("ROLE_" + sub.getRole().name()),
                        sub.getStatus().name(),
                        sub.getCreatedDate().toString(),
                        sub.getUpdatedDate().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(subordinateInfos);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
    
}