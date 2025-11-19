package com.realestatecrm.mapper;

import com.realestatecrm.dto.propertyattribute.request.CreateAttributeRequest;
import com.realestatecrm.dto.propertyattribute.request.UpdateAttributeRequest;
import com.realestatecrm.dto.propertyattribute.response.AttributeOptionResponse;
import com.realestatecrm.dto.propertyattribute.response.PropertyAttributeResponse;
import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.entity.PropertyAttributeOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for PropertyAttribute-related entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface PropertyAttributeMapper {

    /**
     * Maps PropertyAttribute entity to PropertyAttributeResponse DTO.
     */
    @Mapping(target = "dataType", expression = "java(attribute.getDataType().toString())")
    PropertyAttributeResponse toResponse(PropertyAttribute attribute);

    /**
     * Maps PropertyAttributeOption entity to AttributeOptionResponse DTO.
     */
    @Mapping(target = "attributeId", source = "attribute.id")
    AttributeOptionResponse toOptionResponse(PropertyAttributeOption option);

    // ==================== Request to Entity Mappings ====================

    /**
     * Maps CreateAttributeRequest DTO to PropertyAttribute entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "customerSearchCriteria", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    PropertyAttribute toEntity(CreateAttributeRequest request);

    /**
     * Maps UpdateAttributeRequest DTO to PropertyAttribute entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "customerSearchCriteria", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    PropertyAttribute toEntity(UpdateAttributeRequest request);
}
