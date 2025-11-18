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
import com.realestatecrm.service.PropertyService;
import com.realestatecrm.service.SavedSearchService;
import com.realestatecrm.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserService userService;
    private final SavedSearchService savedSearchService;

    @Autowired
    public PropertyController(PropertyService propertyService, UserService userService, SavedSearchService savedSearchService) {
        this.propertyService = propertyService;
        this.userService = userService;
        this.savedSearchService = savedSearchService;
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
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Page<Property> properties;
        if (currentUser.getRole().name().equals("ADMIN")) {
            properties = propertyService.getAllProperties(pageable);
        } else {
            // Get properties for current user and their subordinates
            List<User> accessibleUsers = userService.getAccessibleUsers(currentUser.getId());
            List<Long> userIds = accessibleUsers.stream().map(User::getId).collect(Collectors.toList());
            properties = propertyService.getPropertiesByAgents(userIds, pageable);
        }

        Page<PropertyResponse> response = properties.map(this::convertToPropertyResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

        // Return basic property info without attributes for better performance
        return ResponseEntity.ok(convertToPropertyResponse(property));
    }

    @GetMapping("/{id}/with-attributes")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyResponse> getPropertyByIdWithAttributes(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

        // Return complete property info including all dynamic attributes
        return ResponseEntity.ok(convertToPropertyResponseWithAttributes(property));
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> createProperty(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePropertyRequest request) {

        User currentUser = userService.getUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setAgent(currentUser);
        property.setStatus(PropertyStatus.ACTIVE);

        Property createdProperty = propertyService.createProperty(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToPropertyResponse(createdProperty));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<PropertyResponse> updateProperty(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {

        Property property = new Property();
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPrice(request.getPrice());
        property.setStatus(request.getStatus());

        Property updatedProperty = propertyService.updateProperty(id, property);
        return ResponseEntity.ok(convertToPropertyResponse(updatedProperty));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok(new MessageResponse("Property deleted successfully"));
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

        return ResponseEntity.ok(convertToAttributeValueResponse(value));
    }

    @GetMapping("/{id}/values")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AttributeValueResponse>> getAttributeValues(@PathVariable Long id) {
        List<AttributeValue> values = propertyService.getAttributeValues(id);
        List<AttributeValueResponse> responses = values.stream()
                .map(this::convertToAttributeValueResponse)
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
                .orElseThrow(() -> new RuntimeException("Current user not found"));

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
                .map(this::convertToPropertySharingResponse)
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
                .map(this::convertToPropertyResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/search/by-criteria")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<?> searchPropertiesByCriteria(
            @Valid @RequestBody PropertySearchCriteriaRequest request) {

        try {
            Page<Property> properties = savedSearchService.executeSearch(request);
            Page<PropertyResponse> response = properties.map(this::convertToPropertyResponse);
            return ResponseEntity.ok(response);
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

    private PropertyResponse convertToPropertyResponseWithAttributes(Property property) {
        List<AttributeValue> values = propertyService.getAttributeValues(property.getId());
        List<AttributeValueResponse> valueResponses = values.stream()
                .map(this::convertToAttributeValueResponse)
                .collect(Collectors.toList());

        return new PropertyResponse(
                property.getId(),
                property.getTitle(),
                property.getDescription(),
                property.getPrice(),
                property.getAgent().getId(),
                property.getAgent().getFullName(),
                property.getStatus(),
                property.getCreatedDate(),
                property.getUpdatedDate(),
                valueResponses
        );
    }

    private AttributeValueResponse convertToAttributeValueResponse(AttributeValue value) {
        return new AttributeValueResponse(
                value.getId(),
                value.getProperty().getId(),
                value.getAttribute().getId(),
                value.getAttribute().getName(),
                value.getAttribute().getDataType(),
                value.getValue()
        );
    }

    private PropertySharingResponse convertToPropertySharingResponse(PropertySharing sharing) {
        return new PropertySharingResponse(
                sharing.getId(),
                sharing.getProperty().getId(),
                sharing.getSharedWithUser().getId(),
                sharing.getSharedWithUser().getFullName(),
                sharing.getSharedByUser().getId(),
                sharing.getSharedByUser().getFullName(),
                sharing.getCreatedDate()
        );
    }
}