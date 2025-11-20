package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.property.request.CreatePropertyRequest;
import com.realestatecrm.dto.property.request.SetAttributeValueRequest;
import com.realestatecrm.dto.property.request.SharePropertyRequest;
import com.realestatecrm.dto.property.request.UpdatePropertyRequest;
import com.realestatecrm.dto.property.response.PropertyResponse;
import com.realestatecrm.dto.property.response.PropertySharingResponse;
import com.realestatecrm.dto.property.response.AttributeValueResponse;
import com.realestatecrm.dto.savedsearch.PropertySearchCriteriaRequest;
import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyStatus;
import com.realestatecrm.mapper.PropertyMapper;
import com.realestatecrm.service.PropertyService;
import com.realestatecrm.service.SavedSearchService;
import com.realestatecrm.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserService userService;
    private final SavedSearchService savedSearchService;
    private final PropertyMapper propertyMapper;

    @Autowired
    public PropertyController(PropertyService propertyService, UserService userService,
                            SavedSearchService savedSearchService, PropertyMapper propertyMapper) {
        this.propertyService = propertyService;
        this.userService = userService;
        this.savedSearchService = savedSearchService;
        this.propertyMapper = propertyMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PropertyResponse>> getAllProperties(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        Page<Property> properties;
        if (currentUser.getRole().name().equals("ADMIN")) {
            properties = propertyService.getAllProperties(pageable);
        } else {
            // Get properties for current user and their subordinates
            List<User> accessibleUsers = userService.getAccessibleUsers(currentUser.getId());
            List<Long> userIds = accessibleUsers.stream().map(User::getId).collect(Collectors.toList());
            properties = propertyService.getPropertiesByAgents(userIds, pageable);
        }

        Page<PropertyResponse> response = properties.map(propertyMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + id));

        // Return basic property info without attributes for better performance
        return ResponseEntity.ok(propertyMapper.toResponse(property));
    }

    @GetMapping("/{id}/with-attributes")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyResponse> getPropertyByIdWithAttributes(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + id));

        // Return complete property info including all dynamic attributes
        List<AttributeValue> values = propertyService.getAttributeValues(property.getId());
        return ResponseEntity.ok(propertyMapper.toResponseWithAttributes(property, values));
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePropertyRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        Property property = propertyMapper.toEntity(request);
        property.setAgent(currentUser);
        property.setStatus(PropertyStatus.ACTIVE);

        Property createdProperty = propertyService.createProperty(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyMapper.toResponse(createdProperty));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {

        Property property = propertyMapper.toEntity(request);
        Property updatedProperty = propertyService.updateProperty(id, property);
        return ResponseEntity.ok(propertyMapper.toResponse(updatedProperty));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok(new MessageResponse("Property deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updatePropertyStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyStatusRequest request) {
        Property property = propertyService.updatePropertyStatus(id, request.status());
        return ResponseEntity.ok(propertyMapper.toResponse(property));
    }

    @PostMapping("/{id}/values")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<AttributeValueResponse> setAttributeValue(
            @PathVariable Long id,
            @Valid @RequestBody SetAttributeValueRequest request) {

        AttributeValue value = propertyService.setAttributeValue(
                id,
                request.getAttributeId(),
                request.getValue()
        );

        return ResponseEntity.ok(propertyMapper.toAttributeValueResponse(value));
    }

    @GetMapping("/{id}/values")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AttributeValueResponse>> getAttributeValues(@PathVariable Long id) {
        List<AttributeValue> values = propertyService.getAttributeValues(id);
        List<AttributeValueResponse> responses = values.stream()
                .map(propertyMapper::toAttributeValueResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/values/{attributeId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAttributeValue(
            @PathVariable Long id,
            @PathVariable Long attributeId) {

        propertyService.deleteAttributeValue(id, attributeId);
        return ResponseEntity.ok(new MessageResponse("Attribute value deleted successfully"));
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> shareProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SharePropertyRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));

        propertyService.shareProperty(id, request.getSharedWithUserId(), currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Property shared successfully"));
    }

    @DeleteMapping("/{id}/share/{userId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> unshareProperty(
            @PathVariable Long id,
            @PathVariable Long userId) {

        propertyService.unshareProperty(id, userId);
        return ResponseEntity.ok(new MessageResponse("Property unshared successfully"));
    }

    @GetMapping("/{id}/sharing")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<PropertySharingResponse>> getPropertySharing(@PathVariable Long id) {
        List<PropertySharing> sharing = propertyService.getPropertySharing(id);
        List<PropertySharingResponse> responses = sharing.stream()
                .map(propertyMapper::toPropertySharingResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<PropertyResponse>> searchProperties(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) PropertyStatus status) {

        List<Property> properties;

        if (minPrice != null && maxPrice != null) {
            properties = propertyService.searchPropertiesByPriceRange(minPrice, maxPrice);
        } else {
            properties = propertyService.getAllProperties();
        }

        if (status != null) {
            properties = properties.stream()
                    .filter(p -> p.getStatus() == status)
                    .collect(Collectors.toList());
        }

        List<PropertyResponse> responses = properties.stream()
                .map(propertyMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search/by-criteria")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> searchPropertiesByCriteria(
            @Valid @RequestBody PropertySearchCriteriaRequest request) {

        try {
            Page<Property> properties = savedSearchService.executeSearch(request);
            Page<PropertyResponse> response = properties.map(propertyMapper::toResponse);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}