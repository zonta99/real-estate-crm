package com.realestatecrm.repository;

import com.realestatecrm.entity.UserHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHierarchyRepository extends JpaRepository<UserHierarchy, Long> {

    Optional<UserHierarchy> findBySupervisorIdAndSubordinateId(Long supervisorId, Long subordinateId);

    List<UserHierarchy> findBySupervisorId(Long supervisorId);

    List<UserHierarchy> findBySubordinateId(Long subordinateId);

    boolean existsBySupervisorIdAndSubordinateId(Long supervisorId, Long subordinateId);

    void deleteBySupervisorIdAndSubordinateId(Long supervisorId, Long subordinateId);

    @Query("SELECT COUNT(uh) FROM UserHierarchy uh WHERE uh.supervisor.id = :userId OR uh.subordinate.id = :userId")
    long countUserRelationships(@Param("userId") Long userId);

    @Query("SELECT uh FROM UserHierarchy uh WHERE uh.subordinate.id = :userId")
    List<UserHierarchy> findSupervisorRelationships(@Param("userId") Long userId);

    @Query("SELECT uh FROM UserHierarchy uh WHERE uh.supervisor.id = :userId")
    List<UserHierarchy> findSubordinateRelationships(@Param("userId") Long userId);

    // SIMPLIFIED: Check if proposed subordinate already supervises proposed supervisor (up to 2 levels)
    @Query("""
        SELECT COUNT(*) > 0 FROM UserHierarchy uh 
        WHERE (uh.supervisor.id = :proposedSubordinateId AND uh.subordinate.id = :proposedSupervisorId)
           OR (uh.supervisor.id = :proposedSubordinateId AND uh.subordinate.id IN (
                SELECT uh2.supervisor.id FROM UserHierarchy uh2 WHERE uh2.subordinate.id = :proposedSupervisorId
              ))
        """)
    boolean wouldCreateCycle(@Param("proposedSubordinateId") Long proposedSubordinateId,
                             @Param("proposedSupervisorId") Long proposedSupervisorId);
}