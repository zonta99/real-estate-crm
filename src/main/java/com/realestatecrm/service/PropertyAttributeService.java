package com.realestatecrm.service;

import com.realestatecrm.entity.PropertyAttribute;
import com.realestatecrm.entity.PropertyAttributeOption;
import com.realestatecrm.enums.PropertyCategory;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.repository.PropertyAttributeRepository;
import com.realestatecrm.repository.PropertyAttributeOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyAttributeService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyAttributeService.class);

    private final PropertyAttributeRepository propertyAttributeRepository;
    private final PropertyAttributeOptionRepository propertyAttributeOptionRepository;

    @Autowired
    public PropertyAttributeService(PropertyAttributeRepository propertyAttributeRepository,
                                    PropertyAttributeOptionRepository propertyAttributeOptionRepository) {
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.propertyAttributeOptionRepository = propertyAttributeOptionRepository;
    }

    @Transactional(readOnly = true)
    public List<PropertyAttribute> getAllAttributes() {
        return propertyAttributeRepository.findAllOrderedByDisplay();
    }

    @Transactional(readOnly = true)
    public List<PropertyAttribute> getSearchableAttributes() {
        return propertyAttributeRepository.findSearchableOrderedByDisplay();
    }

    @Transactional(readOnly = true)
    public List<PropertyAttribute> getAttributesByCategory(PropertyCategory category) {
        return propertyAttributeRepository.findByCategoryOrderByDisplayOrderAsc(category);
    }

    @Transactional(readOnly = true)
    public Optional<PropertyAttribute> getAttributeById(Long id) {
        return propertyAttributeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return propertyAttributeRepository.existsByName(name);
    }

    public PropertyAttribute createAttribute(PropertyAttribute attribute) {
        validateAttribute(attribute);

        if (existsByName(attribute.getName())) {
            throw new IllegalArgumentException("Attribute with name '" + attribute.getName() + "' already exists");
        }

        // Set display order if not provided
        if (attribute.getDisplayOrder() == null) {
            List<PropertyAttribute> categoryAttributes = propertyAttributeRepository
                    .findByCategoryOrderByDisplayOrderAsc(attribute.getCategory());
            int nextOrder = categoryAttributes.stream()
                    .mapToInt(attr -> attr.getDisplayOrder() != null ? attr.getDisplayOrder() : 0)
                    .max().orElse(0) + 1;
            attribute.setDisplayOrder(nextOrder);
        }

        PropertyAttribute savedAttribute = propertyAttributeRepository.save(attribute);

        // Create default options for select types if provided
        if (attribute.requiresOptions() && attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            for (PropertyAttributeOption option : attribute.getOptions()) {
                option.setAttribute(savedAttribute);
                propertyAttributeOptionRepository.save(option);
            }
        }

        return savedAttribute;
    }

    public PropertyAttribute updateAttribute(Long id, PropertyAttribute updatedAttribute) {
        PropertyAttribute existingAttribute = propertyAttributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + id));

        validateAttributeUpdate(existingAttribute, updatedAttribute);

        // Update basic properties
        existingAttribute.setName(updatedAttribute.getName());
        existingAttribute.setIsRequired(updatedAttribute.getIsRequired());
        existingAttribute.setIsSearchable(updatedAttribute.getIsSearchable());
        existingAttribute.setCategory(updatedAttribute.getCategory());
        existingAttribute.setDisplayOrder(updatedAttribute.getDisplayOrder());

        // Note: Data type should generally not be changed after creation due to existing values
        // In a production system, you might want to prevent this or handle data migration
        if (updatedAttribute.getDataType() != existingAttribute.getDataType()) {
            validateDataTypeChange(existingAttribute, updatedAttribute.getDataType());
            existingAttribute.setDataType(updatedAttribute.getDataType());
        }

        return propertyAttributeRepository.save(existingAttribute);
    }

    public void deleteAttribute(Long id) {
        PropertyAttribute attribute = propertyAttributeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + id));

        // Check if attribute is in use by any properties
        if (!attribute.getAttributeValues().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete attribute '" + attribute.getName() +
                    "' because it is used by " + attribute.getAttributeValues().size() + " properties");
        }

        propertyAttributeRepository.deleteById(id);
    }

    // Option management methods
    public PropertyAttributeOption addAttributeOption(Long attributeId, String optionValue, Integer displayOrder) {
        PropertyAttribute attribute = propertyAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attributeId));

        if (!attribute.requiresOptions()) {
            throw new IllegalArgumentException("Attribute '" + attribute.getName() + "' does not support options");
        }

        PropertyAttributeOption option = new PropertyAttributeOption(attribute, optionValue, displayOrder);
        return propertyAttributeOptionRepository.save(option);
    }

    @Transactional(readOnly = true)
    public List<PropertyAttributeOption> getAttributeOptions(Long attributeId) {
        return propertyAttributeOptionRepository.findByAttributeIdOrderByDisplayOrder(attributeId);
    }

    public void deleteAttributeOption(Long optionId) {
        PropertyAttributeOption option = propertyAttributeOptionRepository.findById(optionId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute option not found with id: " + optionId));

        // In a production system, you might want to check if this option is used in property values
        propertyAttributeOptionRepository.deleteById(optionId);
    }

    public void reorderAttributes(PropertyCategory category, List<Long> attributeIds) {
        for (int i = 0; i < attributeIds.size(); i++) {
            Long attributeId = attributeIds.get(i);
            PropertyAttribute attribute = propertyAttributeRepository.findById(attributeId)
                    .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attributeId));

            if (attribute.getCategory() != category) {
                throw new IllegalArgumentException("Attribute " + attributeId + " is not in category " + category);
            }

            attribute.setDisplayOrder(i + 1);
            propertyAttributeRepository.save(attribute);
        }
    }

    private void validateAttribute(PropertyAttribute attribute) {
        if (attribute.getName() == null || attribute.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name cannot be empty");
        }

        if (attribute.getDataType() == null) {
            throw new IllegalArgumentException("Attribute data type must be specified");
        }

        if (attribute.getCategory() == null) {
            throw new IllegalArgumentException("Attribute category must be specified");
        }

        if (attribute.getIsRequired() == null) {
            attribute.setIsRequired(false);
        }

        if (attribute.getIsSearchable() == null) {
            attribute.setIsSearchable(true);
        }

        // Validate that select types have options if creating with options
        if (attribute.requiresOptions() && attribute.getOptions() != null) {
            if (attribute.getOptions().isEmpty()) {
                throw new IllegalArgumentException("Select type attributes must have at least one option");
            }
        }
    }

    private void validateAttributeUpdate(PropertyAttribute existing, PropertyAttribute updated) {
        if (updated.getName() != null && !updated.getName().equals(existing.getName())
                && existsByName(updated.getName())) {
            throw new IllegalArgumentException("Attribute with name '" + updated.getName() + "' already exists");
        }
    }

    private void validateDataTypeChange(PropertyAttribute existing, PropertyDataType newDataType) {
        // In production, you would implement proper data migration logic here
        // For now, we'll allow it but log a warning
        logger.warn("DATA TYPE CHANGE: Attribute '{}' data type changing from {} to {}. " +
                "Existing property values may become incompatible.",
                existing.getName(), existing.getDataType(), newDataType);
    }
}