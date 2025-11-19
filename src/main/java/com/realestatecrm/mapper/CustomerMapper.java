package com.realestatecrm.mapper;

import com.realestatecrm.dto.customer.request.CreateCustomerInteractionRequest;
import com.realestatecrm.dto.customer.request.CreateCustomerRequest;
import com.realestatecrm.dto.customer.request.UpdateCustomerRequest;
import com.realestatecrm.dto.customer.response.CustomerInteractionResponse;
import com.realestatecrm.dto.customer.response.CustomerNoteResponse;
import com.realestatecrm.dto.customer.response.CustomerResponse;
import com.realestatecrm.entity.Customer;
import com.realestatecrm.entity.CustomerInteraction;
import com.realestatecrm.entity.CustomerNote;
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
    @Mapping(target = "savedSearches", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    /**
     * Maps UpdateCustomerRequest DTO to Customer entity.
     * Note: agent field must be set manually by the controller if needed.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "savedSearches", ignore = true)
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
