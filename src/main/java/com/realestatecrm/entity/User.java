package com.realestatecrm.entity;

import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User extends AuditableEntity implements UserDetails {

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // Add these missing methods:
    @Override
    public boolean isAccountNonExpired() { return status == UserStatus.ACTIVE; }

    @Override
    public boolean isAccountNonLocked() { return status == UserStatus.ACTIVE; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == UserStatus.ACTIVE; }
}