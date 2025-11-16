package com.realestatecrm.controller;

import com.realestatecrm.dto.common.MessageResponse;
import com.realestatecrm.dto.propertyattribute.request.CreateAttributeOptionRequest;
import com.realestatecrm.dto.propertyattribute.request.CreateAttributeRequest;
import com.realestatecrm.dto.propertyattribute.request.ReorderAttributesRequest;
import com.realestatecrm.dto.propertyattribute.request.UpdateAttributeRequest;
import com.realestatecrm.dto.propertyattribute.response.AttributeOptionResponse;
import com.realestatecrm.dto.propertyattribute.response.PropertyAttributeResponse;
import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.entity.PropertyAttributeOption;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.service.PropertyAttributeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/property-attributes")
public class PropertyAttributeController {

    private final PropertyAttributeService propertyAttributeService;

    @Autowired
    public PropertyAttributeController(PropertyAttributeService propertyAttributeService) {
        this.propertyAttributeService = propertyAttributeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getAllAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getAllAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/searchable")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getSearchableAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getSearchableAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getAttributesByCategory(@PathVariable PropertyCategory category) {
        List<PropertyAttribute> attributes = propertyAttributeService.getAttributesByCategory(category);
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(this::convertToAttributeResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyAttributeResponse> getAttributeById(@PathVariable Long id) {
        PropertyAttribute attribute = propertyAttributeService.getAttributeById(id)
                .orElseThrow(() -> new RuntimeException("Property attribute not found with id: " + id));

        return ResponseEntity.ok(convertToAttributeResponse(attribute));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
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
    @Transactional
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
    
}