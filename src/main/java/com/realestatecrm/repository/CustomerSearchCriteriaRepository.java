package com.realestatecrm.repository;

import com.realestatecrm.entity.CustomerSearchCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerSearchCriteriaRepository extends JpaRepository<CustomerSearchCriteria, Long> {

    List<CustomerSearchCriteria> findByCustomerId(Long customerId);

    List<CustomerSearchCriteria> findByAttributeId(Long attributeId);

    Optional<CustomerSearchCriteria> findByCustomerIdAndAttributeId(Long customerId, Long attributeId);

    void deleteByCustomerIdAndAttributeId(Long customerId, Long attributeId);

    @Query("SELECT csc FROM CustomerSearchCriteria csc WHERE csc.customer.id = :customerId AND csc.attribute.isSearchable = true")
    List<CustomerSearchCriteria> findSearchableCriteriaByCustomerId(@Param("customerId") Long customerId);

    long countByCustomerId(Long customerId);

    @Query("SELECT csc FROM CustomerSearchCriteria csc WHERE csc.attribute.id = :attributeId AND csc.textValue = :textValue")
    List<CustomerSearchCriteria> findByAttributeIdAndTextValue(@Param("attributeId") Long attributeId, @Param("textValue") String textValue);

    @Query("SELECT csc FROM CustomerSearchCriteria csc WHERE csc.attribute.id = :attributeId AND csc.booleanValue = :booleanValue")
    List<CustomerSearchCriteria> findByAttributeIdAndBooleanValue(@Param("attributeId") Long attributeId, @Param("booleanValue") Boolean booleanValue);
}