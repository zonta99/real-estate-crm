package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.customer.request.CreateCustomerRequest;
import com.realestatecrm.dto.customer.request.CreateCustomerInteractionRequest;
import com.realestatecrm.dto.customer.request.CreateCustomerNoteRequest;
import com.realestatecrm.dto.customer.request.SetSearchCriteriaRequest;
import com.realestatecrm.dto.customer.request.UpdateCustomerRequest;
import com.realestatecrm.dto.customer.response.CustomerInteractionResponse;
import com.realestatecrm.dto.customer.response.CustomerNoteResponse;
import com.realestatecrm.dto.customer.response.CustomerResponse;
import com.realestatecrm.dto.customer.response.CustomerSearchCriteriaResponse;
import com.realestatecrm.dto.customer.response.PropertyMatchResponse;
import com.realestatecrm.entity.*;
import com.realestatecrm.enums.InteractionType;
import com.realestatecrm.enums.CustomerStatus;
import com.realestatecrm.mapper.CustomerMapper;
import com.realestatecrm.service.CustomerService;
import com.realestatecrm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService,
                            CustomerMapper customerMapper) {
        this.customerService = customerService;
        this.userService = userService;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) CustomerStatus status,
            Pageable pageable) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Page<Customer> customers;
        if (currentUser.getRole().name().equals("ADMIN")) {
            customers = customerService.getAllCustomers(pageable);
        } else {
            // Get customers for current user and their subordinates
            List<User> accessibleUsers = userService.getAccessibleUsers(currentUser.getId());
            List<Long> userIds = accessibleUsers.stream().map(User::getId).collect(Collectors.toList());
            customers = customerService.getCustomersByAgents(userIds, pageable);
        }

        if (status != null) {
            customers = customerService.getCustomersByStatus(status, pageable);
        }

        Page<CustomerResponse> response = customers.map(customerMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id));

        return ResponseEntity.ok(convertToCustomerResponse(customer));
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> createCustomer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCustomerRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setBudgetMin(request.getBudgetMin());
        customer.setBudgetMax(request.getBudgetMax());
        customer.setNotes(request.getNotes());
        customer.setLeadSource(request.getLeadSource());
        customer.setAgent(currentUser);
        customer.setStatus(CustomerStatus.LEAD);

        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToCustomerResponse(createdCustomer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setBudgetMin(request.getBudgetMin());
        customer.setBudgetMax(request.getBudgetMax());
        customer.setNotes(request.getNotes());
        customer.setLeadSource(request.getLeadSource());
        customer.setStatus(request.getStatus());

        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        return ResponseEntity.ok(convertToCustomerResponse(updatedCustomer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(new MessageResponse("Customer deleted successfully"));
    }

    @PostMapping("/{id}/search-criteria")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerSearchCriteriaResponse> setSearchCriteria(
            @PathVariable Long id,
            @Valid @RequestBody SetSearchCriteriaRequest request) {

        CustomerSearchCriteria criteria = customerService.setSearchCriteria(
                id,
                request.getAttributeId(),
                request.getTextValue(),
                request.getNumberMinValue(),
                request.getNumberMaxValue(),
                request.getBooleanValue(),
                request.getMultiSelectValue()
        );

        return ResponseEntity.ok(customerMapper.toSearchCriteriaResponse(criteria));
    }

    @GetMapping("/{id}/search-criteria")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerSearchCriteriaResponse>> getSearchCriteria(@PathVariable Long id) {
        List<CustomerSearchCriteria> criteria = customerService.getSearchCriteria(id);
        List<CustomerSearchCriteriaResponse> responses = criteria.stream()
                .map(this::customerMapper.toSearchCriteriaResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/search-criteria/{attributeId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteSearchCriteria(
            @PathVariable Long id,
            @PathVariable Long attributeId) {

        customerService.deleteSearchCriteria(id, attributeId);
        return ResponseEntity.ok(new MessageResponse("Search criteria deleted successfully"));
    }

    @GetMapping({"/{id}/matching-properties", "/{id}/matches"})
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<PropertyMatchResponse>> getMatchingProperties(@PathVariable Long id) {
        List<Property> matchingProperties = customerService.findMatchingProperties(id);
        List<PropertyMatchResponse> responses = matchingProperties.stream()
                .map(this::customerMapper.toPropertyMatchResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponse>> searchCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {

        List<Customer> customers = customerService.searchCustomers(name, status, phone, email);
        List<CustomerResponse> responses = customers.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/budget-range")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponse>> getCustomersByBudgetRange(
            @RequestParam BigDecimal minBudget,
            @RequestParam BigDecimal maxBudget) {

        List<Customer> customers = customerService.getCustomersByBudgetRange(minBudget, maxBudget);
        List<CustomerResponse> responses = customers.stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // Customer Notes Endpoints
    @PostMapping("/{id}/notes")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerNoteResponse> addCustomerNote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCustomerNoteRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        CustomerNote note = customerService.createCustomerNote(id, currentUser, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toCustomerNoteResponse(note));
    }

    @GetMapping("/{id}/notes")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerNoteResponse>> getCustomerNotes(@PathVariable Long id) {
        List<CustomerNote> notes = customerService.getCustomerNotes(id);
        List<CustomerNoteResponse> responses = notes.stream()
                .map(customerMapper::toCustomerNoteResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/notes/{noteId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCustomerNote(
            @PathVariable Long id,
            @PathVariable Long noteId) {

        customerService.deleteCustomerNote(noteId);
        return ResponseEntity.ok(new MessageResponse("Customer note deleted successfully"));
    }

    // Customer Interactions Endpoints
    @PostMapping("/{id}/interactions")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerInteractionResponse> createCustomerInteraction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCustomerInteractionRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        CustomerInteraction interaction = new CustomerInteraction();
        interaction.setType(request.getType());
        interaction.setSubject(request.getSubject());
        interaction.setNotes(request.getNotes());
        interaction.setInteractionDate(request.getInteractionDate());
        interaction.setDurationMinutes(request.getDurationMinutes());

        if (request.getRelatedPropertyId() != null) {
            Property property = new Property();
            property.setId(request.getRelatedPropertyId());
            interaction.setRelatedProperty(property);
        }

        CustomerInteraction createdInteraction = customerService.createCustomerInteraction(id, currentUser, interaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toCustomerInteractionResponse(createdInteraction));
    }

    @GetMapping("/{id}/interactions")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerInteractionResponse>> getCustomerInteractions(@PathVariable Long id) {
        List<CustomerInteraction> interactions = customerService.getCustomerInteractions(id);
        List<CustomerInteractionResponse> responses = interactions.stream()
                .map(customerMapper::toCustomerInteractionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/interactions/{interactionId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteCustomerInteraction(
            @PathVariable Long id,
            @PathVariable Long interactionId) {

        customerService.deleteCustomerInteraction(interactionId);
        return ResponseEntity.ok(new MessageResponse("Customer interaction deleted successfully"));
    }
}