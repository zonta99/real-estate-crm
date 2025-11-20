package com.realestatecrm.repository;

import com.realestatecrm.entity.PropertySharing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertySharingRepository extends BaseRepository<PropertySharing, Long> {

    List<PropertySharing> findByPropertyId(Long propertyId);

    List<PropertySharing> findBySharedWithUserId(Long userId);

    List<PropertySharing> findBySharedByUserId(Long userId);

    Optional<PropertySharing> findByPropertyIdAndSharedWithUserId(Long propertyId, Long sharedWithUserId);

    boolean existsByPropertyIdAndSharedWithUserId(Long propertyId, Long sharedWithUserId);

    void deleteByPropertyIdAndSharedWithUserId(Long propertyId, Long sharedWithUserId);

    @Query("SELECT ps FROM PropertySharing ps WHERE ps.property.agent.id = :agentId")
    List<PropertySharing> findByPropertyOwner(@Param("agentId") Long agentId);

    long countByPropertyId(Long propertyId);

    @Query("SELECT ps FROM PropertySharing ps WHERE ps.sharedWithUser.id = :userId AND ps.property.status = :status")
    List<PropertySharing> findBySharedWithUserIdAndPropertyStatus(@Param("userId") Long userId, @Param("status") String status);
}