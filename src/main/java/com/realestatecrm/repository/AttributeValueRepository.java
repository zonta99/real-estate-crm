package com.realestatecrm.repository;

import com.realestatecrm.entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeValueRepository extends BaseRepository<AttributeValue, Long> {

    List<AttributeValue> findByPropertyId(Long propertyId);

    List<AttributeValue> findByAttributeId(Long attributeId);

    Optional<AttributeValue> findByPropertyIdAndAttributeId(Long propertyId, Long attributeId);

    // Batch fetch method to avoid N+1 queries
    List<AttributeValue> findByPropertyIdInAndAttributeIdIn(List<Long> propertyIds, List<Long> attributeIds);

    void deleteByPropertyIdAndAttributeId(Long propertyId, Long attributeId);

    List<AttributeValue> findByAttributeIdAndTextValue(Long attributeId, String textValue);

    @Query("SELECT pv FROM AttributeValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue BETWEEN :minValue AND :maxValue")
    List<AttributeValue> findByAttributeIdAndNumberValueBetween(@Param("attributeId") Long attributeId,
                                                                @Param("minValue") BigDecimal minValue,
                                                                @Param("maxValue") BigDecimal maxValue);

    List<AttributeValue> findByAttributeIdAndBooleanValue(Long attributeId, Boolean booleanValue);

    @Query("SELECT pv FROM AttributeValue pv WHERE pv.attribute.id = :attributeId AND pv.multiSelectValue LIKE %:value%")
    List<AttributeValue> findByAttributeIdAndMultiSelectContains(@Param("attributeId") Long attributeId,
                                                                 @Param("value") String value);

    @Query("SELECT pv FROM AttributeValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue >= :minValue")
    List<AttributeValue> findByAttributeIdAndNumberValueGreaterThanEqual(@Param("attributeId") Long attributeId,
                                                                         @Param("minValue") BigDecimal minValue);

    @Query("SELECT pv FROM AttributeValue pv WHERE pv.attribute.id = :attributeId AND pv.numberValue <= :maxValue")
    List<AttributeValue> findByAttributeIdAndNumberValueLessThanEqual(@Param("attributeId") Long attributeId,
                                                                      @Param("maxValue") BigDecimal maxValue);
}
