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
public interface UserRepository extends JpaRepository<User, Long> {

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

    // MISSING METHOD 1: Recursive subordinate finding
    @Query("""
        WITH RECURSIVE subordinate_hierarchy AS (
            SELECT uh.subordinate_id, uh.supervisor_id, 1 as level
            FROM user_hierarchy uh 
            WHERE uh.supervisor_id = :supervisorId
            
            UNION ALL
            
            SELECT uh.subordinate_id, uh.supervisor_id, sh.level + 1
            FROM user_hierarchy uh
            INNER JOIN subordinate_hierarchy sh ON uh.supervisor_id = sh.subordinate_id
            WHERE sh.level < 10
        )
        SELECT u FROM User u 
        WHERE u.id IN (SELECT DISTINCT subordinate_id FROM subordinate_hierarchy)
        """)
    List<User> findAllSubordinates(@Param("supervisorId") Long supervisorId);

    // MISSING METHOD 2: Role-based query with active status
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = 'ACTIVE' ORDER BY u.firstName, u.lastName")
    List<User> findByRoleAndActiveStatus(@Param("role") Role role);
}