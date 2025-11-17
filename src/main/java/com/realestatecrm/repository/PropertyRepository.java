package com.realestatecrm.repository;

import com.realestatecrm.entity.Property;
import com.realestatecrm.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // LAZY FIX: Optimized query with agent eager loading to avoid LazyInitializationException
    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p WHERE p.id = :id")
    Optional<Property> findByIdWithAgent(@Param("id") Long id);

    // LAZY FIX: Optimized findAll with agent
    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p")
    List<Property> findAllWithAgent();

    // LAZY FIX: Optimized findAll with pagination and agent
    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p")
    Page<Property> findAllWithAgent(Pageable pageable);

    // LAZY FIX: All query methods now fetch agent to prevent LazyInitializationException
    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByAgentId(Long agentId);

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findByAgentId(Long agentId, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByStatus(PropertyStatus status);

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByAgentIdAndStatus(Long agentId, PropertyStatus status);

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findByAgentIdAndStatus(Long agentId, PropertyStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByAgentIdIn(List<Long> agentIds);

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findByAgentIdIn(List<Long> agentIds, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN FETCH p.agent LEFT JOIN PropertySharing ps ON p.id = ps.property.id WHERE p.agent.id = :agentId OR ps.sharedWithUser.id = :agentId")
    List<Property> findAccessibleByAgent(@Param("agentId") Long agentId);

    @Query("SELECT DISTINCT p FROM Property p LEFT JOIN FETCH p.agent LEFT JOIN PropertySharing ps ON p.id = ps.property.id WHERE (p.agent.id = :agentId OR ps.sharedWithUser.id = :agentId) AND p.status = :status")
    List<Property> findAccessibleByAgentAndStatus(@Param("agentId") Long agentId, @Param("status") PropertyStatus status);

    @Query("SELECT COUNT(p) FROM Property p WHERE p.agent.id = :agentId AND p.status = :status")
    long countByAgentIdAndStatus(@Param("agentId") Long agentId, @Param("status") PropertyStatus status);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByTitleContainingIgnoreCase(String title);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p WHERE p.price >= :minPrice")
    List<Property> findByPriceGreaterThanEqual(@Param("minPrice") BigDecimal minPrice);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p WHERE p.price <= :maxPrice")
    List<Property> findByPriceLessThanEqual(@Param("maxPrice") BigDecimal maxPrice);
}