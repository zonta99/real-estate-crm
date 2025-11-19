package com.realestatecrm.mapper;

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
}
