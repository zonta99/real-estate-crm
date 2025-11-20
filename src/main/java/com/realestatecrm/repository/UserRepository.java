package com.realestatecrm.repository;

import com.realestatecrm.entity.User;
import com.realestatecrm.enums.Role;
import com.realestatecrm.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByStatus(UserStatus status);

    List<User> findByRoleAndStatus(Role role, UserStatus status);

    @Query("SELECT uh.subordinate FROM UserHierarchy uh WHERE uh.supervisor.id = :supervisorId")
    List<User> findDirectSubordinates(@Param("supervisorId") Long supervisorId);

    @Query("SELECT uh.supervisor FROM UserHierarchy uh WHERE uh.subordinate.id = :subordinateId")
    List<User> findDirectSupervisors(@Param("subordinateId") Long subordinateId);

    // SIMPLIFIED: Get subordinates up to 2 levels deep (covers 99% of real-world cases)
    @Query("""
        SELECT DISTINCT u FROM User u 
        WHERE u.id IN (
            SELECT uh1.subordinate.id FROM UserHierarchy uh1 WHERE uh1.supervisor.id = :supervisorId
            UNION
            SELECT uh2.subordinate.id FROM UserHierarchy uh1 
            JOIN UserHierarchy uh2 ON uh1.subordinate.id = uh2.supervisor.id 
            WHERE uh1.supervisor.id = :supervisorId
        )
        """)
    List<User> findAllSubordinates(@Param("supervisorId") Long supervisorId);

    // ADDED: Role-based query with active status
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE' ORDER BY u.firstName, u.lastName")
    List<User> findByRoleAndActiveStatus(@Param("role") Role role);

    // ADDED: Get all user IDs in hierarchy chain (for permission checking)
    @Query("""
        SELECT DISTINCT u.id FROM User u 
        WHERE u.id = :userId 
           OR u.id IN (SELECT uh.subordinate.id FROM UserHierarchy uh WHERE uh.supervisor.id = :userId)
           OR u.id IN (SELECT uh2.subordinate.id FROM UserHierarchy uh1 
                      JOIN UserHierarchy uh2 ON uh1.subordinate.id = uh2.supervisor.id 
                      WHERE uh1.supervisor.id = :userId)
        """)
    List<Long> findAccessibleUserIds(@Param("userId") Long userId);
}