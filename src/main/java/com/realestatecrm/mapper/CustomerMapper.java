package com.realestatecrm.mapper;

import com.realestatecrm.dto.customer.response.CustomerInteractionResponse;
import com.realestatecrm.dto.customer.response.CustomerNoteResponse;
import com.realestatecrm.dto.customer.response.CustomerResponse;
import com.realestatecrm.dto.customer.response.CustomerSearchCriteriaResponse;
import com.realestatecrm.dto.customer.response.PropertyMatchResponse;
import com.realestatecrm.entity.Customer;
import com.realestatecrm.entity.CustomerInteraction;
import com.realestatecrm.entity.CustomerNote;
import com.realestatecrm.entity.CustomerSearchCriteria;
import com.realestatecrm.entity.Property;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Customer-related entity to DTO conversions.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /**
     * Maps Customer entity to CustomerResponse DTO.
     */
    @Mapping(target = "agentId", source = "agent.id")
    @Mapping(target = "agentName", expression = "java(customer.getAgent().getFullName())")
    CustomerResponse toResponse(Customer customer);

    /**
     * Maps CustomerSearchCriteria entity to CustomerSearchCriteriaResponse DTO.
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "attributeId", source = "attribute.id")
    @Mapping(target = "attributeName", source = "attribute.name")
    @Mapping(target = "dataType", expression = "java(criteria.getAttribute().getDataType().toString())")
    CustomerSearchCriteriaResponse toSearchCriteriaResponse(CustomerSearchCriteria criteria);

    /**
     * Maps Property entity to PropertyMatchResponse DTO for customer property matches.
     */
    @Mapping(target = "propertyId", source = "id")
    @Mapping(target = "propertyTitle", source = "title")
    @Mapping(target = "propertyDescription", source = "description")
    @Mapping(target = "propertyPrice", source = "price")
    @Mapping(target = "propertyStatus", source = "status")
    @Mapping(target = "agentId", source = "agent.id")
    @Mapping(target = "agentName", expression = "java(property.getAgent().getFullName())")
    PropertyMatchResponse toPropertyMatchResponse(Property property);

    /**
     * Maps CustomerNote entity to CustomerNoteResponse DTO.
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(note.getCustomer().getFullName())")
    @Mapping(target = "createdByUserId", source = "createdBy.id")
    @Mapping(target = "createdByUserName", expression = "java(note.getCreatedBy().getFullName())")
    CustomerNoteResponse toCustomerNoteResponse(CustomerNote note);

    /**
     * Maps CustomerInteraction entity to CustomerInteractionResponse DTO.
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(interaction.getCustomer().getFullName())")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", expression = "java(interaction.getUser().getFullName())")
    @Mapping(target = "relatedPropertyId", source = "relatedProperty.id")
    @Mapping(target = "relatedPropertyTitle", source = "relatedProperty.title")
    CustomerInteractionResponse toCustomerInteractionResponse(CustomerInteraction interaction);
}
