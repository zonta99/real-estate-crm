package com.realestatecrm.repository;

import com.realestatecrm.entity.CustomerNote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Long> {

    // LAZY FIX: Eagerly fetch customer and createdBy to prevent LazyInitializationException
    @EntityGraph(attributePaths = {"customer", "createdBy"})
    @Query("SELECT n FROM CustomerNote n WHERE n.customer.id = :customerId ORDER BY n.createdDate DESC")
    List<CustomerNote> findByCustomerId(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = {"customer", "createdBy"})
    @Query("SELECT n FROM CustomerNote n WHERE n.customer.id = :customerId AND n.createdBy.id = :userId ORDER BY n.createdDate DESC")
    List<CustomerNote> findByCustomerIdAndCreatedById(@Param("customerId") Long customerId,
                                                       @Param("userId") Long userId);
}
