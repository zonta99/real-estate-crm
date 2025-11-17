package com.realestatecrm.repository;

import com.realestatecrm.entity.CustomerInteraction;
import com.realestatecrm.enums.InteractionType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CustomerInteractionRepository extends JpaRepository<CustomerInteraction, Long> {

    // LAZY FIX: Eagerly fetch customer, user, and relatedProperty to prevent LazyInitializationException
    @EntityGraph(attributePaths = {"customer", "user", "relatedProperty"})
    @Query("SELECT i FROM CustomerInteraction i WHERE i.customer.id = :customerId ORDER BY i.interactionDate DESC")
    List<CustomerInteraction> findByCustomerId(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = {"customer", "user", "relatedProperty"})
    @Query("SELECT i FROM CustomerInteraction i WHERE i.customer.id = :customerId AND i.type = :type ORDER BY i.interactionDate DESC")
    List<CustomerInteraction> findByCustomerIdAndType(@Param("customerId") Long customerId,
                                                       @Param("type") InteractionType type);

    @EntityGraph(attributePaths = {"customer", "user", "relatedProperty"})
    @Query("SELECT i FROM CustomerInteraction i WHERE i.customer.id = :customerId " +
           "AND i.interactionDate BETWEEN :startDate AND :endDate ORDER BY i.interactionDate DESC")
    List<CustomerInteraction> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    @EntityGraph(attributePaths = {"customer", "user", "relatedProperty"})
    @Query("SELECT i FROM CustomerInteraction i WHERE i.user.id = :userId ORDER BY i.interactionDate DESC")
    List<CustomerInteraction> findByUserId(@Param("userId") Long userId);
}
