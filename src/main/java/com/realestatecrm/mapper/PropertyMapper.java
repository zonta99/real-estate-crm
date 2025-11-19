package com.realestatecrm.mapper;

import com.realestatecrm.dto.property.request.CreatePropertyRequest;
import com.realestatecrm.dto.property.request.UpdatePropertyRequest;
import com.realestatecrm.dto.property.response.AttributeValueResponse;
import com.realestatecrm.dto.property.response.PropertyResponse;
import com.realestatecrm.dto.property.response.PropertySharingResponse;
import com.realestatecrm.entity.AttributeValue;
import com.realestatecrm.entity.Property;
import com.realestatecrm.entity.PropertySharing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for Property-related entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface PropertyMapper {

    /**
     * Maps Property entity to PropertyResponse DTO (without attribute values).
     */
    @Mapping(target = "agentId", source = "agent.id")
    @Mapping(target = "agentName", expression = "java(property.getAgent().getFullName())")
    @Mapping(target = "attributeValues", ignore = true)
    PropertyResponse toResponse(Property property);

    /**
     * Maps Property entity to PropertyResponse DTO with attribute values.
     */
    @Mapping(target = "agentId", source = "property.agent.id")
    @Mapping(target = "agentName", expression = "java(property.getAgent().getFullName())")
    @Mapping(target = "attributeValues", source = "attributeValues")
    PropertyResponse toResponseWithAttributes(Property property, List<AttributeValue> attributeValues);

    /**
     * Maps AttributeValue entity to AttributeValueResponse DTO.
     */
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "dataType", expression = "java(value.getAttribute().getDataType().toString())")
    @Mapping(target = "value", source = "value")
    AttributeValueResponse toAttributeValueResponse(AttributeValue value);

    /**
     * Maps list of AttributeValue entities to list of AttributeValueResponse DTOs.
     */
    List<AttributeValueResponse> toAttributeValueResponseList(List<AttributeValue> values);

    /**
     * Maps PropertySharing entity to PropertySharingResponse DTO.
     */
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "sharedWithUserId", source = "sharedWithUser.id")
    @Mapping(target = "sharedWithUserName", expression = "java(sharing.getSharedWithUser().getFullName())")
    @Mapping(target = "sharedByUserId", source = "sharedByUser.id")
    @Mapping(target = "sharedByUserName", expression = "java(sharing.getSharedByUser().getFullName())")
    PropertySharingResponse toPropertySharingResponse(PropertySharing sharing);

    // ==================== Request to Entity Mappings ====================

    /**
     * Maps CreatePropertyRequest DTO to Property entity.
     * Note: agent and status fields must be set manually by the controller.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Property toEntity(CreatePropertyRequest request);

    /**
     * Maps UpdatePropertyRequest DTO to Property entity.
     * Note: agent field must be set manually by the controller if needed.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "attributeValues", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    Property toEntity(UpdatePropertyRequest request);
}
