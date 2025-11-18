package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.property.response.PropertyResponse;
import com.realestatecrm.dto.savedsearch.PropertySearchCriteriaRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchResponse;
import com.realestatecrm.entity.Property;
import com.realestatecrm.entity.User;
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
@RequestMapping("/api/saved-searches")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;
    private final UserService userService;

    @Autowired
    public SavedSearchController(SavedSearchService savedSearchService, UserService userService) {
        this.savedSearchService = savedSearchService;
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<SavedSearchResponse>> getAllSavedSearches(
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<SavedSearchResponse> searches = savedSearchService.getSavedSearchesByUser(currentUser.getId());
        return ResponseEntity.ok(searches);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSavedSearchById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        SavedSearchResponse response = savedSearchService.getSavedSearchById(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found"));

        // Authorization check
        if (!response.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Access denied"));
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createSavedSearch(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SavedSearchRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        SavedSearchResponse response = savedSearchService.createSavedSearch(currentUser, request);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Saved search created successfully");
        responseBody.put("data", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PutMapping("/{id}")
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
        }
    }

    @DeleteMapping("/{id}")
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

    @GetMapping("/{id}/execute")
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
            Page<PropertyResponse> response = properties.map(this::convertToPropertyResponse);
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

    private PropertyResponse convertToPropertyResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getDescription(),
                property.getPrice(),
                property.getAgent().getId(),
                property.getAgent().getFullName(),
                property.getStatus(),
                property.getCreatedDate(),
                property.getUpdatedDate()
        );
    }
}
