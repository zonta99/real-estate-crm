package com.realestatecrm.repository;

import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyAttributeRepository extends BaseRepository<PropertyAttribute, Long> {

    List<PropertyAttribute> findByCategory(PropertyCategory category);

    List<PropertyAttribute> findByDataType(PropertyDataType dataType);

    List<PropertyAttribute> findByIsRequired(Boolean isRequired);

    List<PropertyAttribute> findByIsSearchable(Boolean isSearchable);

    List<PropertyAttribute> findByCategoryOrderByDisplayOrderAsc(PropertyCategory category);

    @Query("SELECT pa FROM PropertyAttribute pa ORDER BY pa.category ASC, pa.displayOrder ASC, pa.name ASC")
    List<PropertyAttribute> findAllOrderedByDisplay();

    @Query("SELECT pa FROM PropertyAttribute pa WHERE pa.isSearchable = true ORDER BY pa.category ASC, pa.displayOrder ASC")
    List<PropertyAttribute> findSearchableOrderedByDisplay();

    boolean existsByName(String name);

    List<PropertyAttribute> findByNameContainingIgnoreCase(String name);
}