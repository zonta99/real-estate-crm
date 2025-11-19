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
import com.realestatecrm.enums.PropertyStatus;
import com.realestatecrm.mapper.SavedSearchMapper;
import com.realestatecrm.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SavedSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SavedSearchService.class);

    /**
     * JSON Schema Version for saved search filters.
     * Current version: 1.0 - Initial schema with SearchFilterDTO structure.
     * <p> <p>
     * Version History:
     * - 1.0 (current): Initial implementation with all 6 data types support
     * <p> <p>
     * Future versions should maintain backward compatibility by:
     * 1. Adding new optional fields (not required)
     * 2. Providing default values for missing fields
     * 3. Using @JsonIgnoreProperties(ignoreUnknown = true) on DTOs
     */
    private static final String JSON_SCHEMA_VERSION = "1.0";

    // TypeReference constants for JSON deserialization optimization
    private static final TypeReference<List<SearchFilterDTO>> SEARCH_FILTER_LIST_TYPE =
            new TypeReference<List<SearchFilterDTO>>() {};
    private static final TypeReference<List<String>> STRING_LIST_TYPE =
            new TypeReference<List<String>>() {};

    private final SavedSearchRepository savedSearchRepository;
    private final CustomerRepository customerRepository;
    private final PropertyRepository propertyRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    private final SavedSearchMapper savedSearchMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    public SavedSearchService(SavedSearchRepository savedSearchRepository,
                              CustomerRepository customerRepository,
                              PropertyRepository propertyRepository,
                              AttributeValueRepository attributeValueRepository,
                              PropertyAttributeRepository propertyAttributeRepository,
                              SavedSearchMapper savedSearchMapper) {
        this.savedSearchRepository = savedSearchRepository;
        this.customerRepository = customerRepository;
        this.propertyRepository = propertyRepository;
        this.attributeValueRepository = attributeValueRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        this.savedSearchMapper = savedSearchMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getAllSavedSearches() {
        return savedSearchRepository.findAllWithCustomer().stream()
                .map(savedSearchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getSavedSearchesByCustomer(Long customerId) {
        return savedSearchRepository.findByCustomerIdWithCustomer(customerId).stream()
                .map(savedSearchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavedSearchResponse> getSavedSearchesByAgent(Long agentId) {
        return savedSearchRepository.findByCustomerAgentId(agentId).stream()
                .map(savedSearchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SavedSearchResponse> getSavedSearchById(Long id) {
        return savedSearchRepository.findByIdWithCustomer(id)
                .map(savedSearchMapper::toResponse);
    }

    public SavedSearchResponse createSavedSearch(Long customerId, SavedSearchRequest request) {
        logger.debug("Creating saved search '{}' for customer id={}", request.getName(), customerId);
        validateSavedSearchRequest(request);

        Customer customer = customerRepository.findByIdWithAgent(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(request.getFilters());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize filters for saved search '{}': {}", request.getName(), e.getMessage());
            throw new IllegalArgumentException("Failed to serialize filters to JSON", e);
        }

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setCustomer(customer);
        savedSearch.setName(request.getName());
        savedSearch.setDescription(request.getDescription());
        savedSearch.setFiltersJson(filtersJson);

        SavedSearch saved = savedSearchRepository.save(savedSearch);
        logger.info("Created saved search '{}' (id={}) for customer '{}' (id={}) with {} filters",
                saved.getName(), saved.getId(), customer.getFullName(), customerId, request.getFilters().size());

        return savedSearchMapper.toResponse(savedSearchRepository.findByIdWithCustomer(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found after creation")));
    }

    public SavedSearchResponse updateSavedSearch(Long id, Long agentId, SavedSearchRequest request) {
        logger.debug("Updating saved search id={} by agent id={}", id, agentId);
        validateSavedSearchRequest(request);

        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + id));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            logger.warn("Unauthorized update attempt: Agent {} tried to update saved search {} owned by agent {}",
                    agentId, id, savedSearch.getCustomer().getAgent().getId());
            throw new SecurityException("Access denied: You can only update saved searches for your own customers");
        }

        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(request.getFilters());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize filters for saved search update id={}: {}", id, e.getMessage());
            throw new IllegalArgumentException("Failed to serialize filters to JSON", e);
        }

        String oldName = savedSearch.getName();
        savedSearch.setName(request.getName());
        savedSearch.setDescription(request.getDescription());
        savedSearch.setFiltersJson(filtersJson);

        SavedSearch updated = savedSearchRepository.save(savedSearch);
        logger.info("Updated saved search '{}' -> '{}' (id={}) for customer '{}' (id={})",
                oldName, updated.getName(), id,
                updated.getCustomer().getFullName(), updated.getCustomer().getId());

        return savedSearchMapper.toResponse(updated);
    }

    public void deleteSavedSearch(Long id, Long agentId) {
        logger.debug("Deleting saved search id={} by agent id={}", id, agentId);

        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(id)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + id));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            logger.warn("Unauthorized delete attempt: Agent {} tried to delete saved search {} owned by agent {}",
                    agentId, id, savedSearch.getCustomer().getAgent().getId());
            throw new SecurityException("Access denied: You can only delete saved searches for your own customers");
        }

        String searchName = savedSearch.getName();
        String customerName = savedSearch.getCustomer().getFullName();
        savedSearchRepository.delete(savedSearch);

        logger.info("Deleted saved search '{}' (id={}) for customer '{}' by agent id={}",
                searchName, id, customerName, agentId);
    }

    /**
     * Execute a property search based on dynamic criteria.
     * <p>
     * PERFORMANCE WARNING:
     * This method currently uses IN-MEMORY FILTERING which has significant limitations:
     * - Loads ALL ACTIVE properties into memory
     * - Applies filters in Java rather than at the database level
     * - Performance degrades with large datasets (10,000+ properties)
     * - Not suitable for production at scale
     * <p>
     * OPTIMIZATION NEEDED (High Priority):
     * For production deployment with large property datasets, this method should be
     * rewritten to use JPA Criteria API or QueryDSL to build dynamic database queries.
     * This would allow:
     * - Database-level filtering and pagination
     * - Proper indexing utilization
     * - Better query performance (from O(n) to O(log n) with indexes)
     * - Reduced memory consumption
     * <p>
     * CURRENT OPTIMIZATIONS:
     * - Batch fetching of AttributeValues to avoid N+1 queries
     * - Performance logging with warnings for slow queries (>1s)
     * <p>
     * RECOMMENDED APPROACH FOR REFACTORING:
     * 1. Use JPA Criteria API with CriteriaBuilder
     * 2. Build JOIN queries to AttributeValue based on filters
     * 3. Apply filtering at SQL level using WHERE clauses
     * 4. Use database pagination with LIMIT/OFFSET
     *
     * @param searchRequest Search criteria with filters, pagination, and sorting
     * @return Page of matching properties
     */
    @Transactional(readOnly = true)
    public Page<Property> executeSearch(PropertySearchCriteriaRequest searchRequest) {
        long startTime = System.currentTimeMillis();
        logger.debug("Starting property search with {} filters", searchRequest.getFilters().size());

        validateSearchRequest(searchRequest);

        // Get all ACTIVE properties
        List<Property> allProperties = propertyRepository.findByStatus(PropertyStatus.ACTIVE);
        logger.debug("Retrieved {} ACTIVE properties from database", allProperties.size());

        // Batch fetch all AttributeValues to avoid N+1 query problem
        Set<Long> propertyIds = allProperties.stream()
                .map(Property::getId)
                .collect(Collectors.toSet());

        Set<Long> attributeIds = searchRequest.getFilters().stream()
                .map(SearchFilterDTO::getAttributeId)
                .collect(Collectors.toSet());

        Map<Long, Map<Long, AttributeValue>> attributeValueCache =
                batchFetchAttributeValues(propertyIds, attributeIds);

        long batchFetchTime = System.currentTimeMillis();
        logger.debug("Batch fetched attribute values in {} ms", (batchFetchTime - startTime));

        // Filter properties based on search criteria
        List<Property> matchedProperties = allProperties.stream()
                .filter(property -> matchesAllFilters(property, searchRequest.getFilters(), attributeValueCache))
                .collect(Collectors.toList());

        long filterTime = System.currentTimeMillis();
        logger.debug("Filtered to {} matching properties in {} ms",
                matchedProperties.size(), (filterTime - batchFetchTime));

        // Apply sorting and pagination
        Pageable pageable = createPageable(searchRequest);

        // Manual pagination since we're filtering in memory
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), matchedProperties.size());

        List<Property> pageContent = start < matchedProperties.size()
            ? matchedProperties.subList(start, end)
            : List.of();

        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("Property search completed: {} matches out of {} properties, returning page {} with {} results (total time: {} ms)",
                matchedProperties.size(), allProperties.size(), searchRequest.getPage(),
                pageContent.size(), totalTime);

        if (totalTime > 1000) {
            logger.warn("Property search took longer than 1 second ({} ms). Consider optimization for {} total properties.",
                    totalTime, allProperties.size());
        }

        return new PageImpl<>(pageContent, pageable, matchedProperties.size());
    }

    @Transactional(readOnly = true)
    public Page<Property> executeSavedSearch(Long searchId, Long agentId, Integer page, Integer size, String sort) {
        logger.debug("Executing saved search id={} for agent id={}", searchId, agentId);

        SavedSearch savedSearch = savedSearchRepository.findByIdWithCustomer(searchId)
                .orElseThrow(() -> new EntityNotFoundException("Saved search not found with id: " + searchId));

        // Authorization check: ensure the agent owns this customer's saved search
        if (!savedSearch.getCustomer().getAgent().getId().equals(agentId)) {
            logger.warn("Unauthorized access attempt: Agent {} tried to execute saved search {} owned by agent {}",
                    agentId, searchId, savedSearch.getCustomer().getAgent().getId());
            throw new SecurityException("Access denied: You can only execute saved searches for your own customers");
        }

        // Deserialize filters from JSON
        List<SearchFilterDTO> filters;
        try {
            filters = objectMapper.readValue(savedSearch.getFiltersJson(), SEARCH_FILTER_LIST_TYPE);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize filters for saved search id={}: {}", searchId, e.getMessage());
            throw new IllegalArgumentException("Failed to deserialize filters from JSON", e);
        }

        logger.info("Executing saved search '{}' (id={}) for customer '{}' (id={}) with {} filters",
                savedSearch.getName(), searchId,
                savedSearch.getCustomer().getFullName(), savedSearch.getCustomer().getId(),
                filters.size());

        // Create search request
        PropertySearchCriteriaRequest searchRequest = new PropertySearchCriteriaRequest();
        searchRequest.setFilters(filters);
        searchRequest.setPage(page != null ? page : 0);
        searchRequest.setSize(size != null ? size : 20);
        searchRequest.setSort(sort != null ? sort : "createdDate,desc");

        return executeSearch(searchRequest);
    }

    /**
     * Batch fetch all AttributeValues for given properties and attributes to avoid N+1 queries.
     *
     * @param propertyIds Set of property IDs
     * @param attributeIds Set of attribute IDs
     * @return Map of propertyId -> (attributeId -> AttributeValue)
     */
    private Map<Long, Map<Long, AttributeValue>> batchFetchAttributeValues(
            Set<Long> propertyIds, Set<Long> attributeIds) {

        if (propertyIds.isEmpty() || attributeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Fetch all relevant AttributeValues in one query
        List<AttributeValue> attributeValues = attributeValueRepository
                .findByPropertyIdInAndAttributeIdIn(new ArrayList<>(propertyIds), new ArrayList<>(attributeIds));

        // Build nested map: propertyId -> attributeId -> AttributeValue
        Map<Long, Map<Long, AttributeValue>> cache = new HashMap<>();
        for (AttributeValue av : attributeValues) {
            cache.computeIfAbsent(av.getProperty().getId(), k -> new HashMap<>())
                    .put(av.getAttribute().getId(), av);
        }

        return cache;
    }

    private boolean matchesAllFilters(Property property, List<SearchFilterDTO> filters,
                                      Map<Long, Map<Long, AttributeValue>> attributeValueCache) {
        for (SearchFilterDTO filter : filters) {
            if (!matchesFilter(property, filter, attributeValueCache)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesFilter(Property property, SearchFilterDTO filter,
                                  Map<Long, Map<Long, AttributeValue>> attributeValueCache) {
        // Get AttributeValue from cache instead of querying database
        Map<Long, AttributeValue> propertyAttributes = attributeValueCache.get(property.getId());
        if (propertyAttributes == null) {
            return false; // Property has no attribute values
        }

        AttributeValue value = propertyAttributes.get(filter.getAttributeId());
        if (value == null) {
            return false; // Property doesn't have this specific attribute value
        }

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
            List<String> propertyValues = objectMapper.readValue(propertyMultiSelectValue, STRING_LIST_TYPE);

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

    @Transactional(readOnly = true)
    public long countSavedSearchesByCustomer(Long customerId) {
        return savedSearchRepository.countByCustomerId(customerId);
    }
}
