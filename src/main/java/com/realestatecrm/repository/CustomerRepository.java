package com.realestatecrm.repository;

import com.realestatecrm.entity.Customer;
import com.realestatecrm.enums.CustomerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByAgentId(Long agentId);

    Page<Customer> findByAgentId(Long agentId, Pageable pageable);

    List<Customer> findByStatus(CustomerStatus status);

    List<Customer> findByAgentIdAndStatus(Long agentId, CustomerStatus status);

    Page<Customer> findByAgentIdAndStatus(Long agentId, CustomerStatus status, Pageable pageable);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByPhone(String phone);

    List<Customer> findByAgentIdIn(List<Long> agentIds);

    Page<Customer> findByAgentIdIn(List<Long> agentIds, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE (c.budgetMin <= :price OR c.budgetMin IS NULL) AND (c.budgetMax >= :price OR c.budgetMax IS NULL)")
    List<Customer> findByPriceInBudgetRange(@Param("price") BigDecimal price);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByNameContaining(@Param("name") String name);

    long countByAgentIdAndStatus(Long agentId, CustomerStatus status);

    List<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("SELECT c FROM Customer c WHERE c.budgetMax >= :minBudget")
    List<Customer> findByBudgetMaxGreaterThanEqual(@Param("minBudget") BigDecimal minBudget);

    @Query("SELECT c FROM Customer c WHERE c.budgetMin <= :maxBudget")
    List<Customer> findByBudgetMinLessThanEqual(@Param("maxBudget") BigDecimal maxBudget);
}