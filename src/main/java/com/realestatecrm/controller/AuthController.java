package com.realestatecrm.controller;

import com.realestatecrm.dto.auth.request.RefreshTokenRequest;
import com.realestatecrm.entity.Permission;
import com.realestatecrm.entity.RefreshToken;
import com.realestatecrm.entity.User;
import com.realestatecrm.mapper.UserInfoMapper;
import com.realestatecrm.security.JwtUtils;
import com.realestatecrm.service.CustomUserDetailsService;
import com.realestatecrm.service.RefreshTokenService;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;
    private final RefreshTokenService refreshTokenService;
    private final UserInfoMapper userInfoMapper;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private int jwtExpirationMs;

    @Autowired
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          PermissionService permissionService,
                          RefreshTokenService refreshTokenService,
                          UserInfoMapper userInfoMapper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.permissionService = permissionService;
        this.refreshTokenService = refreshTokenService;
        this.userInfoMapper = userInfoMapper;
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

        // SECURITY FIX: Create proper refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        UserInfo userInfo = userInfoMapper.toUserInfo(user);

        return ResponseEntity.ok(new LoginResponse(
                jwt,
                refreshToken.getToken(),  // Return refresh token
                userInfo,
                jwtExpirationMs / 1000
        ));
    }

    /**
     * SECURITY FIX: Proper refresh token mechanism with validation and rotation
     * Request body should contain: { "refreshToken": "token-string" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshTokenStr = request.refreshToken();

        if (refreshTokenStr == null || refreshTokenStr.isEmpty()) {
            throw new RuntimeException("Refresh token is required");
        }

        // Find and validate refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // Verify token is not expired or revoked
        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        // Rotate refresh token for security
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshTokenStr);

        // Generate new access token
        User user = refreshToken.getUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, List.of(() -> "ROLE_" + user.getRole().name()));

        String newAccessToken = jwtUtils.generateJwtToken(authentication);

        return ResponseEntity.ok(new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                new java.util.Date(System.currentTimeMillis() + jwtExpirationMs)
        ));
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfo> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserInfo userInfo = userInfoMapper.toUserInfo(user);

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
        List<UserInfo> subordinateInfos = userInfoMapper.toUserInfoList(subordinates);

        return ResponseEntity.ok(subordinateInfos);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal UserDetails userDetails) {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
    
}