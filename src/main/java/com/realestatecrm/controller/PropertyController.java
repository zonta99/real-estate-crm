package com.realestatecrm.controller;

import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyStatus;
import com.realestatecrm.service.PropertyService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PropertyController {

    private final PropertyService propertyService;
    private final UserService userService;

    @Autowired
    public PropertyController(PropertyService propertyService, UserService userService) {
        this.propertyService = propertyService;
        this.userService = userService;
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
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        Property property = propertyService.getPropertyById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

        return ResponseEntity.ok(convertToPropertyResponse(property));
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
    public ResponseEntity<PropertyValueResponse> setPropertyValue(
            @PathVariable Long id,
            @Valid @RequestBody SetPropertyValueRequest request) {

        PropertyValue value = propertyService.setPropertyValue(
                id,
                request.getAttributeId(),
                request.getValue()
        );

        return ResponseEntity.ok(convertToPropertyValueResponse(value));
    }

    @GetMapping("/{id}/values")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<List<PropertyValueResponse>> getPropertyValues(@PathVariable Long id) {
        List<PropertyValue> values = propertyService.getPropertyValues(id);
        List<PropertyValueResponse> responses = values.stream()
                .map(this::convertToPropertyValueResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}/values/{attributeId}")
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deletePropertyValue(
            @PathVariable Long id,
            @PathVariable Long attributeId) {

        propertyService.deletePropertyValue(id, attributeId);
        return ResponseEntity.ok(new MessageResponse("Property value deleted successfully"));
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

    private PropertyValueResponse convertToPropertyValueResponse(PropertyValue value) {
        return new PropertyValueResponse(
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

    // DTOs
    public static class CreatePropertyRequest {
        @NotBlank
        private String title;
        private String description;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
    }

    public static class UpdatePropertyRequest {
        @NotBlank
        private String title;
        private String description;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal price;

        @NotNull
        private PropertyStatus status;

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public PropertyStatus getStatus() { return status; }
        public void setStatus(PropertyStatus status) { this.status = status; }
    }

    public static class SetPropertyValueRequest {
        @NotNull
        private Long attributeId;
        private Object value;

        public Long getAttributeId() { return attributeId; }
        public void setAttributeId(Long attributeId) { this.attributeId = attributeId; }
        public Object getValue() { return value; }
        public void setValue(Object value) { this.value = value; }
    }

    public static class SharePropertyRequest {
        @NotNull
        private Long sharedWithUserId;

        public Long getSharedWithUserId() { return sharedWithUserId; }
        public void setSharedWithUserId(Long sharedWithUserId) { this.sharedWithUserId = sharedWithUserId; }
    }

    public static class PropertyResponse {
        private Long id;
        private String title;
        private String description;
        private BigDecimal price;
        private Long agentId;
        private String agentName;
        private PropertyStatus status;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public PropertyResponse(Long id, String title, String description, BigDecimal price,
                                Long agentId, String agentName, PropertyStatus status,
                                LocalDateTime createdDate, LocalDateTime updatedDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.price = price;
            this.agentId = agentId;
            this.agentName = agentName;
            this.status = status;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
        }

        // Getters
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public BigDecimal getPrice() { return price; }
        public Long getAgentId() { return agentId; }
        public String getAgentName() { return agentName; }
        public PropertyStatus getStatus() { return status; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
    }

    public static class PropertyValueResponse {
        private Long id;
        private Long propertyId;
        private Long attributeId;
        private String attributeName;
        private String dataType;
        private Object value;

        public PropertyValueResponse(Long id, Long propertyId, Long attributeId, String attributeName,
                                     Object dataType, Object value) {
            this.id = id;
            this.propertyId = propertyId;
            this.attributeId = attributeId;
            this.attributeName = attributeName;
            this.dataType = dataType.toString();
            this.value = value;
        }

        // Getters
        public Long getId() { return id; }
        public Long getPropertyId() { return propertyId; }
        public Long getAttributeId() { return attributeId; }
        public String getAttributeName() { return attributeName; }
        public String getDataType() { return dataType; }
        public Object getValue() { return value; }
    }

    public static class PropertySharingResponse {
        private Long id;
        private Long propertyId;
        private Long sharedWithUserId;
        private String sharedWithUserName;
        private Long sharedByUserId;
        private String sharedByUserName;
        private LocalDateTime createdDate;

        public PropertySharingResponse(Long id, Long propertyId, Long sharedWithUserId, String sharedWithUserName,
                                       Long sharedByUserId, String sharedByUserName, LocalDateTime createdDate) {
            this.id = id;
            this.propertyId = propertyId;
            this.sharedWithUserId = sharedWithUserId;
            this.sharedWithUserName = sharedWithUserName;
            this.sharedByUserId = sharedByUserId;
            this.sharedByUserName = sharedByUserName;
            this.createdDate = createdDate;
        }

        // Getters
        public Long getId() { return id; }
        public Long getPropertyId() { return propertyId; }
        public Long getSharedWithUserId() { return sharedWithUserId; }
        public String getSharedWithUserName() { return sharedWithUserName; }
        public Long getSharedByUserId() { return sharedByUserId; }
        public String getSharedByUserName() { return sharedByUserName; }
        public LocalDateTime getCreatedDate() { return createdDate; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}