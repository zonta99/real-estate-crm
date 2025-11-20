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
    private final PropertyRepository propertyRepository;
    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerInteractionRepository customerInteractionRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           PropertyRepository propertyRepository,
                           CustomerNoteRepository customerNoteRepository,
                           CustomerInteractionRepository customerInteractionRepository) {
        this.customerRepository = customerRepository;
        this.propertyRepository = propertyRepository;
        this.customerNoteRepository = customerNoteRepository;
        this.customerInteractionRepository = customerInteractionRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        // LAZY FIX: Use findAllWithAgent to eagerly fetch agent relationship
        return customerRepository.findAllWithAgent();
    }

    @Transactional(readOnly = true)
    public Page<Customer> getAllCustomers(Pageable pageable) {
        // LAZY FIX: Use findAllWithAgent to eagerly fetch agent relationship
        return customerRepository.findAllWithAgent(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> getCustomerById(Long id) {
        // LAZY FIX: Use findByIdWithAgent to eagerly fetch agent relationship
        return customerRepository.findByIdWithAgent(id);
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

    public Customer updateCustomerStatus(Long id, CustomerStatus status) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setStatus(status);
        return customerRepository.save(customer);
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
        return customerRepository.findAllWithAgent();
    }

    @Transactional(readOnly = true)
    public List<Customer> getCustomersByBudgetRange(BigDecimal minBudget, BigDecimal maxBudget) {
        return customerRepository.findAll().stream()
                .filter(customer -> isWithinBudgetRange(customer, minBudget, maxBudget))
                .collect(Collectors.toList());
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

    // Customer Notes Operations
    public CustomerNote createCustomerNote(Long customerId, User createdBy, String content) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        CustomerNote note = new CustomerNote(customer, createdBy, content);
        return customerNoteRepository.save(note);
    }

    @Transactional(readOnly = true)
    public List<CustomerNote> getCustomerNotes(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found with id: " + customerId);
        }
        return customerNoteRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerNote> getCustomerNoteById(Long noteId) {
        return customerNoteRepository.findById(noteId);
    }

    public void deleteCustomerNote(Long noteId) {
        if (!customerNoteRepository.existsById(noteId)) {
            throw new EntityNotFoundException("Customer note not found with id: " + noteId);
        }
        customerNoteRepository.deleteById(noteId);
    }

    // Customer Interactions Operations
    public CustomerInteraction createCustomerInteraction(Long customerId, User user, CustomerInteraction interaction) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        interaction.setCustomer(customer);
        interaction.setUser(user);

        // Validate and set related property if provided
        if (interaction.getRelatedProperty() != null && interaction.getRelatedProperty().getId() != null) {
            Property property = propertyRepository.findById(interaction.getRelatedProperty().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + interaction.getRelatedProperty().getId()));
            interaction.setRelatedProperty(property);
        }

        return customerInteractionRepository.save(interaction);
    }

    @Transactional(readOnly = true)
    public List<CustomerInteraction> getCustomerInteractions(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found with id: " + customerId);
        }
        return customerInteractionRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerInteraction> getCustomerInteractionById(Long interactionId) {
        return customerInteractionRepository.findById(interactionId);
    }

    public CustomerInteraction updateCustomerInteraction(Long interactionId, CustomerInteraction updatedInteraction) {
        CustomerInteraction existingInteraction = customerInteractionRepository.findById(interactionId)
                .orElseThrow(() -> new EntityNotFoundException("Customer interaction not found with id: " + interactionId));

        existingInteraction.setType(updatedInteraction.getType());
        existingInteraction.setSubject(updatedInteraction.getSubject());
        existingInteraction.setNotes(updatedInteraction.getNotes());
        existingInteraction.setInteractionDate(updatedInteraction.getInteractionDate());
        existingInteraction.setDurationMinutes(updatedInteraction.getDurationMinutes());

        if (updatedInteraction.getRelatedProperty() != null && updatedInteraction.getRelatedProperty().getId() != null) {
            Property property = propertyRepository.findById(updatedInteraction.getRelatedProperty().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + updatedInteraction.getRelatedProperty().getId()));
            existingInteraction.setRelatedProperty(property);
        }

        return customerInteractionRepository.save(existingInteraction);
    }

    public void deleteCustomerInteraction(Long interactionId) {
        if (!customerInteractionRepository.existsById(interactionId)) {
            throw new EntityNotFoundException("Customer interaction not found with id: " + interactionId);
        }
        customerInteractionRepository.deleteById(interactionId);
    }
}