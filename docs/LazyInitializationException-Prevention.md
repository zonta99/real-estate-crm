# LazyInitializationException Prevention Guide

## What is LazyInitializationException?

`LazyInitializationException` occurs when you try to access a lazy-loaded entity relationship **outside** of an active Hibernate session/transaction.

### How It Happens

1. An entity with lazy relationships is loaded within a `@Transactional` method
2. The transaction commits and the Hibernate session closes
3. Code tries to access a lazy relationship (e.g., `property.getAgent().getName()`)
4. Hibernate tries to fetch the relationship but has no active session
5. **BOOM!** `LazyInitializationException`

---

## Why We Changed Property.agent to LAZY

**Before (EAGER):**
```java
@ManyToOne(fetch = FetchType.EAGER)
private User agent;
```
- **Problem**: Always loads agent, even when not needed
- **Impact**: N+1 query problem - causes 1000s of extra database queries

**After (LAZY):**
```java
@ManyToOne(fetch = FetchType.LAZY)
private User agent;
```
- **Benefit**: Only loads agent when explicitly requested
- **Trade-off**: Must handle lazy loading properly to avoid exceptions

---

## Where Can This Error Occur?

### ❌ **Anti-Pattern (Causes Exception)**

```java
// BAD: Entity leaves @Transactional boundary, then accessed
@Service
public class PropertyService {
    @Transactional
    public Property getProperty(Long id) {
        return propertyRepository.findById(id).get();
    } // Transaction ends here!
}

@RestController
public class PropertyController {
    public PropertyResponse get(Long id) {
        Property property = propertyService.getProperty(id);
        // ERROR: Accessing lazy relationship outside transaction!
        return new PropertyResponse(
            property.getId(),
            property.getAgent().getName() // LazyInitializationException!
        );
    }
}
```

### ✅ **Solution 1: Fetch Within Transaction**

```java
@Repository
public interface PropertyRepository {
    @EntityGraph(attributePaths = {"agent"})
    Optional<Property> findByIdWithAgent(Long id);
}

@Service
public class PropertyService {
    @Transactional(readOnly = true)
    public Property getProperty(Long id) {
        // Fetches agent eagerly within transaction
        return propertyRepository.findByIdWithAgent(id).get();
    }
}
```

### ✅ **Solution 2: Convert to DTO Within Transaction**

```java
@Service
public class PropertyService {
    @Transactional(readOnly = true)
    public PropertyDTO getProperty(Long id) {
        Property property = propertyRepository.findById(id).get();
        // Convert to DTO while still in transaction
        return new PropertyDTO(
            property.getId(),
            property.getAgent().getName() // OK: Still in transaction
        );
    }
}

@RestController
public class PropertyController {
    public PropertyDTO get(Long id) {
        return propertyService.getProperty(id); // Already a DTO
    }
}
```

### ✅ **Solution 3: Controller-Level Transaction**

```java
@RestController
public class PropertyController {
    @GetMapping("/{id}")
    @Transactional(readOnly = true) // Keep transaction open
    public PropertyResponse get(Long id) {
        Property property = propertyService.getProperty(id);
        return new PropertyResponse(
            property.getId(),
            property.getAgent().getName() // OK: Transaction still active
        );
    }
}
```

---

## Common Locations Where This Occurs

### 1. **Controllers Accessing Lazy Relationships**

**Symptom**: Error when building DTOs/responses from entities

**Fix**: Either:
- Use `@EntityGraph` to fetch relationships
- Convert entities to DTOs in service layer
- Add `@Transactional(readOnly = true)` to controller methods

### 2. **Jackson JSON Serialization**

**Symptom**: Error when returning entities directly from controllers

```java
// BAD: Returning entity directly
@GetMapping("/{id}")
public Property get(Long id) {
    return propertyService.getProperty(id); // JSON serialization fails!
}
```

**Fix**:
```java
// GOOD: Return DTO instead
@GetMapping("/{id}")
public PropertyDTO get(Long id) {
    return propertyService.getPropertyDTO(id);
}
```

### 3. **Stream Operations Outside Transactions**

```java
// BAD
@Service
public class PropertyService {
    @Transactional
    public List<Property> getAll() {
        return propertyRepository.findAll();
    }
}

// Controller
List<Property> properties = service.getAll();
properties.stream()
    .map(p -> p.getAgent().getName()) // ERROR!
    .collect(Collectors.toList());
```

**Fix**:
```java
// GOOD: Do mapping inside service
@Service
public class PropertyService {
    @Transactional(readOnly = true)
    public List<String> getAgentNames() {
        return propertyRepository.findAll().stream()
            .map(p -> p.getAgent().getName()) // OK: In transaction
            .collect(Collectors.toList());
    }
}
```

### 4. **Async Methods / Background Tasks**

```java
// BAD
@Async
public void processProperty(Property property) {
    // Async method runs in different thread
    String agentName = property.getAgent().getName(); // ERROR!
}
```

**Fix**:
```java
// GOOD: Pass DTO or fetch within async method
@Async
public void processProperty(Long propertyId) {
    Property property = propertyRepository.findByIdWithAgent(propertyId).get();
    String agentName = property.getAgent().getName(); // OK
}
```

### 5. **Security / Audit / Logging**

```java
// BAD: In aspect or logging
@AfterReturning(pointcut = "...", returning = "property")
public void audit(Property property) {
    logger.info("Agent: {}", property.getAgent().getName()); // ERROR!
}
```

**Fix**:
```java
// GOOD: Pass IDs or DTOs
@AfterReturning(pointcut = "...", returning = "property")
public void audit(Property property) {
    logger.info("Property: {}, Agent ID: {}",
        property.getId(), property.getAgent().getId()); // OK: ID is always loaded
}
```

### 6. **Template Engines (Thymeleaf, JSP)**

```html
<!-- BAD: In Thymeleaf template -->
<div th:text="${property.agent.name}"></div> <!-- ERROR! -->
```

**Fix**: Ensure controller passes fully-loaded entities or DTOs with all needed data.

---

## Best Practices

### 1. **Use @EntityGraph for Specific Queries**

```java
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // When you need agent
    @EntityGraph(attributePaths = {"agent"})
    Optional<Property> findByIdWithAgent(Long id);

    // When you need agent and sharing info
    @EntityGraph(attributePaths = {"agent", "sharedWith"})
    List<Property> findAllWithDetails();
}
```

### 2. **Always Convert Entities to DTOs in Service Layer**

```java
@Service
@Transactional
public class PropertyService {

    @Transactional(readOnly = true)
    public PropertyDTO getPropertyDTO(Long id) {
        Property property = propertyRepository.findByIdWithAgent(id).get();
        return convertToDTO(property); // Convert inside transaction
    }

    private PropertyDTO convertToDTO(Property property) {
        return new PropertyDTO(
            property.getId(),
            property.getTitle(),
            property.getAgent().getId(),
            property.getAgent().getName()
        );
    }
}
```

### 3. **Mark All Service Methods as @Transactional**

```java
@Service
@Transactional // Class-level for write operations
public class PropertyService {

    @Transactional(readOnly = true) // Method-level for read operations
    public Property getProperty(Long id) {
        return propertyRepository.findById(id).get();
    }
}
```

### 4. **Never Return Entities from Controllers**

```java
// ❌ BAD
@GetMapping("/{id}")
public Property get(Long id) {
    return propertyService.get(id);
}

// ✅ GOOD
@GetMapping("/{id}")
public PropertyResponse get(Long id) {
    Property property = propertyService.get(id);
    return convertToResponse(property);
}

// ✅ BETTER
@GetMapping("/{id}")
public PropertyResponse get(Long id) {
    return propertyService.getPropertyResponse(id); // Service returns DTO
}
```

---

## Checklist: Have You Fixed All Occurrences?

- [ ] All repository methods that fetch relationships use `@EntityGraph`
- [ ] All service methods are marked `@Transactional` or `@Transactional(readOnly = true)`
- [ ] All entity-to-DTO conversions happen inside `@Transactional` methods
- [ ] Controllers never return entity objects directly
- [ ] No lazy relationship access in async methods or background tasks
- [ ] Template views receive fully-loaded DTOs, not entities
- [ ] Logging/auditing code doesn't access lazy relationships
- [ ] Stream operations that access relationships happen inside transactions

---

## How We Fixed It in This Project

### 1. **PropertyRepository Changes**

Added `@EntityGraph(attributePaths = {"agent"})` to **all** query methods:

```java
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @EntityGraph(attributePaths = {"agent"})
    Optional<Property> findByIdWithAgent(Long id);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findAllWithAgent();

    @EntityGraph(attributePaths = {"agent"})
    Page<Property> findAllWithAgent(Pageable pageable);

    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByAgentId(Long agentId);

    // ... and ALL other methods
}
```

### 2. **PropertyService Changes**

Updated to use the new methods:

```java
@Service
@Transactional
public class PropertyService {

    @Transactional(readOnly = true)
    public Optional<Property> getPropertyById(Long id) {
        return propertyRepository.findByIdWithAgent(id); // Was: findById
    }

    @Transactional(readOnly = true)
    public List<Property> getAllProperties() {
        return propertyRepository.findAllWithAgent(); // Was: findAll
    }
}
```

### 3. **Controller Already Has @Transactional**

```java
@GetMapping("/{id}")
@PreAuthorize("hasRole('AGENT') or hasRole('BROKER') or hasRole('ADMIN')")
@Transactional(readOnly = true) // Keeps session open
public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
    Property property = propertyService.getPropertyById(id).orElseThrow();
    return ResponseEntity.ok(convertToPropertyResponse(property)); // OK: Still in transaction
}
```

---

## Testing for LazyInitializationException

### Manual Test:

1. Remove `@Transactional` from a controller method
2. Access a lazy relationship
3. Should throw exception

### Unit Test:

```java
@Test
void shouldNotThrowLazyInitException() {
    // Get property
    Property property = propertyService.getPropertyById(1L).get();

    // Try to access lazy relationship outside transaction
    assertDoesNotThrow(() -> {
        String agentName = property.getAgent().getName();
        assertNotNull(agentName);
    });
}
```

---

## Summary

**The Problem**: Lazy loading improves performance but requires active sessions

**The Solution**:
1. Use `@EntityGraph` to fetch relationships when needed
2. Keep conversions inside `@Transactional` methods
3. Return DTOs, never entities
4. Be aware of transaction boundaries

**This Issue Can Appear Anywhere**:
- Controllers
- Services
- Async methods
- Templates
- JSON serialization
- Logging/auditing

**Prevention is Key**: Follow the best practices above to avoid this issue entirely.
