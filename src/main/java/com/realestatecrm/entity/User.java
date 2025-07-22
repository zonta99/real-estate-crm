package com.realestatecrm.entity;

import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime updatedDate;

    // Relationships
    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties;

    @OneToMany(mappedBy = "agent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Customer> customers;

    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHierarchy> subordinates;

    @OneToMany(mappedBy = "subordinate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserHierarchy> supervisors;

    @OneToMany(mappedBy = "sharedByUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertySharing> sharedProperties;

    @OneToMany(mappedBy = "sharedWithUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PropertySharing> receivedProperties;

    // Constructors
    public User() {}

    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }

    public List<Property> getProperties() { return properties; }
    public void setProperties(List<Property> properties) { this.properties = properties; }

    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) { this.customers = customers; }

    public List<UserHierarchy> getSubordinates() { return subordinates; }
    public void setSubordinates(List<UserHierarchy> subordinates) { this.subordinates = subordinates; }

    public List<UserHierarchy> getSupervisors() { return supervisors; }
    public void setSupervisors(List<UserHierarchy> supervisors) { this.supervisors = supervisors; }

    public List<PropertySharing> getSharedProperties() { return sharedProperties; }
    public void setSharedProperties(List<PropertySharing> sharedProperties) { this.sharedProperties = sharedProperties; }

    public List<PropertySharing> getReceivedProperties() { return receivedProperties; }
    public void setReceivedProperties(List<PropertySharing> receivedProperties) { this.receivedProperties = receivedProperties; }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return username;
    }
}