package com.realestatecrm.repository;

import com.realestatecrm.entity.Customer;
import com.realestatecrm.enums.CustomerStatus;
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
public interface CustomerRepository extends BaseRepository<Customer, Long> {

    // LAZY FIX: Optimized queries with agent eager loading
    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c")
    List<Customer> findAllWithAgent();

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c")
    Page<Customer> findAllWithAgent(Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c WHERE c.id = :id")
    Optional<Customer> findByIdWithAgent(@Param("id") Long id);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByAgentId(Long agentId);

    @EntityGraph(attributePaths = {"agent"})
    Page<Customer> findByAgentId(Long agentId, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByStatus(CustomerStatus status);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByAgentIdAndStatus(Long agentId, CustomerStatus status);

    @EntityGraph(attributePaths = {"agent"})
    Page<Customer> findByAgentIdAndStatus(Long agentId, CustomerStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    Optional<Customer> findByEmail(String email);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByPhone(String phone);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByAgentIdIn(List<Long> agentIds);

    @EntityGraph(attributePaths = {"agent"})
    Page<Customer> findByAgentIdIn(List<Long> agentIds, Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c WHERE (c.budgetMin <= :price OR c.budgetMin IS NULL) AND (c.budgetMax >= :price OR c.budgetMax IS NULL)")
    List<Customer> findByPriceInBudgetRange(@Param("price") BigDecimal price);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByNameContaining(@Param("name") String name);

    long countByAgentIdAndStatus(Long agentId, CustomerStatus status);

    @EntityGraph(attributePaths = {"agent"})
    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c WHERE c.budgetMax >= :minBudget")
    List<Customer> findByBudgetMaxGreaterThanEqual(@Param("minBudget") BigDecimal minBudget);

    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT c FROM Customer c WHERE c.budgetMin <= :maxBudget")
    List<Customer> findByBudgetMinLessThanEqual(@Param("maxBudget") BigDecimal maxBudget);
}