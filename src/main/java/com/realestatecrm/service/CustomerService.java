package com.realestatecrm.service;

import com.realestatecrm.entity.*;
import com.realestatecrm.enums.CustomerStatus;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerSearchCriteriaRepository searchCriteriaRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyValueRepository propertyValueRepository;
    private final PropertyAttributeRepository propertyAttributeRepository;
    /*private final UserRepository userRepository;*/

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           CustomerSearchCriteriaRepository searchCriteriaRepository,
                           PropertyRepository propertyRepository,
                           PropertyValueRepository propertyValueRepository,
                           PropertyAttributeRepository propertyAttributeRepository,
                           UserRepository userRepository) {
        this.customerRepository = customerRepository;
        this.searchCriteriaRepository = searchCriteriaRepository;
        this.propertyRepository = propertyRepository;
        this.propertyValueRepository = propertyValueRepository;
        this.propertyAttributeRepository = propertyAttributeRepository;
        //this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByAgent(Long agentId) {
        return customerRepository.findByAgentId(agentId);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomersByAgent(Long agentId, Pageable pageable) {
        return customerRepository.findByAgentId(agentId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByAgents(List<Long> agentIds) {
        return customerRepository.findByAgentIdIn(agentIds);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomersByAgents(List<Long> agentIds, Pageable pageable) {
        return customerRepository.findByAgentIdIn(agentIds, pageable);
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByStatus(CustomerStatus status) {
        return customerRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomersByStatus(CustomerStatus status, Pageable pageable) {
        return customerRepository.findByAgentIdAndStatus(null, status, pageable);
    }

    public Customer createCustomer(Customer customer) {
        validateCustomer(customer);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));

        validateCustomerUpdate(existingCustomer, updatedCustomer);

        existingCustomer.setFirstName(updatedCustomer.getFirstName());
        existingCustomer.setLastName(updatedCustomer.getLastName());
        existingCustomer.setPhone(updatedCustomer.getPhone());
        existingCustomer.setEmail(updatedCustomer.getEmail());
        existingCustomer.setBudgetMin(updatedCustomer.getBudgetMin());
        existingCustomer.setBudgetMax(updatedCustomer.getBudgetMax());
        existingCustomer.setNotes(updatedCustomer.getNotes());
        existingCustomer.setLeadSource(updatedCustomer.getLeadSource());
        existingCustomer.setStatus(updatedCustomer.getStatus());

        return customerRepository.save(existingCustomer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    public CustomerSearchCriteria setSearchCriteria(Long customerId, Long attributeId,
                                                    String textValue, BigDecimal numberMinValue,
                                                    BigDecimal numberMaxValue, Boolean booleanValue,
                                                    String multiSelectValue) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        PropertyAttribute attribute = propertyAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new EntityNotFoundException("Attribute not found with id: " + attributeId));

        Optional<CustomerSearchCriteria> existingCriteria = searchCriteriaRepository
                .findByCustomerIdAndAttributeId(customerId, attributeId);

        CustomerSearchCriteria criteria = existingCriteria.orElse(new CustomerSearchCriteria(customer, attribute));

        // Clear all values first
        criteria.setTextValue(null);
        criteria.setNumberMinValue(null);
        criteria.setNumberMaxValue(null);
        criteria.setBooleanValue(null);
        criteria.setMultiSelectValue(null);

        // Set appropriate value based on attribute type
        switch (attribute.getDataType()) {
            case TEXT, SINGLE_SELECT -> criteria.setTextValue(textValue);
            case NUMBER -> {
                criteria.setNumberMinValue(numberMinValue);
                criteria.setNumberMaxValue(numberMaxValue);
            }
            case BOOLEAN -> criteria.setBooleanValue(booleanValue);
            case MULTI_SELECT -> criteria.setMultiSelectValue(multiSelectValue);
        }

        return searchCriteriaRepository.save(criteria);
    }

    @Transactional(readOnly = true)
    public List<CustomerSearchCriteria> getSearchCriteria(Long customerId) {
        return searchCriteriaRepository.findByCustomerId(customerId);
    }

    public void deleteSearchCriteria(Long customerId, Long attributeId) {
        searchCriteriaRepository.deleteByCustomerIdAndAttributeId(customerId, attributeId);
    }

    @Transactional(readOnly = true)
    public List<Property> findMatchingProperties(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        List<CustomerSearchCriteria> searchCriteria = searchCriteriaRepository
                .findSearchableCriteriaByCustomerId(customerId);

        List<Property> allProperties = propertyRepository.findAll();

        return allProperties.stream()
                .filter(property -> matchesAllCriteria(property, customer, searchCriteria))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String name, CustomerStatus status, String phone, String email) {
        if (name != null && !name.trim().isEmpty()) {
            return customerRepository.findByNameContaining(name.trim());
        }
        if (status != null) {
            return customerRepository.findByStatus(status);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            return customerRepository.findByPhone(phone.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            return customerRepository.findByEmail(email.trim()).map(List::of).orElse(List.of());
        }
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByBudgetRange(BigDecimal minBudget, BigDecimal maxBudget) {
        return customerRepository.findAll().stream()
                .filter(customer -> isWithinBudgetRange(customer, minBudget, maxBudget))
                .collect(Collectors.toList());
    }

    private boolean matchesAllCriteria(Property property, Customer customer, List<CustomerSearchCriteria> searchCriteria) {
        // Check budget constraints first
        if (!isWithinBudget(property.getPrice(), customer.getBudgetMin(), customer.getBudgetMax())) {
            return false;
        }

        // Check each search criteria
        for (CustomerSearchCriteria criteria : searchCriteria) {
            if (!matchesCriteria(property, criteria)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesCriteria(Property property, CustomerSearchCriteria criteria) {
        Optional<PropertyValue> propertyValue = propertyValueRepository
                .findByPropertyIdAndAttributeId(property.getId(), criteria.getAttribute().getId());

        if (propertyValue.isEmpty()) {
            return false; // Property doesn't have this attribute value
        }

        PropertyValue value = propertyValue.get();

        return switch (criteria.getAttribute().getDataType()) {
            case TEXT, SINGLE_SELECT -> matchesTextCriteria(value.getTextValue(), criteria.getTextValue());
            case NUMBER ->
                    matchesNumberCriteria(value.getNumberValue(), criteria.getNumberMinValue(), criteria.getNumberMaxValue());
            case BOOLEAN -> matchesBooleanCriteria(value.getBooleanValue(), criteria.getBooleanValue());
            case MULTI_SELECT ->
                    matchesMultiSelectCriteria(value.getMultiSelectValue(), criteria.getMultiSelectValue());
        };
    }

    private boolean matchesTextCriteria(String propertyValue, String criteriaValue) {
        if (criteriaValue == null) return true;
        return criteriaValue.equalsIgnoreCase(propertyValue);
    }

    private boolean matchesNumberCriteria(BigDecimal propertyValue, BigDecimal minValue, BigDecimal maxValue) {
        if (propertyValue == null) return false;

        if (minValue != null && propertyValue.compareTo(minValue) < 0) {
            return false;
        }

        if (maxValue != null && propertyValue.compareTo(maxValue) > 0) {
            return false;
        }

        return true;
    }

    private boolean matchesBooleanCriteria(Boolean propertyValue, Boolean criteriaValue) {
        if (criteriaValue == null) return true;
        return criteriaValue.equals(propertyValue);
    }

    private boolean matchesMultiSelectCriteria(String propertyValue, String criteriaValue) {
        if (criteriaValue == null || propertyValue == null) return true;

        // Simple contains check - in a real implementation, you'd parse JSON arrays
        return propertyValue.contains(criteriaValue);
    }

    private boolean isWithinBudget(BigDecimal price, BigDecimal minBudget, BigDecimal maxBudget) {
        if (minBudget != null && price.compareTo(minBudget) < 0) {
            return false;
        }

        if (maxBudget != null && price.compareTo(maxBudget) > 0) {
            return false;
        }

        return true;
    }

    private boolean isWithinBudgetRange(Customer customer, BigDecimal minBudget, BigDecimal maxBudget) {
        if (customer.getBudgetMax() != null && customer.getBudgetMax().compareTo(minBudget) < 0) {
            return false;
        }

        if (customer.getBudgetMin() != null && customer.getBudgetMin().compareTo(maxBudget) > 0) {
            return false;
        }

        return true;
    }

    private void validateCustomer(Customer customer) {
        if (customer.getEmail() != null &&
                customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + customer.getEmail());
        }

        if (customer.getBudgetMin() != null && customer.getBudgetMax() != null &&
                customer.getBudgetMin().compareTo(customer.getBudgetMax()) > 0) {
            throw new IllegalArgumentException("Minimum budget cannot be greater than maximum budget");
        }
    }

    private void validateCustomerUpdate(Customer existingCustomer, Customer updatedCustomer) {
        if (updatedCustomer.getEmail() != null &&
                !updatedCustomer.getEmail().equals(existingCustomer.getEmail()) &&
                customerRepository.findByEmail(updatedCustomer.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + updatedCustomer.getEmail());
        }

        if (updatedCustomer.getBudgetMin() != null && updatedCustomer.getBudgetMax() != null &&
                updatedCustomer.getBudgetMin().compareTo(updatedCustomer.getBudgetMax()) > 0) {
            throw new IllegalArgumentException("Minimum budget cannot be greater than maximum budget");
        }
    }
}