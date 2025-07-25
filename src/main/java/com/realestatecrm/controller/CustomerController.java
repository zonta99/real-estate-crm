package com.realestatecrm.controller;

import com.realestatecrm.entity.Customer;
import com.realestatecrm.entity.CustomerSearchCriteria;
import com.realestatecrm.entity.Property;
import com.realestatecrm.entity.User;
import com.realestatecrm.enums.CustomerStatus;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
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

        Page<CustomerResponse> response = customers.map(this::convertToCustomerResponse);
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

        return ResponseEntity.ok(convertToSearchCriteriaResponse(criteria));
    }

    @GetMapping("/{id}/search-criteria")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<CustomerSearchCriteriaResponse>> getSearchCriteria(@PathVariable Long id) {
        List<CustomerSearchCriteria> criteria = customerService.getSearchCriteria(id);
        List<CustomerSearchCriteriaResponse> responses = criteria.stream()
                .map(this::convertToSearchCriteriaResponse)
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

    @GetMapping("/{id}/matching-properties")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<PropertyMatchResponse>> getMatchingProperties(@PathVariable Long id) {
        List<Property> matchingProperties = customerService.findMatchingProperties(id);
        List<PropertyMatchResponse> responses = matchingProperties.stream()
                .map(this::convertToPropertyMatchResponse)
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
                .map(this::convertToCustomerResponse)
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
                .map(this::convertToCustomerResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    private CustomerResponse convertToCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getBudgetMin(),
                customer.getBudgetMax(),
                customer.getNotes(),
                customer.getLeadSource(),
                customer.getStatus(),
                customer.getAgent().getId(),
                customer.getAgent().getFullName(),
                customer.getCreatedDate(),
                customer.getUpdatedDate()
        );
    }

    private CustomerSearchCriteriaResponse convertToSearchCriteriaResponse(CustomerSearchCriteria criteria) {
        return new CustomerSearchCriteriaResponse(
                criteria.getId(),
                criteria.getCustomer().getId(),
                criteria.getAttribute().getId(),
                criteria.getAttribute().getName(),
                criteria.getAttribute().getDataType().toString(),
                criteria.getTextValue(),
                criteria.getNumberMinValue(),
                criteria.getNumberMaxValue(),
                criteria.getBooleanValue(),
                criteria.getMultiSelectValue()
        );
    }

    private PropertyMatchResponse convertToPropertyMatchResponse(Property property) {
        return new PropertyMatchResponse(
                property.getId(),
                property.getTitle(),
                property.getDescription(),
                property.getPrice(),
                property.getAgent().getFullName(),
                property.getStatus().toString()
        );
    }

    // DTOs
    public static class CreateCustomerRequest {
        @NotBlank
        private String firstName;

        @NotBlank
        private String lastName;

        @NotBlank
        private String phone;

        @Email
        private String email;

        private BigDecimal budgetMin;
        private BigDecimal budgetMax;
        private String notes;
        private String leadSource;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public BigDecimal getBudgetMin() { return budgetMin; }
        public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
        public BigDecimal getBudgetMax() { return budgetMax; }
        public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getLeadSource() { return leadSource; }
        public void setLeadSource(String leadSource) { this.leadSource = leadSource; }
    }

    public static class UpdateCustomerRequest {
        @NotBlank
        private String firstName;

        @NotBlank
        private String lastName;

        @NotBlank
        private String phone;

        @Email
        private String email;

        private BigDecimal budgetMin;
        private BigDecimal budgetMax;
        private String notes;
        private String leadSource;

        @NotNull
        private CustomerStatus status;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public BigDecimal getBudgetMin() { return budgetMin; }
        public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
        public BigDecimal getBudgetMax() { return budgetMax; }
        public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public String getLeadSource() { return leadSource; }
        public void setLeadSource(String leadSource) { this.leadSource = leadSource; }
        public CustomerStatus getStatus() { return status; }
        public void setStatus(CustomerStatus status) { this.status = status; }
    }

    public static class SetSearchCriteriaRequest {
        @NotNull
        private Long attributeId;
        private String textValue;
        private BigDecimal numberMinValue;
        private BigDecimal numberMaxValue;
        private Boolean booleanValue;
        private String multiSelectValue;

        // Getters and setters
        public Long getAttributeId() { return attributeId; }
        public void setAttributeId(Long attributeId) { this.attributeId = attributeId; }
        public String getTextValue() { return textValue; }
        public void setTextValue(String textValue) { this.textValue = textValue; }
        public BigDecimal getNumberMinValue() { return numberMinValue; }
        public void setNumberMinValue(BigDecimal numberMinValue) { this.numberMinValue = numberMinValue; }
        public BigDecimal getNumberMaxValue() { return numberMaxValue; }
        public void setNumberMaxValue(BigDecimal numberMaxValue) { this.numberMaxValue = numberMaxValue; }
        public Boolean getBooleanValue() { return booleanValue; }
        public void setBooleanValue(Boolean booleanValue) { this.booleanValue = booleanValue; }
        public String getMultiSelectValue() { return multiSelectValue; }
        public void setMultiSelectValue(String multiSelectValue) { this.multiSelectValue = multiSelectValue; }
    }

    public static class CustomerResponse {
        private Long id;
        private String firstName;
        private String lastName;
        private String phone;
        private String email;
        private BigDecimal budgetMin;
        private BigDecimal budgetMax;
        private String notes;
        private String leadSource;
        private CustomerStatus status;
        private Long agentId;
        private String agentName;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public CustomerResponse(Long id, String firstName, String lastName, String phone, String email,
                                BigDecimal budgetMin, BigDecimal budgetMax, String notes, String leadSource,
                                CustomerStatus status, Long agentId, String agentName,
                                LocalDateTime createdDate, LocalDateTime updatedDate) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.email = email;
            this.budgetMin = budgetMin;
            this.budgetMax = budgetMax;
            this.notes = notes;
            this.leadSource = leadSource;
            this.status = status;
            this.agentId = agentId;
            this.agentName = agentName;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
        }

        // Getters
        public Long getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public BigDecimal getBudgetMin() { return budgetMin; }
        public BigDecimal getBudgetMax() { return budgetMax; }
        public String getNotes() { return notes; }
        public String getLeadSource() { return leadSource; }
        public CustomerStatus getStatus() { return status; }
        public Long getAgentId() { return agentId; }
        public String getAgentName() { return agentName; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
    }

    public static class CustomerSearchCriteriaResponse {
        private Long id;
        private Long customerId;
        private Long attributeId;
        private String attributeName;
        private String dataType;
        private String textValue;
        private BigDecimal numberMinValue;
        private BigDecimal numberMaxValue;
        private final Boolean booleanValue;
        private String multiSelectValue;

        public CustomerSearchCriteriaResponse(Long id, Long customerId, Long attributeId, String attributeName,
                                              String dataType, String textValue, BigDecimal numberMinValue,
                                              BigDecimal numberMaxValue, Boolean booleanValue, String multiSelectValue) {
            this.id = id;
            this.customerId = customerId;
            this.attributeId = attributeId;
            this.attributeName = attributeName;
            this.dataType = dataType;
            this.textValue = textValue;
            this.numberMinValue = numberMinValue;
            this.numberMaxValue = numberMaxValue;
            this.booleanValue = booleanValue;
            this.multiSelectValue = multiSelectValue;
        }

        // Getters
        public Long getId() { return id; }
        public Long getCustomerId() { return customerId; }
        public Long getAttributeId() { return attributeId; }
        public String getAttributeName() { return attributeName; }
        public String getDataType() { return dataType; }
        public String getTextValue() { return textValue; }
        public BigDecimal getNumberMinValue() { return numberMinValue; }
        public BigDecimal getNumberMaxValue() { return numberMaxValue; }
        public Boolean getBooleanValue() { return booleanValue; }
        public String getMultiSelectValue() { return multiSelectValue; }
    }

    public static class PropertyMatchResponse {
        private Long id;
        private String title;
        private String description;
        private BigDecimal price;
        private String agentName;
        private String status;

        public PropertyMatchResponse(Long id, String title, String description, BigDecimal price,
                                     String agentName, String status) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.price = price;
            this.agentName = agentName;
            this.status = status;
        }

        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public String getAgentName() { return agentName; }
        public String getStatus() { return status; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}