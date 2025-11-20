package com.realestatecrm.repository;

import com.realestatecrm.entity.PropertyAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyAttributeOptionRepository extends BaseRepository<PropertyAttributeOption, Long> {

    List<PropertyAttributeOption> findByAttributeId(Long attributeId);

    @Query("SELECT pao FROM PropertyAttributeOption pao WHERE pao.attribute.id = :attributeId ORDER BY pao.displayOrder ASC, pao.optionValue ASC")
    List<PropertyAttributeOption> findByAttributeIdOrderByDisplayOrder(@Param("attributeId") Long attributeId);

    Optional<PropertyAttributeOption> findByAttributeIdAndOptionValue(Long attributeId, String optionValue);

    boolean existsByAttributeIdAndOptionValue(Long attributeId, String optionValue);

    void deleteByAttributeId(Long attributeId);

    long countByAttributeId(Long attributeId);

    @Query("SELECT pao FROM PropertyAttributeOption pao WHERE pao.optionValue LIKE %:search% ORDER BY pao.optionValue ASC")
    List<PropertyAttributeOption> findByOptionValueContaining(@Param("search") String search);
}