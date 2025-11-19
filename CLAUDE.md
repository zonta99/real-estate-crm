# CLAUDE.md - AI Assistant Guide

> **Last Updated**: 2025-11-19
> **Purpose**: Guide for AI assistants working with this Real Estate CRM codebase

This document provides comprehensive guidance for AI assistants (like Claude) to effectively understand, navigate, and modify this codebase. It covers architecture, conventions, patterns, and common pitfalls.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Technology Stack](#technology-stack)
4. [Codebase Structure](#codebase-structure)
5. [Domain Model](#domain-model)
6. [Development Workflow](#development-workflow)
7. [Key Conventions](#key-conventions)
8. [Common Patterns](#common-patterns)
9. [Security Guidelines](#security-guidelines)
10. [Testing Guidelines](#testing-guidelines)
11. [Common Pitfalls](#common-pitfalls)
12. [Quick Reference](#quick-reference)

---

## Project Overview

### What is This?

A **Real Estate CRM** backend built with Spring Boot for managing properties, customers, and user relationships in a real estate business.

### Core Features

- **User Management**: Multi-role system (ADMIN, BROKER, AGENT, ASSISTANT) with hierarchies
- **Property Management**: CRUD operations with dynamic attributes and sharing
- **Customer CRM**: Lead tracking, interactions, notes, and search criteria
- **Dynamic Attributes**: Extensible property metadata system
- **Saved Searches**: Complex filtering and saved search criteria
- **JWT Authentication**: Stateless, token-based security

### Key Characteristics

- **Java 21** with modern Spring Boot 3.2.5
- **RESTful API** with OpenAPI/Swagger documentation
- **Stateless Authentication** using JWT tokens
- **Profile-Based Configuration** (dev/prod)
- **MapStruct** for compile-time DTO mapping
- **H2 (dev)** / **PostgreSQL (prod)** databases

---

## Architecture & Design Patterns

### Architectural Style

**Layered (N-Tier) Architecture** with strict separation of concerns:

```
┌─────────────────────────────────────┐
│   Controller Layer (REST API)       │  ← HTTP endpoints, validation, auth
├─────────────────────────────────────┤
│   Service Layer (Business Logic)    │  ← Transactions, domain logic
├─────────────────────────────────────┤
│   Repository Layer (Data Access)    │  ← JPA queries, database ops
├─────────────────────────────────────┤
│   Entity Layer (Domain Model)       │  ← JPA entities, relationships
└─────────────────────────────────────┘
```

### Request Flow

```
HTTP Request
  ↓ (Spring Security Filter Chain)
JWT Validation → Authentication
  ↓
Controller
  ├─ @Valid annotation validates DTO
  ├─ @PreAuthorize checks permissions
  └─ Delegates to Service
       ↓
Service (@Transactional)
  ├─ Business logic
  ├─ Calls Repository
  └─ Converts Entity → DTO (IMPORTANT: within transaction!)
       ↓
Repository (Spring Data JPA)
  ├─ Custom queries with @Query
  ├─ @EntityGraph to fetch relationships
  └─ Returns Entity
       ↓
Response (DTO/JSON)
```

### Design Patterns Used

| Pattern | Usage | Location |
|---------|-------|----------|
| **Repository Pattern** | Data access abstraction | `/repository/` |
| **Service Layer Pattern** | Business logic encapsulation | `/service/` |
| **DTO Pattern** | Request/response objects separate from entities | `/dto/` |
| **Mapper Pattern** | Entity↔DTO transformation with MapStruct | `/mapper/` |
| **Factory Pattern** | `AttributeValue.create*()` static factories | `AttributeValue.java` |
| **Strategy Pattern** | Profile-based security configs (dev vs prod) | `SecurityConfig.java` |
| **Observer Pattern** | JPA auditing with `@EntityListeners` | All entities |

---

## Technology Stack

### Core Framework

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.2.5 | Application framework |
| **Spring Framework** | 6.x | DI, AOP, transaction management |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.2.x | Data persistence |
| **Hibernate** | 6.x | ORM implementation |

### Security & Authentication

| Technology | Version | Purpose |
|------------|---------|---------|
| **JJWT** | 0.12.6 | JWT token generation/validation |
| **BCrypt** | (Spring) | Password hashing |

### Data Mapping & Validation

| Technology | Version | Purpose |
|------------|---------|---------|
| **MapStruct** | 1.5.5 | Compile-time DTO mapping |
| **Jakarta Validation** | 3.x | Bean validation (@NotNull, @Valid, etc.) |

### Database

| Technology | Purpose |
|------------|---------|
| **H2** | In-memory database (development) |
| **PostgreSQL** | Production database |

### API Documentation

| Technology | Version | Purpose |
|------------|---------|---------|
| **SpringDoc OpenAPI** | 2.3.0 | Swagger UI / OpenAPI 3.0 docs |

### Testing

| Technology | Purpose |
|------------|---------|
| **JUnit 5** | Unit testing framework |
| **MockMvc** | Integration testing for controllers |
| **Spring Security Test** | Security testing utilities |

---

## Codebase Structure

### Directory Layout

```
/home/user/real-estate-crm/
├── pom.xml                          # Maven configuration
├── src/main/java/com/realestatecrm/
│   ├── config/                      # Configuration classes
│   │   ├── SecurityConfig.java      # Security & CORS config
│   │   ├── DatabaseConfig.java      # JPA auditing config
│   │   └── DataLoader.java          # Initial data seeding
│   │
│   ├── controller/                  # REST API endpoints (6 controllers)
│   │   ├── AuthController.java      # /api/auth/*
│   │   ├── PropertyController.java  # /api/properties/*
│   │   ├── CustomerController.java  # /api/customers/*
│   │   ├── PropertyAttributeController.java
│   │   ├── UserController.java      # /api/users/*
│   │   └── SavedSearchController.java
│   │
│   ├── service/                     # Business logic (8 services)
│   │   ├── PropertyService.java
│   │   ├── CustomerService.java
│   │   ├── UserService.java
│   │   ├── PropertyAttributeService.java
│   │   ├── RefreshTokenService.java
│   │   ├── PermissionService.java
│   │   └── CustomUserDetailsService.java
│   │
│   ├── repository/                  # Data access (13 repositories)
│   │   ├── PropertyRepository.java
│   │   ├── UserRepository.java
│   │   ├── CustomerRepository.java
│   │   └── ... (and 10 more)
│   │
│   ├── entity/                      # JPA entities (13 entities)
│   │   ├── Property.java
│   │   ├── User.java
│   │   ├── Customer.java
│   │   ├── PropertyAttribute.java
│   │   ├── AttributeValue.java
│   │   └── ... (and 8 more)
│   │
│   ├── dto/                         # Data Transfer Objects
│   │   ├── property/
│   │   │   ├── request/            # CreatePropertyRequest, etc.
│   │   │   └── response/           # PropertyResponse, etc.
│   │   ├── customer/
│   │   ├── user/
│   │   ├── auth/
│   │   ├── propertyattribute/
│   │   ├── savedsearch/
│   │   └── common/
│   │
│   ├── mapper/                      # MapStruct mappers (6 mappers)
│   │   ├── PropertyMapper.java
│   │   ├── CustomerMapper.java
│   │   ├── UserMapper.java
│   │   └── ... (and 3 more)
│   │
│   ├── security/                    # Security utilities
│   │   ├── JwtUtils.java           # JWT generation/validation
│   │   └── AuthTokenFilter.java    # JWT filter
│   │
│   ├── enums/                       # Enumeration types
│   │   ├── Role.java               # ADMIN, BROKER, AGENT, ASSISTANT
│   │   ├── PropertyStatus.java     # ACTIVE, INACTIVE, SOLD
│   │   ├── PropertyDataType.java   # TEXT, NUMBER, BOOLEAN, etc.
│   │   └── ... (and 5 more)
│   │
│   ├── exception/
│   │   └── GlobalExceptionHandler.java  # Centralized error handling
│   │
│   ├── validation/
│   │   ├── ValidPassword.java      # Custom password validator
│   │   └── PasswordValidator.java
│   │
│   └── RealEstateCrmApplication.java  # Main application class
│
├── src/main/resources/
│   ├── application.yml              # Dev configuration
│   └── application-prod.yml         # Production overrides
│
├── src/test/java/
│   └── ... (Integration tests)
│
├── docs/                            # Documentation
│   ├── API-Documentation.md
│   ├── property-crud.md
│   ├── property-attributes.md
│   └── LazyInitializationException-Prevention.md  # IMPORTANT!
│
├── README.md                        # Quick start guide
├── SECURITY.md                      # Security documentation
└── CLAUDE.md                        # This file
```

---

## Domain Model

### Core Entities

```
┌─────────────┐
│    User     │ (System users: agents, brokers, admins)
├─────────────┤
│ id          │
│ username    │
│ email       │
│ password    │ (BCrypt hashed)
│ fullName    │
│ role        │ (ADMIN, BROKER, AGENT, ASSISTANT)
│ status      │ (ACTIVE, INACTIVE)
└─────────────┘
      │ 1
      │ owns/manages
      │
      ↓ N
┌──────────────┐
│   Property   │ (Real estate listings)
├──────────────┤
│ id           │
│ title        │
│ description  │
│ price        │
│ status       │ (ACTIVE, INACTIVE, SOLD)
│ agent        │ → User
└──────────────┘
      │ 1
      │ has
      │
      ↓ N
┌─────────────────────┐
│  AttributeValue     │ (Dynamic property attributes)
├─────────────────────┤
│ id                  │
│ property            │ → Property
│ attribute           │ → PropertyAttribute
│ value               │ (stored as String, typed by attribute)
└─────────────────────┘
      │
      ↓
┌──────────────────────┐
│ PropertyAttribute    │ (Attribute definitions - admin managed)
├──────────────────────┤
│ id                   │
│ name                 │
│ dataType             │ (TEXT, NUMBER, BOOLEAN, DATE, SINGLE_SELECT, MULTI_SELECT)
│ category             │ (BASIC, STRUCTURE, FEATURES, LOCATION, FINANCIAL)
│ required             │
│ searchable           │
│ displayOrder         │
└──────────────────────┘
      │ 1
      │ has options (for SELECT types)
      │
      ↓ N
┌───────────────────────────┐
│ PropertyAttributeOption   │
├───────────────────────────┤
│ id                        │
│ attribute                 │ → PropertyAttribute
│ optionValue               │
│ displayOrder              │
└───────────────────────────┘

┌─────────────┐
│  Customer   │ (Prospects/clients)
├─────────────┤
│ id          │
│ fullName    │
│ email       │
│ phone       │
│ status      │ (LEAD, QUALIFIED, NEGOTIATING, SOLD, LOST)
│ budgetMin   │
│ budgetMax   │
│ assignedTo  │ → User (agent)
└─────────────┘
      │ 1
      │ has
      ↓ N
┌──────────────────────────┐
│ CustomerSearchCriteria   │ (Property preferences)
├──────────────────────────┤
│ id                       │
│ customer                 │ → Customer
│ attribute                │ → PropertyAttribute
│ value                    │
└──────────────────────────┘
```

### Key Enums

```java
// User roles
enum Role { ADMIN, BROKER, AGENT, ASSISTANT }

// User status
enum UserStatus { ACTIVE, INACTIVE }

// Property status
enum PropertyStatus { ACTIVE, INACTIVE, SOLD }

// Customer lifecycle
enum CustomerStatus { LEAD, QUALIFIED, NEGOTIATING, SOLD, LOST }

// Attribute data types
enum PropertyDataType {
    TEXT, NUMBER, BOOLEAN, DATE,
    SINGLE_SELECT, MULTI_SELECT
}

// Attribute categories
enum PropertyCategory {
    BASIC, STRUCTURE, FEATURES, LOCATION, FINANCIAL
}

// Interaction types
enum InteractionType {
    CALL, EMAIL, MEETING, PROPERTY_VIEWING,
    FOLLOW_UP, CONTRACT_NEGOTIATION, OTHER
}
```

### Entity Relationships Summary

- **User → Properties**: One-to-Many (agent owns properties)
- **User → Customers**: One-to-Many (agent manages customers)
- **User → UserHierarchy**: Many-to-Many (supervisor/subordinate)
- **Property → AttributeValues**: One-to-Many
- **Property → PropertySharing**: One-to-Many (sharing with other users)
- **PropertyAttribute → AttributeValues**: One-to-Many
- **PropertyAttribute → PropertyAttributeOptions**: One-to-Many
- **Customer → CustomerSearchCriteria**: One-to-Many
- **Customer → CustomerInteractions**: One-to-Many
- **Customer → CustomerNotes**: One-to-Many

---

## Development Workflow

### Prerequisites

- **Java 21**
- **Maven 3.6+**
- **Git**

### Setup Steps

```bash
# 1. Clone repository (if needed)
git clone <repository-url>
cd real-estate-crm

# 2. Set environment variables
export JWT_SECRET=$(openssl rand -base64 64)
export ADMIN_PASSWORD="YourSecurePassword123!"
export CORS_ALLOWED_ORIGINS="http://localhost:3000"

# 3. Run application
./mvnw spring-boot:run

# 4. Access services
# - API: http://localhost:8080/swagger-ui/index.html
# - Health: http://localhost:8080/actuator/health
# - H2 Console (dev): http://localhost:8080/h2-console
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=PropertyAttributeControllerTests
```

### Building for Production

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run with production profile
export SPRING_PROFILES_ACTIVE=prod
java -jar target/real-estate-crm-0.0.1-SNAPSHOT.jar
```

---

## Key Conventions

### Naming Conventions

| Component | Pattern | Example |
|-----------|---------|---------|
| Controllers | `[Entity]Controller` | `PropertyController` |
| Services | `[Entity]Service` | `PropertyService` |
| Repositories | `[Entity]Repository` | `PropertyRepository` |
| Entities | `[EntityName]` | `Property`, `User` |
| Mappers | `[Entity]Mapper` | `PropertyMapper` |
| Request DTOs | `[Action][Entity]Request` | `CreatePropertyRequest` |
| Response DTOs | `[Entity]Response` | `PropertyResponse` |
| Enums | PascalCase | `PropertyStatus`, `Role` |

### Package Organization

```
com.realestatecrm/
└── dto/
    ├── property/
    │   ├── request/      ← POST/PUT request bodies
    │   └── response/     ← GET response bodies
    ├── customer/
    │   ├── request/
    │   └── response/
    └── common/           ← Shared DTOs (MessageResponse)
```

### REST Endpoint Patterns

```
/api/auth/*           # Authentication endpoints
/api/properties/*     # Property CRUD
/api/customers/*      # Customer CRUD
/api/users/*          # User management
/api/property-attributes/*  # Admin-only attribute config
/api/saved-searches/* # Saved search CRUD
```

### HTTP Method Usage

| Method | Purpose | Example |
|--------|---------|---------|
| GET | Retrieve resource(s) | `GET /api/properties` |
| POST | Create new resource | `POST /api/properties` |
| PUT | Update entire resource | `PUT /api/properties/{id}` |
| DELETE | Delete resource | `DELETE /api/properties/{id}` |

---

## Common Patterns

### 1. Creating a New Feature (Step-by-Step)

When adding a new entity/feature, follow this order:

```
1. Create Entity           → /entity/MyEntity.java
2. Create Repository       → /repository/MyEntityRepository.java
3. Create Service          → /service/MyEntityService.java
4. Create DTOs             → /dto/myentity/request/* and /dto/myentity/response/*
5. Create Mapper           → /mapper/MyEntityMapper.java
6. Create Controller       → /controller/MyEntityController.java
7. Add Validation          → Annotations in DTOs and entities
8. Handle Exceptions       → Update GlobalExceptionHandler if needed
9. Write Tests             → /src/test/java/*
10. Update Documentation   → docs/* and Swagger annotations
```

### 2. MapStruct Mapper Pattern

**Always use MapStruct for entity ↔ DTO conversion.**

```java
@Mapper(componentModel = "spring")
public interface PropertyMapper {

    // Entity → Response (for GET requests)
    default PropertyResponse toResponse(Property property) {
        if (property == null) return null;
        return new PropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getAgent().getFullName()  // Access within transaction!
        );
    }

    // Request → Entity (for POST/PUT requests)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agent", ignore = true)  // Set manually in controller
    @Mapping(target = "createdDate", ignore = true)  // Managed by JPA
    @Mapping(target = "updatedDate", ignore = true)
    Property toEntity(CreatePropertyRequest request);

    // List mapping (auto-generated by MapStruct)
    List<PropertyResponse> toResponseList(List<Property> properties);
}
```

**Key Rules:**
- Use `@Mapping(target = "field", ignore = true)` for fields that should not be mapped
- Use `default` methods for complex mappings (especially with immutable records)
- Use `expression` for custom logic: `@Mapping(target = "x", expression = "java(...)")`
- Always set `componentModel = "spring"` for Spring dependency injection

### 3. Service Layer Pattern

```java
@Service
@Transactional  // Default: read-write transactions
public class PropertyService {

    private final PropertyRepository repository;
    private final PropertyMapper mapper;

    public PropertyService(PropertyRepository repository, PropertyMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // Read-only operations (optimization)
    @Transactional(readOnly = true)
    public PropertyResponse getById(Long id) {
        Property property = repository.findByIdWithAgent(id)
            .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        // IMPORTANT: Convert to DTO within transaction!
        return mapper.toResponse(property);
    }

    // Write operation
    public PropertyResponse create(CreatePropertyRequest request, User currentUser) {
        Property property = mapper.toEntity(request);
        property.setAgent(currentUser);  // Set manually
        property.setStatus(PropertyStatus.ACTIVE);

        Property saved = repository.save(property);
        return mapper.toResponse(saved);
    }
}
```

**Key Rules:**
- **ALWAYS** mark service classes with `@Transactional`
- Use `@Transactional(readOnly = true)` for read-only methods (performance)
- Convert entities to DTOs **within the transaction** (before method returns)
- Throw specific exceptions (`EntityNotFoundException`, `IllegalArgumentException`)

### 4. Controller Pattern

```java
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT', 'BROKER', 'ADMIN')")
    public ResponseEntity<PropertyResponse> getById(@PathVariable Long id) {
        PropertyResponse response = propertyService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('AGENT') or hasRole('BROKER')")
    public ResponseEntity<PropertyResponse> create(
            @Valid @RequestBody CreatePropertyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userService.findByUsername(userDetails.getUsername());
        PropertyResponse response = propertyService.create(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Property deleted successfully"));
    }
}
```

**Key Rules:**
- Use `@Valid` on request bodies to trigger validation
- Use `@AuthenticationPrincipal` to get current user
- Use `@PreAuthorize` for method-level security
- Return `ResponseEntity<T>` with appropriate HTTP status codes
- **NEVER** return entities directly (always use DTOs)

### 5. Repository Pattern with @EntityGraph

**Problem:** Lazy-loaded relationships cause `LazyInitializationException` when accessed outside transactions.

**Solution:** Use `@EntityGraph` to eagerly fetch relationships when needed.

```java
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Standard findById (doesn't fetch agent)
    Optional<Property> findById(Long id);

    // Custom method WITH agent loaded
    @EntityGraph(attributePaths = {"agent"})
    Optional<Property> findByIdWithAgent(Long id);

    // Fetch multiple associations
    @EntityGraph(attributePaths = {"agent", "attributeValues", "attributeValues.attribute"})
    Optional<Property> findByIdWithDetails(Long id);

    // List query with agent
    @EntityGraph(attributePaths = {"agent"})
    List<Property> findByStatus(PropertyStatus status);

    // Custom JPQL query with EntityGraph
    @EntityGraph(attributePaths = {"agent"})
    @Query("SELECT p FROM Property p WHERE p.price BETWEEN :min AND :max")
    List<Property> findByPriceRange(@Param("min") BigDecimal min, @Param("max") BigDecimal max);
}
```

**When to Use:**
- Always use when the controller/service will access lazy relationships
- Use for any query whose results will be serialized to JSON
- Specify ALL relationships that will be accessed

### 6. DTO Record Pattern (Immutable DTOs)

**Modern Java records are preferred for DTOs:**

```java
// Response DTO
public record PropertyResponse(
    Long id,
    String title,
    String description,
    BigDecimal price,
    Long agentId,
    String agentName,
    PropertyStatus status,
    LocalDateTime createdDate,
    LocalDateTime updatedDate,
    List<AttributeValueResponse> attributeValues  // Optional field
) {
    // Compact constructor for default values
    public PropertyResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        Long agentId,
        String agentName,
        PropertyStatus status,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
    ) {
        this(id, title, description, price, agentId, agentName, status,
             createdDate, updatedDate, null);  // attributeValues = null
    }
}

// Request DTO
public record CreatePropertyRequest(
    @NotBlank(message = "Title is required")
    String title,

    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    BigDecimal price
) {}
```

### 7. Validation Pattern

```java
public record CreatePropertyRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    String title,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    BigDecimal price,

    @Email(message = "Invalid email format")
    String contactEmail
) {}
```

**Common Validation Annotations:**
- `@NotNull`: Field cannot be null
- `@NotBlank`: String cannot be null, empty, or whitespace
- `@Size(min, max)`: String/collection size constraints
- `@Email`: Email format validation
- `@DecimalMin`, `@DecimalMax`: Numeric range
- `@Pattern(regexp)`: Regex validation
- `@Valid`: Nested object validation

### 8. Exception Handling Pattern

**All exceptions are handled by `GlobalExceptionHandler`:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(400, "Validation failed", errors));
    }
}
```

**When to throw what:**
- `EntityNotFoundException`: Resource not found (404)
- `IllegalArgumentException`: Invalid business logic (400)
- `AccessDeniedException`: Insufficient permissions (403)
- `DataIntegrityViolationException`: Database constraint violation (409)

---

## Security Guidelines

### Authentication Flow

```
1. POST /api/auth/login with { username, password }
   ↓
2. Server validates credentials
   ↓
3. Returns { token, refreshToken, user, expiresIn }
   ↓
4. Client stores token (memory or sessionStorage)
   ↓
5. Client includes token in all requests:
   Authorization: Bearer <token>
   ↓
6. Token expires after 24 hours
   ↓
7. POST /api/auth/refresh with { refreshToken } to get new tokens
```

### Authorization Patterns

```java
// Single role
@PreAuthorize("hasRole('ADMIN')")

// Multiple roles (OR)
@PreAuthorize("hasAnyRole('AGENT', 'BROKER')")

// Multiple roles (alternative syntax)
@PreAuthorize("hasRole('AGENT') or hasRole('BROKER')")

// Custom permission check
@PreAuthorize("@permissionService.canAccessProperty(#propertyId, principal)")

// SpEL expressions
@PreAuthorize("hasRole('AGENT') and #property.agent.id == principal.id")
```

### Role Hierarchy

```
ADMIN
  ├─ Can do everything
  └─ Manages property attributes

BROKER
  ├─ Manages agents
  └─ Views all properties

AGENT
  ├─ Creates/manages properties
  ├─ Manages customers
  └─ Views shared properties

ASSISTANT
  └─ Read-only access
```

### Security Checklist

When implementing new features:

- [ ] Validate user has permission to access resource
- [ ] Use `@AuthenticationPrincipal` to get current user
- [ ] Apply `@PreAuthorize` on sensitive operations
- [ ] **NEVER** log sensitive data (passwords, tokens)
- [ ] Validate input with `@Valid` annotations
- [ ] Check resource ownership (prevent IDOR vulnerabilities)
- [ ] Use parameterized queries (prevent SQL injection)
- [ ] Sanitize user input before storage

### Environment Variables

**Required:**
- `JWT_SECRET`: HMAC-SHA256 secret (generate with `openssl rand -base64 64`)
- `ADMIN_PASSWORD`: Initial admin password (min 12 chars)

**Recommended:**
- `CORS_ALLOWED_ORIGINS`: Comma-separated frontend URLs

**Optional:**
- `JWT_EXPIRATION`: Token expiration in ms (default: 86400000 = 24h)
- `ADMIN_USERNAME`: Admin username (default: `admin`)

See `SECURITY.md` for comprehensive security documentation.

---

## Testing Guidelines

### Testing Patterns

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PropertyControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "agent", roles = {"AGENT"})
    void shouldCreateProperty() throws Exception {
        CreatePropertyRequest request = new CreatePropertyRequest(
            "Test Property", "Description", new BigDecimal("100000")
        );

        mockMvc.perform(post("/api/properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Property"))
            .andExpect(jsonPath("$.price").value(100000));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ASSISTANT"})
    void shouldForbidCreateProperty_whenNotAuthorized() throws Exception {
        mockMvc.perform(post("/api/properties")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

### Test Utilities

- `@SpringBootTest`: Full application context
- `@AutoConfigureMockMvc`: Auto-configure MockMvc
- `@ActiveProfiles("dev")`: Use dev profile for tests
- `@WithMockUser`: Mock authenticated user
- `@Transactional`: Rollback after test (clean database)

---

## Common Pitfalls

### 1. LazyInitializationException (CRITICAL!)

**The Problem:** Most common error in this codebase!

Lazy-loaded JPA relationships (e.g., `property.getAgent()`) throw `LazyInitializationException` when accessed **outside** of a transaction.

**Why it Happens:**

```java
// BAD Example
@Service
public class PropertyService {
    @Transactional
    public Property getProperty(Long id) {
        return repository.findById(id).get();
    } // ← Transaction ends here!
}

@RestController
public class PropertyController {
    public PropertyResponse get(Long id) {
        Property property = service.getProperty(id);
        // ERROR: Accessing lazy relationship outside transaction!
        return new PropertyResponse(
            property.getId(),
            property.getAgent().getName()  // LazyInitializationException!
        );
    }
}
```

**Solutions:**

**Option 1: Use @EntityGraph** (Recommended)

```java
@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    @EntityGraph(attributePaths = {"agent"})
    Optional<Property> findByIdWithAgent(Long id);
}
```

**Option 2: Convert to DTO Within Transaction**

```java
@Service
public class PropertyService {
    @Transactional(readOnly = true)
    public PropertyResponse getProperty(Long id) {
        Property property = repository.findById(id).get();
        // Convert while still in transaction
        return mapper.toResponse(property);  // Accesses agent here
    }
}
```

**Option 3: Controller-Level Transaction** (Not recommended)

```java
@GetMapping("/{id}")
@Transactional(readOnly = true)  // Keep transaction open
public PropertyResponse get(Long id) {
    Property property = service.getProperty(id);
    return mapper.toResponse(property);  // OK: Transaction still active
}
```

**Read Full Guide:** `/docs/LazyInitializationException-Prevention.md`

### 2. Returning Entities from Controllers

**NEVER** return JPA entities directly from controllers:

```java
// ❌ BAD
@GetMapping("/{id}")
public Property get(Long id) {
    return propertyService.get(id);  // LazyInitializationException + security issues
}

// ✅ GOOD
@GetMapping("/{id}")
public PropertyResponse get(Long id) {
    return propertyService.getPropertyResponse(id);  // Returns DTO
}
```

**Why?**
- Causes `LazyInitializationException` during JSON serialization
- Exposes internal entity structure
- May leak sensitive data (e.g., password hashes)
- Makes API fragile to entity changes

### 3. Forgetting @Transactional

**Always** mark service methods as `@Transactional`:

```java
// ❌ BAD
@Service
public class PropertyService {
    public void create(Property property) {
        repository.save(property);  // May not commit!
    }
}

// ✅ GOOD
@Service
@Transactional  // Class-level default
public class PropertyService {
    @Transactional(readOnly = true)  // Optimization for reads
    public Property get(Long id) {
        return repository.findById(id).get();
    }

    public void create(Property property) {  // Uses class-level @Transactional
        repository.save(property);
    }
}
```

### 4. Ignoring Validation

Always validate user input:

```java
// ❌ BAD
@PostMapping
public PropertyResponse create(@RequestBody CreatePropertyRequest request) {
    // No validation!
}

// ✅ GOOD
@PostMapping
public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request) {
    // @Valid triggers validation based on DTO annotations
}
```

### 5. Hardcoding Security Secrets

```java
// ❌ BAD
String jwtSecret = "my-secret-key";

// ✅ GOOD
@Value("${jwt.secret}")
private String jwtSecret;  // From environment variable
```

### 6. Not Using MapStruct

**Always** use MapStruct for DTO mapping:

```java
// ❌ BAD (manual mapping, error-prone)
public PropertyResponse toResponse(Property property) {
    PropertyResponse response = new PropertyResponse();
    response.setId(property.getId());
    response.setTitle(property.getTitle());
    // ... 20 more lines
    return response;
}

// ✅ GOOD (MapStruct)
@Mapper(componentModel = "spring")
public interface PropertyMapper {
    PropertyResponse toResponse(Property property);  // Auto-generated
}
```

### 7. Forgetting to Set Ignored Fields

When using MapStruct request-to-entity mapping:

```java
// In PropertyMapper
@Mapping(target = "id", ignore = true)
@Mapping(target = "agent", ignore = true)
Property toEntity(CreatePropertyRequest request);

// In Controller - MUST set ignored fields manually!
@PostMapping
public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request,
                                @AuthenticationPrincipal UserDetails userDetails) {
    Property property = mapper.toEntity(request);

    // ✅ Set ignored fields manually
    User currentUser = userService.findByUsername(userDetails.getUsername());
    property.setAgent(currentUser);
    property.setStatus(PropertyStatus.ACTIVE);

    return propertyService.save(property);
}
```

---

## Quick Reference

### File Locations

| Need to find... | Look in... |
|-----------------|------------|
| REST endpoints | `/controller/` |
| Business logic | `/service/` |
| Database queries | `/repository/` |
| Entity definitions | `/entity/` |
| Request/response DTOs | `/dto/` |
| DTO mapping logic | `/mapper/` |
| Security config | `/config/SecurityConfig.java` |
| Error handling | `/exception/GlobalExceptionHandler.java` |
| Enumerations | `/enums/` |
| JWT logic | `/security/JwtUtils.java` |
| Configuration | `/src/main/resources/application.yml` |

### Common Commands

```bash
# Run application (dev mode)
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Generate MapStruct mappers
./mvnw clean compile

# Skip tests
./mvnw clean package -DskipTests

# Run with prod profile
export SPRING_PROFILES_ACTIVE=prod
./mvnw spring-boot:run
```

### API Endpoints Quick Reference

```
Authentication:
POST   /api/auth/login              # Login
POST   /api/auth/refresh            # Refresh token
POST   /api/auth/logout             # Logout
GET    /api/auth/user               # Get current user
GET    /api/auth/permissions        # Get user permissions

Properties:
GET    /api/properties              # List all properties
GET    /api/properties/{id}         # Get property by ID
POST   /api/properties              # Create property
PUT    /api/properties/{id}         # Update property
DELETE /api/properties/{id}         # Delete property
POST   /api/properties/{id}/values  # Set attribute values
POST   /api/properties/{id}/share   # Share property

Customers:
GET    /api/customers               # List customers
GET    /api/customers/{id}          # Get customer
POST   /api/customers               # Create customer
PUT    /api/customers/{id}          # Update customer
DELETE /api/customers/{id}          # Delete customer

Users:
GET    /api/users                   # List users (admin)
POST   /api/users                   # Create user (admin)
PUT    /api/users/{id}              # Update user
DELETE /api/users/{id}              # Delete user (admin)

Property Attributes (Admin):
GET    /api/property-attributes     # List attributes
POST   /api/property-attributes     # Create attribute
PUT    /api/property-attributes/{id} # Update attribute
DELETE /api/property-attributes/{id} # Delete attribute
POST   /api/property-attributes/reorder # Reorder attributes
```

### Environment Variables

```bash
# Required
export JWT_SECRET=$(openssl rand -base64 64)
export ADMIN_PASSWORD="SecurePassword123!"

# Recommended
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:4200"

# Optional
export JWT_EXPIRATION=86400000
export ADMIN_USERNAME=admin
export SPRING_PROFILES_ACTIVE=dev
```

### Useful Documentation Links

- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Console (dev)**: http://localhost:8080/h2-console

### Key Files to Read First

1. `README.md` - Quick start guide
2. `SECURITY.md` - Security requirements and best practices
3. `docs/LazyInitializationException-Prevention.md` - Avoid #1 bug
4. `src/main/resources/application.yml` - Configuration
5. `src/main/java/com/realestatecrm/entity/Property.java` - Example entity
6. `src/main/java/com/realestatecrm/controller/PropertyController.java` - Example controller

---

## Working with AI Assistants

### When Making Changes

1. **Always read files before editing** - Use the Read tool
2. **Understand the architecture** - Follow the layered pattern
3. **Use MapStruct for mappings** - Never manual DTO conversion
4. **Add @EntityGraph when needed** - Prevent LazyInitializationException
5. **Follow naming conventions** - Consistency is key
6. **Add validation** - Use Jakarta validation annotations
7. **Test your changes** - Write integration tests
8. **Update documentation** - Keep docs in sync with code

### Best Practices for AI Code Generation

- **Read existing similar code first** - Follow established patterns
- **Generate MapStruct mappers** - Don't write manual mappers
- **Use records for DTOs** - Modern Java pattern
- **Add comprehensive JavaDoc** - Explain complex logic
- **Include error handling** - Consider edge cases
- **Generate tests** - Include happy path and error cases
- **Check security** - Add appropriate @PreAuthorize annotations

### Common AI Tasks

**Task: Add a new entity**
1. Read similar entity (e.g., Property.java)
2. Create entity with JPA annotations
3. Create repository extending JpaRepository
4. Create service with @Transactional
5. Create request/response DTOs
6. Create MapStruct mapper
7. Create controller with security
8. Write tests

**Task: Add a new endpoint**
1. Add method to controller
2. Add @PreAuthorize for security
3. Validate input with @Valid
4. Delegate to service layer
5. Return DTO (never entity)
6. Write integration test

**Task: Fix LazyInitializationException**
1. Identify which relationship is lazy
2. Add @EntityGraph to repository method
3. Update service to use new repository method
4. Ensure DTO conversion happens in @Transactional method

---

## Conclusion

This Real Estate CRM follows well-established Spring Boot patterns and conventions. By understanding the architecture, following the coding patterns, and avoiding common pitfalls (especially `LazyInitializationException`), you can effectively work with and extend this codebase.

**Key Takeaways:**
- **Layered architecture**: Controller → Service → Repository → Entity
- **MapStruct for all mappings**: Never manual DTO conversion
- **@EntityGraph for lazy relationships**: Prevent LazyInitializationException
- **DTOs everywhere**: Never return entities from controllers
- **Security first**: Validate, authorize, and sanitize
- **Transactional boundaries**: Keep conversions inside transactions

**Questions?** Refer to:
- This document (CLAUDE.md)
- Security documentation (SECURITY.md)
- API documentation (docs/API-Documentation.md)
- Lazy loading guide (docs/LazyInitializationException-Prevention.md)

---

**Generated**: 2025-11-19
**Version**: 1.0
**Maintained by**: Development Team
