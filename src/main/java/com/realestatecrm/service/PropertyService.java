package com.realestatecrm.service;

import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyValueRepository propertyValueRepository;
    private final PropertySharingRepository propertySharingRepository;
    private final UserRepository userRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository,
                           PropertyValueRepository propertyValueRepository,
                           PropertySharingRepository propertySharingRepository,
                           UserRepository userRepository,
                           PropertyAttributeRepository propertyAttributeRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyValueRepository = propertyValueRepository;
        this.propertySharingRepository = propertySharingRepository;
        this.userRepository = userRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
    }

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Property> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Property> getPropertiesByAgent(Long agentId) {
        return propertyRepository.findByAgentId(agentId);
    }

    @Transactional(readOnly = true)
    public Page<Property> getPropertiesByAgent(Long agentId, Pageable pageable) {
        return propertyRepository.findByAgentId(agentId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Property> getPropertiesByAgents(List<Long> agentIds) {
        return propertyRepository.findByAgentIdIn(agentIds);
    }

    @Transactional(readOnly = true)
    public Page<Property> getPropertiesByAgents(List<Long> agentIds, Pageable pageable) {
        return propertyRepository.findByAgentIdIn(agentIds, pageable);
    }

    @Transactional(readOnly = true)
    public List<Property> getAccessibleProperties(Long userId) {
        return propertyRepository.findAccessibleByAgent(userId);
    }

    public Property createProperty(Property property) {
        validateProperty(property);
        Property savedProperty = propertyRepository.save(property);
        return savedProperty;
    }

    public Property updateProperty(Long id, Property updatedProperty) {
        Property existingProperty = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + id));

        existingProperty.setTitle(updatedProperty.getTitle());
        existingProperty.setDescription(updatedProperty.getDescription());
        existingProperty.setPrice(updatedProperty.getPrice());
        existingProperty.setStatus(updatedProperty.getStatus());

        return propertyRepository.save(existingProperty);
    }

    public void deleteProperty(Long id) {
        if (!propertyRepository.existsById(id)) {
            throw new EntityNotFoundException("Property not found with id: " + id);
        }
        propertyRepository.deleteById(id);
    }

    public PropertyValue setPropertyValue(Long propertyId, Long attributeId, Object value) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + propertyId));

        PropertyAttribute attribute = propertyAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attributeId));

        validatePropertyValue(attribute, value);

        Optional<PropertyValue> existingValue = propertyValueRepository
                .findByPropertyIdAndAttributeId(propertyId, attributeId);

        PropertyValue propertyValue = existingValue.orElse(new PropertyValue(property, attribute));

        // Set value based on attribute type
        clearPropertyValueFields(propertyValue);
        switch (attribute.getDataType()) {
            case TEXT, SINGLE_SELECT -> propertyValue.setTextValue((String) value);
            case NUMBER -> propertyValue.setNumberValue((BigDecimal) value);
            case BOOLEAN -> propertyValue.setBooleanValue((Boolean) value);
            case MULTI_SELECT -> propertyValue.setMultiSelectValue((String) value);
        }

        return propertyValueRepository.save(propertyValue);
    }

    @Transactional(readOnly = true)
    public List<PropertyValue> getPropertyValues(Long propertyId) {
        return propertyValueRepository.findByPropertyId(propertyId);
    }

    public void deletePropertyValue(Long propertyId, Long attributeId) {
        propertyValueRepository.deleteByPropertyIdAndAttributeId(propertyId, attributeId);
    }

    public PropertySharing shareProperty(Long propertyId, Long sharedWithUserId, Long sharedByUserId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + propertyId));

        User sharedWithUser = userRepository.findById(sharedWithUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + sharedWithUserId));

        User sharedByUser = userRepository.findById(sharedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + sharedByUserId));

        // Validate that the sharing user owns the property
        if (!property.getAgent().getId().equals(sharedByUserId)) {
            throw new IllegalArgumentException("User can only share properties they own");
        }

        // Check if already shared
        if (propertySharingRepository.existsByPropertyIdAndSharedWithUserId(propertyId, sharedWithUserId)) {
            throw new IllegalArgumentException("Property is already shared with this user");
        }

        PropertySharing sharing = new PropertySharing(property, sharedWithUser, sharedByUser);
        return propertySharingRepository.save(sharing);
    }

    public void unshareProperty(Long propertyId, Long sharedWithUserId) {
        if (!propertySharingRepository.existsByPropertyIdAndSharedWithUserId(propertyId, sharedWithUserId)) {
            throw new EntityNotFoundException("Property sharing not found");
        }
        propertySharingRepository.deleteByPropertyIdAndSharedWithUserId(propertyId, sharedWithUserId);
    }

    @Transactional(readOnly = true)
    public List<PropertySharing> getPropertySharing(Long propertyId) {
        return propertySharingRepository.findByPropertyId(propertyId);
    }

    @Transactional(readOnly = true)
    public List<Property> searchPropertiesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return propertyRepository.findByPriceBetween(minPrice, maxPrice);
    }

    private void validateProperty(Property property) {
        if (property.getPrice() != null && property.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Property price must be positive");
        }

        if (property.getAgent() == null) {
            throw new IllegalArgumentException("Property must have an assigned agent");
        }
    }

    private void validatePropertyValue(PropertyAttribute attribute, Object value) {
        if (value == null && attribute.getIsRequired()) {
            throw new IllegalArgumentException("Value is required for attribute: " + attribute.getName());
        }

        if (value != null) {
            switch (attribute.getDataType()) {
                case TEXT, SINGLE_SELECT -> {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException("Expected String value for " + attribute.getDataType());
                    }
                }
                case NUMBER -> {
                    if (!(value instanceof BigDecimal)) {
                        throw new IllegalArgumentException("Expected BigDecimal value for NUMBER type");
                    }
                }
                case BOOLEAN -> {
                    if (!(value instanceof Boolean)) {
                        throw new IllegalArgumentException("Expected Boolean value for BOOLEAN type");
                    }
                }
                case MULTI_SELECT -> {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException("Expected JSON String for MULTI_SELECT type");
                    }
                }
            }
        }
    }

    private void clearPropertyValueFields(PropertyValue propertyValue) {
        propertyValue.setTextValue(null);
        propertyValue.setNumberValue(null);
        propertyValue.setBooleanValue(null);
        propertyValue.setMultiSelectValue(null);
    }
}