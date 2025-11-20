# Real Estate CRM Documentation

> **Last Updated**: 2025-11-20
> **Version**: 1.0

Welcome to the Real Estate CRM documentation. This directory contains comprehensive guides for developers, frontend integrators, and system administrators.

---

## 📚 Documentation Index

### For Frontend Developers

#### **[Frontend Integration Guide](Frontend-Integration-Guide.md)** ⭐ **START HERE**
**Complete guide for integrating with the Real Estate CRM API**

- ✅ **60+ API endpoints** with detailed request/response formats
- ✅ **Complete TypeScript type definitions** (copy-paste ready)
- ✅ **Authentication flow** with JWT and refresh tokens
- ✅ **Working code examples** for all operations
- ✅ **Error handling** patterns and response formats
- ✅ **Pagination**, sorting, and filtering details
- ✅ **Full-featured client class** with auto-refresh
- ✅ **Role-based access control** documentation

**Covers:**
- Authentication (login, refresh, logout)
- Property Management (CRUD, attributes, sharing, search)
- Customer Management (CRUD, notes, interactions)
- User Management (CRUD, hierarchies)
- Property Attributes (dynamic attribute system)
- Saved Searches (create and execute searches)

#### **[API Testing Guide](API-Testing-Guide.md)** 🧪 **PRACTICAL**
**Quick and easy way to test all API endpoints with mock data**

- ✅ **100+ test requests** covering all endpoints
- ✅ **Pre-configured test users** with different roles
- ✅ **VS Code REST Client** integration
- ✅ **Common test scenarios** and workflows
- ✅ **Troubleshooting tips** and debugging

---

### For Backend Developers

#### **[Lazy Initialization Prevention](Lazy-Initialization-Prevention.md)** ⚠️ **CRITICAL**
Essential guide to prevent the most common bug in this codebase. Required reading before making any changes to entities or repositories.

**Key Topics:**
- Understanding JPA lazy loading
- Using `@EntityGraph` correctly
- Transaction boundary management
- DTO conversion patterns
- Common pitfalls and solutions

---

## 🏗️ Architecture Overview

### Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Boot 3.2.5 |
| Language | Java 21 |
| Database | PostgreSQL (prod), H2 (dev) |
| Security | JWT (JJWT 0.12.6) |
| API Docs | OpenAPI 3.0 / Swagger UI |
| Mapping | MapStruct 1.5.5 |

### Architecture Pattern

**Layered (N-Tier) Architecture**:
```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Entity Layer (Domain Model)
```

---

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.6+
- PostgreSQL (production) or H2 (development)

### Run the Application

```bash
# Set required environment variables
export JWT_SECRET=$(openssl rand -base64 64)
export ADMIN_PASSWORD="YourSecurePassword123!"

# Run in development mode
./mvnw spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui/index.html
```

### Default Credentials (Development)

```
Username: admin
Password: (value of ADMIN_PASSWORD env variable)
Role: ADMIN
```

---

## 📖 API Documentation Access

### Interactive API Documentation (Swagger UI)
**URL**: `http://localhost:8080/swagger-ui/index.html`

Test all endpoints directly from your browser with:
- Interactive request/response testing
- Built-in authentication
- Schema documentation
- Example values

### OpenAPI Specification
**URL**: `http://localhost:8080/v3/api-docs`

Download the raw OpenAPI 3.0 JSON specification for import into tools like:
- Postman
- Insomnia
- API clients generators

---

## 🔐 Authentication

### JWT Token System

```
Access Token:  24 hours (86400 seconds)
Refresh Token: 7 days
```

### Authentication Flow

```
1. POST /api/auth/login → Get tokens
2. Include token in header: Authorization: Bearer <token>
3. POST /api/auth/refresh → Renew tokens before expiration
4. POST /api/auth/logout → Clear session
```

### Role Hierarchy

| Role | Access Level |
|------|-------------|
| **ADMIN** | Full system access, manages attributes |
| **BROKER** | Manages agents and their resources |
| **AGENT** | Manages own properties and customers |
| **ASSISTANT** | Read-only access |

---

## 🔗 API Endpoints Summary

### Core Endpoints

| Category | Base Path | Endpoints | Description |
|----------|-----------|-----------|-------------|
| **Authentication** | `/api/auth` | 6 | Login, refresh, logout, user info |
| **Properties** | `/api/properties` | 14 | CRUD, attributes, sharing, search |
| **Customers** | `/api/customers` | 13 | CRUD, notes, interactions, search |
| **Users** | `/api/users` | 9 | CRUD, hierarchy management |
| **Attributes** | `/api/property-attributes` | 11 | Dynamic attribute configuration |
| **Saved Searches** | `/api/saved-searches` | 7 | Search creation and execution |

**Total**: 60 endpoints

---

## 📦 Data Models

### Core Entities

- **User**: System users (admins, brokers, agents, assistants)
- **Property**: Real estate listings with dynamic attributes
- **Customer**: Prospects and clients with interactions
- **PropertyAttribute**: Dynamic attribute definitions
- **AttributeValue**: Property-specific attribute values
- **SavedSearch**: Named property search criteria
- **CustomerInteraction**: Customer communication history
- **CustomerNote**: Customer notes and comments

### Enumerations

```typescript
enum Role { ADMIN, BROKER, AGENT, ASSISTANT }
enum PropertyStatus { ACTIVE, INACTIVE, SOLD }
enum CustomerStatus { LEAD, QUALIFIED, NEGOTIATING, SOLD, LOST }
enum PropertyDataType { TEXT, NUMBER, BOOLEAN, DATE, SINGLE_SELECT, MULTI_SELECT }
enum InteractionType { CALL, EMAIL, MEETING, PROPERTY_VIEWING, FOLLOW_UP, CONTRACT_NEGOTIATION, OTHER }
```

---

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### H2 Console (Development)
**URL**: `http://localhost:8080/h2-console`

```
JDBC URL: jdbc:h2:mem:testdb
Username: sa
Password: (empty)
```

---

## 🛠️ Development

### Build for Production
```bash
./mvnw clean package -DskipTests
```

### Run with Production Profile
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/real-estate-crm-0.0.1-SNAPSHOT.jar
```

---

## 📝 Additional Resources

### Root Documentation
- **[README.md](../README.md)**: Project overview and quick start
- **[CLAUDE.md](../CLAUDE.md)**: Comprehensive AI assistant guide with architecture, patterns, and conventions
- **[SECURITY.md](../SECURITY.md)**: Security guidelines and best practices

### Common Patterns

1. **DTO Pattern**: All API responses use DTOs, never entities
2. **MapStruct**: Compile-time DTO mapping (no manual mapping)
3. **@EntityGraph**: Eager fetch lazy relationships to prevent LazyInitializationException
4. **@Transactional**: All service methods are transactional
5. **@PreAuthorize**: Method-level security for role-based access

---

## 🆘 Troubleshooting

### Common Issues

#### 1. LazyInitializationException
**Symptom**: `could not initialize proxy - no Session`

**Solution**: Use `@EntityGraph` in repository methods to eagerly fetch relationships
```java
@EntityGraph(attributePaths = {"agent"})
Optional<Property> findByIdWithAgent(Long id);
```

See: [Lazy-Initialization-Prevention.md](Lazy-Initialization-Prevention.md)

#### 2. 401 Unauthorized
**Causes**:
- Missing or invalid JWT token
- Token expired (24-hour lifetime)
- Incorrect Authorization header format

**Solution**: Refresh token or re-authenticate

#### 3. 403 Forbidden
**Cause**: User role lacks permission for the operation

**Solution**: Check endpoint's required roles in [FRONTEND-INTEGRATION-GUIDE.md](FRONTEND-INTEGRATION-GUIDE.md)

#### 4. 404 Not Found
**Causes**:
- Resource doesn't exist
- User doesn't have access to resource (privacy filter)

**Solution**: Verify resource ID and user permissions

---

## 📞 Support

### For Frontend Integration Questions
1. Check [Frontend-Integration-Guide.md](Frontend-Integration-Guide.md)
2. Test endpoints in Swagger UI: `http://localhost:8080/swagger-ui/index.html`
3. Review error response for detailed validation messages

### For Backend Development Questions
1. Read [CLAUDE.md](../CLAUDE.md) for comprehensive patterns
2. Check [Lazy-Initialization-Prevention.md](Lazy-Initialization-Prevention.md) for JPA issues
3. Review existing similar code in `/controller`, `/service`, or `/repository`

---

## 📄 Document Maintenance

### When to Update This Documentation

- ✅ New API endpoints added
- ✅ Request/response formats changed
- ✅ Authentication flow modified
- ✅ New enums or data types added
- ✅ Business rules changed

### Documentation Standards

- Use clear, concise language
- Include code examples for complex concepts
- Provide both cURL and JavaScript/TypeScript examples
- Keep TypeScript type definitions up to date
- Document all enum values
- Include error cases and validation rules

---

**Last Updated**: 2025-11-19
**Maintained by**: Development Team
**API Version**: 1.0
