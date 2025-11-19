package com.realestatecrm.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realestatecrm.dto.savedsearch.SavedSearchResponse;
import com.realestatecrm.dto.savedsearch.SearchFilterDTO;
import com.realestatecrm.entity.SavedSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * MapStruct mapper for SavedSearch entity to DTO conversions.
 * Configured with componentModel = "spring" to be injectable as a Spring bean.
 */
@Mapper(componentModel = "spring")
public abstract class SavedSearchMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    private static final TypeReference<List<SearchFilterDTO>> SEARCH_FILTER_LIST_TYPE =
            new TypeReference<List<SearchFilterDTO>>() {};

    /**
     * Maps SavedSearch entity to SavedSearchResponse DTO.
     *
     * @param savedSearch The entity to map
     * @return SavedSearchResponse DTO
     */
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(savedSearch.getCustomer().getFullName())")
    @Mapping(target = "agentId", source = "customer.agent.id")
    @Mapping(target = "agentName", expression = "java(savedSearch.getCustomer().getAgent().getFullName())")
    @Mapping(target = "filters", source = "filtersJson", qualifiedByName = "jsonToFilters")
    public abstract SavedSearchResponse toResponse(SavedSearch savedSearch);

    /**
     * Deserializes JSON string to list of SearchFilterDTO.
     *
     * @param filtersJson JSON string representation of filters
     * @return List of SearchFilterDTO
     */
    @Named("jsonToFilters")
    protected List<SearchFilterDTO> jsonToFilters(String filtersJson) {
        if (filtersJson == null || filtersJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(filtersJson, SEARCH_FILTER_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize filters from JSON: " + e.getMessage(), e);
        }
    }
}
