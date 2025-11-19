package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.property.response.PropertyResponse;
import com.realestatecrm.dto.savedsearch.PropertySearchCriteriaRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchResponse;
import com.realestatecrm.entity.Customer;
import com.realestatecrm.entity.Property;
import com.realestatecrm.entity.User;
import com.realestatecrm.mapper.PropertyMapper;
import com.realestatecrm.service.CustomerService;
import com.realestatecrm.service.SavedSearchService;
import com.realestatecrm.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final CustomerService customerService;
    private final UserService userService;
    private final PropertyMapper propertyMapper;

    @Autowired
    public SavedSearchController(SavedSearchService savedSearchService,
                                 CustomerService customerService,
                                 UserService userService,
                                 PropertyMapper propertyMapper) {
        this.savedSearchService = savedSearchService;
        this.customerService = customerService;
        this.userService = userService;
        this.propertyMapper = propertyMapper;
    }

    // Get all saved searches for the current agent's customers
    @GetMapping("/saved-searches")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<SavedSearchResponse>> getAllSavedSearchesForAgent(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<SavedSearchResponse> searches = savedSearchService.getSavedSearchesByAgent(currentUser.getId());
        return ResponseEntity.ok(searches);
    }

    // Get all saved searches for a specific customer
    @GetMapping("/customers/{customerId}/saved-searches")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSavedSearchesForCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Check if customer belongs to current user
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (!customer.getAgent().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You can only view saved searches for your own customers"));
        }

        List<SavedSearchResponse> searches = savedSearchService.getSavedSearchesByCustomer(customerId);
        return ResponseEntity.ok(searches);
    }

    // Get specific saved search by ID
    @GetMapping("/saved-searches/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSavedSearchById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        SavedSearchResponse response = savedSearchService.getSavedSearchById(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found"));

        // Authorization check: ensure agent owns this customer
        if (!response.getAgentId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied"));
        }

        return ResponseEntity.ok(response);
    }

    // Create new saved search for a customer
    @PostMapping("/customers/{customerId}/saved-searches")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> createSavedSearch(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedSearchRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Check if customer belongs to current user
        Customer customer = customerService.getCustomerById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (!customer.getAgent().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied: You can only create saved searches for your own customers"));
        }

        try {
            SavedSearchResponse response = savedSearchService.createSavedSearch(customerId, request);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Saved search created successfully");
            responseBody.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // Update saved search
    @PutMapping("/saved-searches/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateSavedSearch(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedSearchRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            SavedSearchResponse response = savedSearchService.updateSavedSearch(id, currentUser.getId(), request);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Saved search updated successfully");
            responseBody.put("data", response);

            return ResponseEntity.ok(responseBody);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // Delete saved search
    @DeleteMapping("/saved-searches/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteSavedSearch(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            savedSearchService.deleteSavedSearch(id, currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("Saved search deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // Execute a saved search
    @GetMapping("/saved-searches/{id}/execute")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> executeSavedSearch(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false, defaultValue = "createdDate,desc") String sort) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        try {
            Page<Property> properties = savedSearchService.executeSavedSearch(id, currentUser.getId(), page, size, sort);
            Page<PropertyResponse> response = properties.map(propertyMapper::toResponse);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
