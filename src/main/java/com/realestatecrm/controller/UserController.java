package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.user.request.CreateUserRequest;
import com.realestatecrm.dto.user.request.HierarchyRequest;
import com.realestatecrm.dto.user.request.UpdateUserRequest;
import com.realestatecrm.dto.user.request.UpdateUserPasswordRequest;
import com.realestatecrm.dto.user.request.UpdateUserStatusRequest;
import com.realestatecrm.dto.user.response.UserResponse;
import com.realestatecrm.entity.User;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import com.realestatecrm.service.UserService;
import com.realestatecrm.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> accessibleUsers = userService.getAccessibleUsers(currentUser.getId());
        List<UserResponse> userResponses = accessibleUsers.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        // Simple pagination implementation
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userResponses.size());
        Page<UserResponse> page = new PageImpl<>(
                userResponses.subList(start, end),
                pageable,
                userResponses.size()
        );

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER') or @userService.getUserByUsername(authentication.name).get().id == #id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<List<UserResponse>> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status) {

        List<User> users;
        if (username != null) {
            User user = userService.getUserByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            users = List.of(user);
        } else if (role != null) {
            users = userService.getUsersByRole(role);
        } else if (status != null) {
            users = userService.getUsersByStatus(status);
        } else {
            throw new IllegalArgumentException("At least one search parameter is required");
        }

        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(request.password());
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(createdUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BROKER') and @userService.canManageUser(authentication.principal.id, #id))")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setRole(request.role());
        user.setStatus(request.status());

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(userMapper.toResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        User user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserByUsername(authentication.name).get().id == #id")
    public ResponseEntity<MessageResponse> updateUserPassword(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPasswordRequest request) {
        userService.updateUserPassword(id, request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }

    @PostMapping("/{id}/supervisors/{supervisorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<MessageResponse> addSupervisorRelationship(
            @PathVariable Long id,
            @PathVariable Long supervisorId) {

        userService.addSupervisorRelationship(supervisorId, id);
        return ResponseEntity.ok(new MessageResponse("Supervisor relationship added successfully"));
    }

    @DeleteMapping("/{id}/supervisors/{supervisorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<MessageResponse> removeSupervisorRelationship(
            @PathVariable Long id,
            @PathVariable Long supervisorId) {

        userService.removeSupervisorRelationship(supervisorId, id);
        return ResponseEntity.ok(new MessageResponse("Supervisor relationship removed successfully"));
    }

    @GetMapping("/{id}/subordinates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER') or @userService.getUserByUsername(authentication.name).get().id == #id")
    public ResponseEntity<List<UserResponse>> getSubordinates(@PathVariable Long id) {
        List<User> subordinates = userService.getDirectSubordinates(id);
        List<UserResponse> responses = subordinates.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}/supervisors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER') or @userService.getUserByUsername(authentication.name).get().id == #id")
    public ResponseEntity<List<UserResponse>> getSupervisors(@PathVariable Long id) {
        List<User> supervisors = userService.getDirectSupervisors(id);
        List<UserResponse> responses = supervisors.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/roles/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}