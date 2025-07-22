package com.realestatecrm.repository;

import com.realestatecrm.entity.PropertyValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyValueRepository extends JpaRepository<PropertyValue, Long> {

    List<PropertyValue> findByPropertyId(Long propertyId);

    List<PropertyValue> findByAttributeId(Long attributeId);

    Optional<PropertyValue> findByPropertyIdAndAttributeId(Long propertyId, Long attributeId);

    void deleteByPropertyIdAndAttributeId(Long propertyId, Long attributeId);

    List<PropertyValue> findByAttributeIdAndTextValue(Long attributeId, String textValue);

    @Query("SELECT pv FROM PropertyValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue BETWEEN :minValue AND :maxValue")
    List<PropertyValue> findByAttributeIdAndNumberValueBetween(@Param("attributeId") Long attributeId,
                                                               @Param("minValue") BigDecimal minValue,
                                                               @Param("maxValue") BigDecimal maxValue);

    List<PropertyValue> findByAttributeIdAndBooleanValue(Long attributeId, Boolean booleanValue);

    @Query("SELECT pv FROM PropertyValue pv WHERE pv.attribute.id = :attributeId AND pv.multiSelectValue LIKE %:value%")
    List<PropertyValue> findByAttributeIdAndMultiSelectContains(@Param("attributeId") Long attributeId,
                                                                @Param("value") String value);

    @Query("SELECT pv FROM PropertyValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue >= :minValue")
    List<PropertyValue> findByAttributeIdAndNumberValueGreaterThanEqual(@Param("attributeId") Long attributeId,
                                                                        @Param("minValue") BigDecimal minValue);

    @Query("SELECT pv FROM PropertyValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue <= :maxValue")
    List<PropertyValue> findByAttributeIdAndNumberValueLessThanEqual(@Param("attributeId") Long attributeId,
                                                                     @Param("maxValue") BigDecimal maxValue);
}