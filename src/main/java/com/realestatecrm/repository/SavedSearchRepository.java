package com.realestatecrm.repository;

import com.realestatecrm.entity.SavedSearch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends BaseRepository<SavedSearch, Long> {

    // LAZY FIX: Optimized query with customer eager loading to avoid LazyInitializationException
    @EntityGraph(attributePaths = {"customer", "customer.agent"})
    @Query("SELECT s FROM SavedSearch s WHERE s.id = :id")
    Optional<SavedSearch> findByIdWithCustomer(@Param("id") Long id);

    // LAZY FIX: Optimized findAll with customer
    @EntityGraph(attributePaths = {"customer", "customer.agent"})
    @Query("SELECT s FROM SavedSearch s")
    List<SavedSearch> findAllWithCustomer();

    // LAZY FIX: Find all searches for a specific customer
    @EntityGraph(attributePaths = {"customer", "customer.agent"})
    @Query("SELECT s FROM SavedSearch s WHERE s.customer.id = :customerId ORDER BY s.updatedDate DESC")
    List<SavedSearch> findByCustomerIdWithCustomer(@Param("customerId") Long customerId);

    // Find all searches for customers belonging to a specific agent
    @EntityGraph(attributePaths = {"customer", "customer.agent"})
    @Query("SELECT s FROM SavedSearch s WHERE s.customer.agent.id = :agentId ORDER BY s.customer.lastName, s.updatedDate DESC")
    List<SavedSearch> findByCustomerAgentId(@Param("agentId") Long agentId);

    // Check if a saved search exists for a customer
    boolean existsByIdAndCustomerId(Long id, Long customerId);

    // Count searches for a customer
    long countByCustomerId(Long customerId);
}
