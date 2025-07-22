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

    @Query("""
           SELECT CASE WHEN COUNT(uh) > 0 THEN true ELSE false END
           FROM UserHierarchy uh
           WHERE uh.supervisor.id = :startUserId AND uh.subordinate.id = :endUserId
                      OR uh.supervisor.id = :endUserId AND uh.subordinate.id = :startUserId""")
    boolean wouldCreateCycle(@Param("startUserId") Long startUserId, @Param("endUserId") Long endUserId);
}