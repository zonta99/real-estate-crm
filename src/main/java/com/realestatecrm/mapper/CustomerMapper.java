package com.realestatecrm.mapper;

import com.realestatecrm.dto.customer.request.CreateCustomerInteractionRequest;
import com.realestatecrm.dto.customer.request.CreateCustomerRequest;
import com.realestatecrm.dto.customer.request.UpdateCustomerRequest;
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
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "status", source = "status")
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

    // ==================== Request to Entity Mappings ====================

    /**
     * Maps CreateCustomerRequest DTO to Customer entity.
     * Note: agent and status fields must be set manually by the controller.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "searchCriteria", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    /**
     * Maps UpdateCustomerRequest DTO to Customer entity.
     * Note: agent field must be set manually by the controller if needed.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "searchCriteria", ignore = true)
    Customer toEntity(UpdateCustomerRequest request);

    /**
     * Maps CreateCustomerInteractionRequest DTO to CustomerInteraction entity.
     * Note: customer, user, and relatedProperty must be set manually by the controller.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "relatedProperty", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    CustomerInteraction toEntity(CreateCustomerInteractionRequest request);
}
