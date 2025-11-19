package com.realestatecrm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.realestatecrm.dto.savedsearch.PropertySearchCriteriaRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchRequest;
import com.realestatecrm.dto.savedsearch.SavedSearchResponse;
import com.realestatecrm.dto.savedsearch.SearchFilterDTO;
import com.realestatecrm.entity.*;
import com.realestatecrm.enums.PropertyDataType;
import com.realestatecrm.enums.PropertyStatus;
import com.realestatecrm.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final CustomerRepository customerRepository;
    private final PropertyRepository propertyRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SavedSearchService(SavedSearchRepository savedSearchRepository,
                              CustomerRepository customerRepository,
                              PropertyRepository propertyRepository,
                              AttributeValueRepository attributeValueRepository,
                              PropertyAttributeRepository propertyAttributeRepository) {
        this.savedSearchRepository = savedSearchRepository;
        this.customerRepository = customerRepository;
        this.propertyRepository = propertyRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getAllSavedSearches() {
        return savedSearchRepository.findAllWithCustomer().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getSavedSearchesByCustomer(Long customerId) {
        return savedSearchRepository.findByCustomerIdWithCustomer(customerId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getSavedSearchesByAgent(Long agentId) {
        return savedSearchRepository.findByCustomerAgentId(agentId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SavedSearchResponse> getSavedSearchById(Long id) {
        return savedSearchRepository.findByIdWithCustomer(id)
                .map(this::convertToResponse);
    }

    public SavedSearchResponse createSavedSearch(Long customerId, SavedSearchRequest request) {
        validateSavedSearchRequest(request);

        Customer customer = customerRepository.findByIdWithAgent(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(request.getFilters());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize filters to JSON", e);
        }

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setCustomer(customer);
        savedSearch.setName(request.getName());
        savedSearch.setDescription(request.getDescription());
        savedSearch.setFiltersJson(filtersJson);

        SavedSearch saved = savedSearchRepository.save(savedSearch);
        return convertToResponse(savedSearchRepository.findByIdWithCustomer(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found after creation")));
    }

    public SavedSearchResponse updateSavedSearch(Long id, Long agentId, SavedSearchRequest request) {
        validateSavedSearchRequest(request);

        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + id));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            throw new SecurityException("Access denied: You can only update saved searches for your own customers");
        }

        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(request.getFilters());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize filters to JSON", e);
        }

        savedSearch.setName(request.getName());
        savedSearch.setDescription(request.getDescription());
        savedSearch.setFiltersJson(filtersJson);

        SavedSearch updated = savedSearchRepository.save(savedSearch);
        return convertToResponse(updated);
    }

    public void deleteSavedSearch(Long id, Long agentId) {
        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + id));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            throw new SecurityException("Access denied: You can only delete saved searches for your own customers");
        }

        savedSearchRepository.delete(savedSearch);
    }

    @Transactional(readOnly = true)
    public Page<Property> executeSearch(PropertySearchCriteriaRequest searchRequest) {
        validateSearchRequest(searchRequest);

        // Get all ACTIVE properties
        List<Property> allProperties = propertyRepository.findByStatus(PropertyStatus.ACTIVE);

        // Filter properties based on search criteria
        List<Property> matchedProperties = allProperties.stream()
                .filter(property -> matchesAllFilters(property, searchRequest.getFilters()))
                .collect(Collectors.toList());

        // Apply sorting and pagination
        Pageable pageable = createPageable(searchRequest);

        // Manual pagination since we're filtering in memory
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchedProperties.size());

        List<Property> pageContent = start < matchedProperties.size()
            ? matchedProperties.subList(start, end)
            : List.of();

        return new PageImpl<>(pageContent, pageable, matchedProperties.size());
    }

    @Transactional(readOnly = true)
    public Page<Property> executeSavedSearch(Long searchId, Long agentId, Integer page, Integer size, String sort) {
        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(searchId)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + searchId));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            throw new SecurityException("Access denied: You can only execute saved searches for your own customers");
        }

        // Deserialize filters from JSON
        List<SearchFilterDTO> filters;
        try {
            filters = objectMapper.readValue(savedSearch.getFiltersJson(),
                new TypeReference<List<SearchFilterDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize filters from JSON", e);
        }

        // Create search request
        PropertySearchCriteriaRequest searchRequest = new PropertySearchCriteriaRequest();
        searchRequest.setFilters(filters);
        searchRequest.setPage(page != null ? page : 0);
        searchRequest.setSize(size != null ? size : 20);
        searchRequest.setSort(sort != null ? sort : "createdDate,desc");

        return executeSearch(searchRequest);
    }

    private boolean matchesAllFilters(Property property, List<SearchFilterDTO> filters) {
        for (SearchFilterDTO filter : filters) {
            if (!matchesFilter(property, filter)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesFilter(Property property, SearchFilterDTO filter) {
        Optional<AttributeValue> attributeValue = attributeValueRepository
                .findByPropertyIdAndAttributeId(property.getId(), filter.getAttributeId());

        if (attributeValue.isEmpty()) {
            return false; // Property doesn't have this attribute value
        }

        AttributeValue value = attributeValue.get();

        return switch (filter.getDataType()) {
            case NUMBER -> matchesNumberFilter(value.getNumberValue(), filter.getMinValue(), filter.getMaxValue());
            case DATE -> matchesDateFilter(value.getDateValue(), filter.getMinDate(), filter.getMaxDate());
            case TEXT -> matchesTextFilter(value.getTextValue(), filter.getTextValue());
            case SINGLE_SELECT -> matchesSingleSelectFilter(value.getTextValue(), filter.getSelectedValues());
            case MULTI_SELECT -> matchesMultiSelectFilter(value.getMultiSelectValue(), filter.getSelectedValues());
            case BOOLEAN -> matchesBooleanFilter(value.getBooleanValue(), filter.getBooleanValue());
        };
    }

    private boolean matchesNumberFilter(BigDecimal propertyValue, BigDecimal minValue, BigDecimal maxValue) {
        if (propertyValue == null) return false;

        if (minValue != null && propertyValue.compareTo(minValue) < 0) {
            return false;
        }

        if (maxValue != null && propertyValue.compareTo(maxValue) > 0) {
            return false;
        }

        return true;
    }

    private boolean matchesDateFilter(Date propertyDateValue, LocalDate minDate, LocalDate maxDate) {
        if (propertyDateValue == null) return false;

        // Convert Date to LocalDate for comparison
        LocalDate propertyDate = propertyDateValue.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (minDate != null && propertyDate.isBefore(minDate)) {
            return false;
        }

        if (maxDate != null && propertyDate.isAfter(maxDate)) {
            return false;
        }

        return true;
    }

    private boolean matchesTextFilter(String propertyValue, String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) return true;
        if (propertyValue == null) return false;

        // Case-insensitive contains search
        return propertyValue.toLowerCase().contains(searchText.toLowerCase());
    }

    private boolean matchesSingleSelectFilter(String propertyValue, List<String> selectedValues) {
        if (selectedValues == null || selectedValues.isEmpty()) return true;
        if (propertyValue == null) return false;

        // Property value must match one of the selected values (OR logic)
        return selectedValues.stream()
                .anyMatch(value -> value.equalsIgnoreCase(propertyValue));
    }

    private boolean matchesMultiSelectFilter(String propertyMultiSelectValue, List<String> selectedValues) {
        if (selectedValues == null || selectedValues.isEmpty()) return true;
        if (propertyMultiSelectValue == null) return false;

        // Property's multi-select value should contain at least one of the selected values
        // The multiSelectValue is stored as a JSON array string, e.g., ["option1", "option2"]
        try {
            List<String> propertyValues = objectMapper.readValue(propertyMultiSelectValue,
                new TypeReference<List<String>>() {});

            // Check if property has ANY of the selected values (OR logic)
            return selectedValues.stream()
                    .anyMatch(selectedValue -> propertyValues.stream()
                            .anyMatch(pv -> pv.equalsIgnoreCase(selectedValue)));
        } catch (JsonProcessingException e) {
            // Fallback to simple string contains if JSON parsing fails
            return selectedValues.stream()
                    .anyMatch(value -> propertyMultiSelectValue.contains(value));
        }
    }

    private boolean matchesBooleanFilter(Boolean propertyValue, Boolean filterValue) {
        if (filterValue == null) return true;
        if (propertyValue == null) return false;

        return propertyValue.equals(filterValue);
    }

    private void validateSavedSearchRequest(SavedSearchRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Search name is required");
        }

        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter is required");
        }

        // Validate each filter
        for (SearchFilterDTO filter : request.getFilters()) {
            validateFilter(filter);
        }
    }

    private void validateSearchRequest(PropertySearchCriteriaRequest request) {
        if (request.getFilters() == null || request.getFilters().isEmpty()) {
            throw new IllegalArgumentException("At least one filter is required");
        }

        for (SearchFilterDTO filter : request.getFilters()) {
            validateFilter(filter);
        }
    }

    private void validateFilter(SearchFilterDTO filter) {
        if (filter.getAttributeId() == null) {
            throw new IllegalArgumentException("Attribute ID is required for each filter");
        }

        if (filter.getDataType() == null) {
            throw new IllegalArgumentException("Data type is required for each filter");
        }

        // Verify that the attribute exists and is searchable
        PropertyAttribute attribute = propertyAttributeRepository.findById(filter.getAttributeId())
                .orElseThrow(() -> new EntityNotFoundException("Property attribute not found with id: " + filter.getAttributeId()));

        if (!attribute.getIsSearchable()) {
            throw new IllegalArgumentException("Attribute '" + attribute.getName() + "' is not searchable");
        }

        // Validate data type matches
        if (!attribute.getDataType().equals(filter.getDataType())) {
            throw new IllegalArgumentException("Filter data type does not match attribute data type");
        }

        // Validate filter has appropriate values based on data type
        switch (filter.getDataType()) {
            case NUMBER:
                if (filter.getMinValue() == null && filter.getMaxValue() == null) {
                    throw new IllegalArgumentException("NUMBER filter must have at least minValue or maxValue");
                }
                if (filter.getMinValue() != null && filter.getMaxValue() != null
                    && filter.getMinValue().compareTo(filter.getMaxValue()) > 0) {
                    throw new IllegalArgumentException("NUMBER filter minValue must be <= maxValue");
                }
                break;
            case DATE:
                if (filter.getMinDate() == null && filter.getMaxDate() == null) {
                    throw new IllegalArgumentException("DATE filter must have at least minDate or maxDate");
                }
                if (filter.getMinDate() != null && filter.getMaxDate() != null
                    && filter.getMinDate().isAfter(filter.getMaxDate())) {
                    throw new IllegalArgumentException("DATE filter minDate must be <= maxDate");
                }
                break;
            case SINGLE_SELECT:
            case MULTI_SELECT:
                if (filter.getSelectedValues() == null || filter.getSelectedValues().isEmpty()) {
                    throw new IllegalArgumentException(filter.getDataType() + " filter must have at least one selected value");
                }
                break;
            case TEXT:
                if (filter.getTextValue() == null || filter.getTextValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("TEXT filter must have a text value");
                }
                break;
            case BOOLEAN:
                if (filter.getBooleanValue() == null) {
                    throw new IllegalArgumentException("BOOLEAN filter must have a boolean value");
                }
                break;
        }
    }

    private Pageable createPageable(PropertySearchCriteriaRequest request) {
        String[] sortParts = request.getSort().split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortField));
    }

    private SavedSearchResponse convertToResponse(SavedSearch savedSearch) {
        List<SearchFilterDTO> filters;
        try {
            filters = objectMapper.readValue(savedSearch.getFiltersJson(),
                new TypeReference<List<SearchFilterDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize filters from JSON", e);
        }

        return new SavedSearchResponse(
                savedSearch.getId(),
                savedSearch.getCustomer().getId(),
                savedSearch.getCustomer().getFullName(),
                savedSearch.getCustomer().getAgent().getId(),
                savedSearch.getCustomer().getAgent().getFullName(),
                savedSearch.getName(),
                savedSearch.getDescription(),
                filters,
                savedSearch.getCreatedDate(),
                savedSearch.getUpdatedDate()
        );
    }

    @Transactional(readOnly = true)
    public long countSavedSearchesByCustomer(Long customerId) {
        return savedSearchRepository.countByCustomerId(customerId);
    }
}
