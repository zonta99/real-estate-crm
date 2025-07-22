package com.realestatecrm.controller;

import com.realestatecrm.entity.User;
import com.realestatecrm.entity.UserHierarchy;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import com.realestatecrm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @AuthenticationPrincipal String username,
            Pageable pageable) {

        User currentUser = userService.getUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> accessibleUsers = userService.getAccessibleUsers(currentUser.getId());
        List<UserResponse> userResponses = accessibleUsers.stream()
                .map(this::convertToUserResponse)
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return ResponseEntity.ok(convertToUserResponse(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);

        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToUserResponse(createdUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('BROKER') and @userService.canManageUser(authentication.principal.id, #id))")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setStatus(request.getStatus());

        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(convertToUserResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    @PostMapping("/{id}/hierarchy")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<MessageResponse> addSupervisorRelationship(
            @PathVariable Long id,
            @Valid @RequestBody HierarchyRequest request) {

        userService.addSupervisorRelationship(request.getSupervisorId(), id);
        return ResponseEntity.ok(new MessageResponse("Supervisor relationship added successfully"));
    }

    @DeleteMapping("/{id}/hierarchy/{supervisorId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<MessageResponse> removeSupervisorRelationship(
            @PathVariable Long id,
            @PathVariable Long supervisorId) {

        userService.removeSupervisorRelationship(supervisorId, id);
        return ResponseEntity.ok(new MessageResponse("Supervisor relationship removed successfully"));
    }

    @GetMapping("/{id}/subordinates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER') or #id == authentication.principal.id")
    public ResponseEntity<List<UserResponse>> getSubordinates(@PathVariable Long id) {
        List<User> subordinates = userService.getDirectSubordinates(id);
        List<UserResponse> responses = subordinates.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/roles/{role}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('BROKER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        List<UserResponse> responses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedDate(),
                user.getUpdatedDate()
        );
    }

    // DTOs
    public static class CreateUserRequest {
        @NotBlank
        @Size(min = 3, max = 20)
        private String username;

        @NotBlank
        @Size(min = 6, max = 40)
        private String password;

        @Email
        @NotBlank
        private String email;

        private String firstName;
        private String lastName;

        @NotNull
        private Role role;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
    }

    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;

        @Email
        @NotBlank
        private String email;

        @NotNull
        private Role role;

        @NotNull
        private UserStatus status;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public UserStatus getStatus() { return status; }
        public void setStatus(UserStatus status) { this.status = status; }
    }

    public static class HierarchyRequest {
        @NotNull
        private Long supervisorId;

        public Long getSupervisorId() { return supervisorId; }
        public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private Role role;
        private UserStatus status;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public UserResponse(Long id, String username, String email, String firstName, String lastName,
                            Role role, UserStatus status, LocalDateTime createdDate, LocalDateTime updatedDate) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.status = status;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public Role getRole() { return role; }
        public UserStatus getStatus() { return status; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}