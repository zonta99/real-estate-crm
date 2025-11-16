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
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final PropertySharingRepository propertySharingRepository;
    private final UserRepository userRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository,
                           AttributeValueRepository attributeValueRepository,
                           PropertySharingRepository propertySharingRepository,
                           UserRepository userRepository,
                           PropertyAttributeRepository propertyAttributeRepository) {
        this.propertyRepository = propertyRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.propertySharingRepository = propertySharingRepository;
        this.userRepository = userRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
    }

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        // LAZY FIX: Use findAllWithAgent to eagerly fetch agent relationship
        return propertyRepository.findAllWithAgent();
    }

    @Transactional(readOnly = true)
    public Page<Property> getAllProperties(Pageable pageable) {
        // LAZY FIX: Use findAllWithAgent to eagerly fetch agent relationship
        return propertyRepository.findAllWithAgent(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Property> getPropertyById(Long id) {
        // LAZY FIX: Use findByIdWithAgent to eagerly fetch agent relationship
        return propertyRepository.findByIdWithAgent(id);
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
        // LAZY FIX: Use findByIdWithAgent when we might access agent later
        Property existingProperty = propertyRepository.findByIdWithAgent(id)
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

    public AttributeValue setAttributeValue(Long propertyId, Long attributeId, Object value) {
        // LAZY FIX: Use findByIdWithAgent when we might access agent later
        Property property = propertyRepository.findByIdWithAgent(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + propertyId));

        PropertyAttribute attribute = propertyAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attributeId));

        validateAttributeValue(attribute, value);

        Optional<AttributeValue> existingValue = attributeValueRepository
                .findByPropertyIdAndAttributeId(propertyId, attributeId);

        AttributeValue attributeValue = existingValue.orElse(new AttributeValue(property, attribute));

        // Set value based on attribute type
        clearAttributeValueFields(attributeValue);
        switch (attribute.getDataType()) {
            case TEXT, SINGLE_SELECT -> attributeValue.setTextValue((String) value);
            case NUMBER -> attributeValue.setNumberValue(coerceToBigDecimal(value));
            case BOOLEAN -> attributeValue.setBooleanValue((Boolean) value);
            case MULTI_SELECT -> attributeValue.setMultiSelectValue((String) value);
        }

        return attributeValueRepository.save(attributeValue);
    }

    @Transactional(readOnly = true)
    public List<AttributeValue> getAttributeValues(Long propertyId) {
        return attributeValueRepository.findByPropertyId(propertyId);
    }

    public void deleteAttributeValue(Long propertyId, Long attributeId) {
        attributeValueRepository.deleteByPropertyIdAndAttributeId(propertyId, attributeId);
    }

    public PropertySharing shareProperty(Long propertyId, Long sharedWithUserId, Long sharedByUserId) {
        // LAZY FIX: Use findByIdWithAgent since we access property.getAgent() later (line 156)
        Property property = propertyRepository.findByIdWithAgent(propertyId)
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

    private void validateAttributeValue(PropertyAttribute attribute, Object value) {
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
                    // Be flexible: accept BigDecimal, any Number (Integer, Long, Double, etc.), or a numeric String
                    if (!(value instanceof BigDecimal) &&
                        !(value instanceof Number) &&
                        !(value instanceof String && isNumericString((String) value))) {
                        throw new IllegalArgumentException("Expected numeric value (BigDecimal/Number/String) for NUMBER type");
                    }
                    // Additionally, try coercion to validate convertibility
                    coerceToBigDecimal(value); // will throw if not convertible
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

    private BigDecimal coerceToBigDecimal(Object value) {
        if (value == null) return null;
        BigDecimal bd;
        if (value instanceof BigDecimal b) {
            bd = b;
        } else if (value instanceof Number n) {
            // Use String constructor to preserve exact representation
            bd = new BigDecimal(n.toString());
        } else if (value instanceof String s) {
            String trimmed = s.trim();
            if (!isNumericString(trimmed)) {
                throw new IllegalArgumentException("Invalid numeric string: '" + s + "'");
            }
            bd = new BigDecimal(trimmed);
        } else {
            throw new IllegalArgumentException("Unsupported numeric value type: " + value.getClass().getSimpleName());
        }
        // Normalize to the DB scale (2) with HALF_UP rounding to avoid persistence issues
        return bd.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isNumericString(String s) {
        if (s == null) return false;
        String str = s.trim();
        if (str.isEmpty()) return false;
        // Basic check for integer/decimal with optional sign
        return str.matches("[+-]?\\d+(\\.\\d+)?");
    }

    private void clearAttributeValueFields(AttributeValue attributeValue) {
        attributeValue.setTextValue(null);
        attributeValue.setNumberValue(null);
        attributeValue.setBooleanValue(null);
        attributeValue.setMultiSelectValue(null);
    }
}