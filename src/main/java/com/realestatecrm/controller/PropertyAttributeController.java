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
import com.realestatecrm.mapper.PropertyAttributeMapper;
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
    private final PropertyAttributeMapper attributeMapper;

    @Autowired
    public PropertyAttributeController(PropertyAttributeService propertyAttributeService, PropertyAttributeMapper attributeMapper) {
        this.propertyAttributeService = propertyAttributeService;
        this.attributeMapper = attributeMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getAllAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getAllAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/searchable")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getSearchableAttributes() {
        List<PropertyAttribute> attributes = propertyAttributeService.getSearchableAttributes();
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<List<PropertyAttributeResponse>> getAttributesByCategory(@PathVariable PropertyCategory category) {
        List<PropertyAttribute> attributes = propertyAttributeService.getAttributesByCategory(category);
        List<PropertyAttributeResponse> responses = attributes.stream()
                .map(attributeMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    @Transactional(readOnly = true)
    public ResponseEntity<PropertyAttributeResponse> getAttributeById(@PathVariable Long id) {
        PropertyAttribute attribute = propertyAttributeService.getAttributeById(id)
                .orElseThrow(() -> new RuntimeException("Property attribute not found with id: " + id));

        return ResponseEntity.ok(attributeMapper.toResponse(attribute));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<PropertyAttributeResponse> createAttribute(@Valid @RequestBody CreateAttributeRequest request) {
        PropertyAttribute attribute = attributeMapper.toEntity(request);
        PropertyAttribute createdAttribute = propertyAttributeService.createAttribute(attribute);
        return ResponseEntity.status(HttpStatus.CREATED).body(attributeMapper.toResponse(createdAttribute));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<PropertyAttributeResponse> updateAttribute(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttributeRequest request) {

        PropertyAttribute attribute = attributeMapper.toEntity(request);
        PropertyAttribute updatedAttribute = propertyAttributeService.updateAttribute(id, attribute);
        return ResponseEntity.ok(attributeMapper.toResponse(updatedAttribute));
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

        return ResponseEntity.status(HttpStatus.CREATED).body(attributeMapper.toOptionResponse(option));
    }

    @GetMapping("/{id}/options")
    @PreAuthorize("hasAnyRole('ADMIN', 'BROKER', 'AGENT', 'ASSISTANT')")
    public ResponseEntity<List<AttributeOptionResponse>> getAttributeOptions(@PathVariable Long id) {
        List<PropertyAttributeOption> options = propertyAttributeService.getAttributeOptions(id);
        List<AttributeOptionResponse> responses = options.stream()
                .map(attributeMapper::toOptionResponse)
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
}