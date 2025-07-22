package com.realestatecrm.service;

import com.realestatecrm.entity.User;
import com.realestatecrm.entity.UserHierarchy;
import com.realestatecrm.enums.Role;
import com.realestatecrm.repository.UserHierarchyRepository;
import com.realestatecrm.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserHierarchyRepository userHierarchyRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       UserHierarchyRepository userHierarchyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userHierarchyRepository = userHierarchyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ADDED: Missing method for role-based queries
    @Transactional(readOnly = true)
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRoleAndActiveStatus(role);
    }

    public User createUser(User user) {
        validateNewUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        validateUserUpdate(existingUser, updatedUser);

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setStatus(updatedUser.getStatus());

        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public UserHierarchy addSupervisorRelationship(Long supervisorId, Long subordinateId) {
        if (supervisorId.equals(subordinateId)) {
            throw new IllegalArgumentException("A user cannot supervise themselves");
        }

        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new EntityNotFoundException("Supervisor not found with id: " + supervisorId));
        User subordinate = userRepository.findById(subordinateId)
                .orElseThrow(() -> new EntityNotFoundException("Subordinate not found with id: " + subordinateId));

        validateHierarchyRelationship(supervisor, subordinate);

        if (userHierarchyRepository.existsBySupervisorIdAndSubordinateId(supervisorId, subordinateId)) {
            throw new IllegalArgumentException("Hierarchy relationship already exists");
        }

        // Check for circular reference - CORRECTED parameter order
        if (userHierarchyRepository.wouldCreateCycle(subordinateId, supervisorId)) {
            throw new IllegalArgumentException("Cannot create supervisor relationship: would create circular reference");
        }

        UserHierarchy hierarchy = new UserHierarchy(supervisor, subordinate);
        return userHierarchyRepository.save(hierarchy);
    }

    public void removeSupervisorRelationship(Long supervisorId, Long subordinateId) {
        if (!userHierarchyRepository.existsBySupervisorIdAndSubordinateId(supervisorId, subordinateId)) {
            throw new EntityNotFoundException("Hierarchy relationship not found");
        }
        userHierarchyRepository.deleteBySupervisorIdAndSubordinateId(supervisorId, subordinateId);
    }

    @Transactional(readOnly = true)
    public List<User> getDirectSubordinates(Long supervisorId) {
        return userRepository.findDirectSubordinates(supervisorId);
    }

    // ADDED: Missing method implementation
    @Transactional(readOnly = true)
    public List<User> getAllSubordinates(Long supervisorId) {
        return userRepository.findAllSubordinates(supervisorId);
    }

    @Transactional(readOnly = true)
    public List<User> getAccessibleUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getRole() == Role.ADMIN) {
            return userRepository.findAll();
        }

        List<User> accessibleUsers = new ArrayList<>();
        accessibleUsers.add(user); // Include self
        accessibleUsers.addAll(getAllSubordinates(userId)); // Add all subordinates
        return accessibleUsers;
    }

    // ADDED: Missing method for permission checking used in UserController
    @Transactional(readOnly = true)
    public boolean canManageUser(Long managerId, Long targetUserId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new EntityNotFoundException("Manager not found with id: " + managerId));

        if (manager.getRole() == Role.ADMIN) {
            return true;
        }

        if (manager.getRole() == Role.BROKER) {
            List<User> subordinates = getAllSubordinates(managerId);
            return subordinates.stream().anyMatch(user -> user.getId().equals(targetUserId));
        }

        return false;
    }
    
    

    private void validateNewUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
    }

    private void validateUserUpdate(User existingUser, User updatedUser) {
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + updatedUser.getUsername());
        }

        if (updatedUser.getEmail() != null &&
                !updatedUser.getEmail().equals(existingUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
        }
    }

    private void validateHierarchyRelationship(User supervisor, User subordinate) {
        // Basic role validation - could be expanded based on business rules
        if (supervisor.getRole() == Role.ASSISTANT) {
            throw new IllegalArgumentException("Assistants cannot supervise other users");
        }

        if (supervisor.getRole() == Role.AGENT && subordinate.getRole() == Role.BROKER) {
            throw new IllegalArgumentException("Agents cannot supervise brokers");
        }
    }
}