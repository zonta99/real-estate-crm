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
}