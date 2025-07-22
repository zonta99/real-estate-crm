package com.realestatecrm.controller;

import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.entity.PropertyAttributeOption;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.service.PropertyAttributeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/property-attributes")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PropertyAttributeController {

    private final PropertyAttributeService propertyAttributeService;

    @Autowired
    public PropertyAttributeController(PropertyAttributeService propertyAttributeService) {
        this.propertyAttributeService = propertyAttributeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<List<PropertyAttributeResponse>> getAllAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getAllAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/searchable")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<List<PropertyAttributeResponse>> getSearchableAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getSearchableAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<List<PropertyAttributeResponse>> getAttributesByCategory(@PathVariable PropertyCategory category) {
        List<PropertyAttribute> attributes = propertyAttributeService.getAttributesByCategory(category);
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<PropertyAttributeResponse> getAttributeById(@PathVariable Long id) {
        PropertyAttribute attribute = propertyAttributeService.getAttributeById(id)
                .orElseThrow(() -> new RuntimeException("Property attribute not found with id: " + id));

        return ResponseEntity.ok(convertToAttributeResponse(attribute));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyAttributeResponse> createAttribute(@Valid @RequestBody CreateAttributeRequest request) {
        PropertyAttribute attribute = new PropertyAttribute();
        attribute.setName(request.getName());
        attribute.setDataType(request.getDataType());
        attribute.setIsRequired(request.getIsRequired());
        attribute.setIsSearchable(request.getIsSearchable());
        attribute.setCategory(request.getCategory());
        attribute.setDisplayOrder(request.getDisplayOrder());

        PropertyAttribute createdAttribute = propertyAttributeService.createAttribute(attribute);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToAttributeResponse(createdAttribute));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PropertyAttributeResponse> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request) {

        PropertyAttribute attribute = new PropertyAttribute();
        attribute.setName(request.getName());
        attribute.setDataType(request.getDataType());
        attribute.setIsRequired(request.getIsRequired());
        attribute.setIsSearchable(request.getIsSearchable());
        attribute.setCategory(request.getCategory());
        attribute.setDisplayOrder(request.getDisplayOrder());

        PropertyAttribute updatedAttribute = propertyAttributeService.updateAttribute(id, attribute);
        return ResponseEntity.ok(convertToAttributeResponse(updatedAttribute));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAttribute(@PathVariable Long id) {
        propertyAttributeService.deleteAttribute(id);
        return ResponseEntity.ok(new MessageResponse("Property attribute deleted successfully"));
    }

    // Attribute Options Management
    @PostMapping("/{id}/options")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttributeOptionResponse> addAttributeOption(
            @PathVariable Long id,
            @Valid @RequestBody CreateAttributeOptionRequest request) {

        PropertyAttributeOption option = propertyAttributeService.addAttributeOption(
                id,
                request.getOptionValue(),
                request.getDisplayOrder()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToOptionResponse(option));
    }

    @GetMapping("/{id}/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<List<AttributeOptionResponse>> getAttributeOptions(@PathVariable Long id) {
        List<PropertyAttributeOption> options = propertyAttributeService.getAttributeOptions(id);
        List<AttributeOptionResponse> responses = options.stream()
                .map(this::convertToOptionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/options/{optionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteAttributeOption(@PathVariable Long optionId) {
        propertyAttributeService.deleteAttributeOption(optionId);
        return ResponseEntity.ok(new MessageResponse("Attribute option deleted successfully"));
    }

    @PutMapping("/category/{category}/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> reorderAttributes(
            @PathVariable PropertyCategory category,
            @Valid @RequestBody ReorderAttributesRequest request) {

        propertyAttributeService.reorderAttributes(category, request.getAttributeIds());
        return ResponseEntity.ok(new MessageResponse("Attributes reordered successfully"));
    }

    private PropertyAttributeResponse convertToAttributeResponse(PropertyAttribute attribute) {
        List<AttributeOptionResponse> options = null;
        if (attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            options = attribute.getOptions().stream()
                    .map(this::convertToOptionResponse)
                    .collect(Collectors.toList());
        }

        return new PropertyAttributeResponse(
                attribute.getId(),
                attribute.getName(),
                attribute.getDataType().toString(),
                attribute.getIsRequired(),
                attribute.getIsSearchable(),
                attribute.getCategory().toString(),
                attribute.getDisplayOrder(),
                attribute.getCreatedDate(),
                attribute.getUpdatedDate(),
                options
        );
    }

    private AttributeOptionResponse convertToOptionResponse(PropertyAttributeOption option) {
        return new AttributeOptionResponse(
                option.getId(),
                option.getAttribute().getId(),
                option.getOptionValue(),
                option.getDisplayOrder()
        );
    }

    // DTOs
    public static class CreateAttributeRequest {
        @NotBlank
        private String name;

        @NotNull
        private PropertyDataType dataType;

        @NotNull
        private Boolean isRequired = false;

        @NotNull
        private Boolean isSearchable = true;

        @NotNull
        private PropertyCategory category;

        private Integer displayOrder;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public PropertyDataType getDataType() { return dataType; }
        public void setDataType(PropertyDataType dataType) { this.dataType = dataType; }
        public Boolean getIsRequired() { return isRequired; }
        public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
        public Boolean getIsSearchable() { return isSearchable; }
        public void setIsSearchable(Boolean isSearchable) { this.isSearchable = isSearchable; }
        public PropertyCategory getCategory() { return category; }
        public void setCategory(PropertyCategory category) { this.category = category; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class UpdateAttributeRequest extends CreateAttributeRequest {
        // Inherits all fields from CreateAttributeRequest
    }

    public static class CreateAttributeOptionRequest {
        @NotBlank
        private String optionValue;
        private Integer displayOrder;

        public String getOptionValue() { return optionValue; }
        public void setOptionValue(String optionValue) { this.optionValue = optionValue; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    }

    public static class ReorderAttributesRequest {
        @NotNull
        private List<Long> attributeIds;

        public List<Long> getAttributeIds() { return attributeIds; }
        public void setAttributeIds(List<Long> attributeIds) { this.attributeIds = attributeIds; }
    }

    public static class PropertyAttributeResponse {
        private Long id;
        private String name;
        private String dataType;
        private Boolean isRequired;
        private Boolean isSearchable;
        private String category;
        private Integer displayOrder;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;
        private List<AttributeOptionResponse> options;

        public PropertyAttributeResponse(Long id, String name, String dataType, Boolean isRequired,
                                         Boolean isSearchable, String category, Integer displayOrder,
                                         LocalDateTime createdDate, LocalDateTime updatedDate,
                                         List<AttributeOptionResponse> options) {
            this.id = id;
            this.name = name;
            this.dataType = dataType;
            this.isRequired = isRequired;
            this.isSearchable = isSearchable;
            this.category = category;
            this.displayOrder = displayOrder;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
            this.options = options;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDataType() { return dataType; }
        public Boolean getIsRequired() { return isRequired; }
        public Boolean getIsSearchable() { return isSearchable; }
        public String getCategory() { return category; }
        public Integer getDisplayOrder() { return displayOrder; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
        public List<AttributeOptionResponse> getOptions() { return options; }
    }

    public static class AttributeOptionResponse {
        private Long id;
        private Long attributeId;
        private String optionValue;
        private Integer displayOrder;

        public AttributeOptionResponse(Long id, Long attributeId, String optionValue, Integer displayOrder) {
            this.id = id;
            this.attributeId = attributeId;
            this.optionValue = optionValue;
            this.displayOrder = displayOrder;
        }

        // Getters
        public Long getId() { return id; }
        public Long getAttributeId() { return attributeId; }
        public String getOptionValue() { return optionValue; }
        public Integer getDisplayOrder() { return displayOrder; }
    }

    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
    }
}