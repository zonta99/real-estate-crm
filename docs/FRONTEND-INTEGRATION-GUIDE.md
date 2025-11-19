# Frontend Integration Guide

## Table of Contents

1. [Overview](#overview)
2. [Base Configuration](#base-configuration)
3. [Authentication](#authentication)
4. [Error Handling](#error-handling)
5. [Enumerations](#enumerations)
6. [API Endpoints](#api-endpoints)
   - [Authentication Endpoints](#authentication-endpoints)
   - [Property Endpoints](#property-endpoints)
   - [Customer Endpoints](#customer-endpoints)
   - [User Endpoints](#user-endpoints)
   - [Property Attribute Endpoints](#property-attribute-endpoints)
   - [Saved Search Endpoints](#saved-search-endpoints)

---

## Overview

This is a RESTful API for a Real Estate CRM system built with Spring Boot 3.2.5. All endpoints return JSON responses and expect JSON request bodies where applicable.

**API Version**: 1.0  
**Base Path**: `/api`  
**Default Port**: `8080`

---

## Base Configuration

### Base URL
```
http://localhost:8080/api
```

### Content Type
All requests with a body should include:
```
Content-Type: application/json
```

### Authentication Header
Protected endpoints require JWT token:
```
Authorization: Bearer <your-jwt-token>
```

---

## Authentication

### Authentication Flow

1. **Login** with username and password
2. Receive **JWT access token** (expires in 24 hours) and **refresh token**
3. Include access token in `Authorization` header for all protected endpoints
4. When access token expires, use **refresh token** to get new tokens
5. **Logout** to clear session (optional)

### JWT Token Expiration
- **Access Token**: 24 hours (86400 seconds)
- **Refresh Token**: 7 days

### Example Authentication Flow

```javascript
// 1. Login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'agent1',
    password: 'password123'
  })
});
const { token, refreshToken, user, expiresIn } = await loginResponse.json();

// 2. Store tokens
localStorage.setItem('accessToken', token);
localStorage.setItem('refreshToken', refreshToken);

// 3. Use access token for authenticated requests
const propertiesResponse = await fetch('http://localhost:8080/api/properties', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

// 4. Refresh token when access token expires
const refreshResponse = await fetch('http://localhost:8080/api/auth/refresh', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ refreshToken })
});
const { accessToken, refreshToken: newRefreshToken } = await refreshResponse.json();
```

---

## Error Handling

### Standard Error Response Format

All errors follow this structure:

```typescript
interface ErrorResponse {
  status: number;           // HTTP status code (400, 401, 403, 404, 409, 500)
  error: string;            // Error category (e.g., "Validation Failed")
  message: string;          // Human-readable error message
  path: string;             // Request path that caused error
  timestamp: string;        // ISO 8601 timestamp
  validationErrors?: {      // Present only for validation errors (400)
    [fieldName: string]: string;  // Field-specific error messages
  };
}
```

### HTTP Status Codes

| Status | Error Type | Description |
|--------|------------|-------------|
| `400` | Bad Request | Invalid input data or validation errors |
| `401` | Unauthorized | Missing or invalid JWT token |
| `403` | Forbidden | Insufficient permissions for requested resource |
| `404` | Not Found | Requested resource does not exist |
| `409` | Conflict | Data integrity violation (e.g., duplicate entries) |
| `500` | Internal Server Error | Unexpected server error |

### Example Error Responses

#### Validation Error (400)
```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data provided",
  "path": "uri=/api/properties",
  "timestamp": "2025-11-19T10:30:00",
  "validationErrors": {
    "title": "Title is required",
    "price": "Price must be positive"
  }
}
```

#### Authentication Error (401)
```json
{
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials provided",
  "path": "uri=/api/auth/login",
  "timestamp": "2025-11-19T10:30:00",
  "validationErrors": null
}
```

#### Access Denied (403)
```json
{
  "status": 403,
  "error": "Access Denied",
  "message": "You don't have permission to access this resource",
  "path": "uri=/api/users/123",
  "timestamp": "2025-11-19T10:30:00",
  "validationErrors": null
}
```

#### Not Found (404)
```json
{
  "status": 404,
  "error": "Resource Not Found",
  "message": "Property not found with id: 123",
  "path": "uri=/api/properties/123",
  "timestamp": "2025-11-19T10:30:00",
  "validationErrors": null
}
```

#### Data Conflict (409)
```json
{
  "status": 409,
  "error": "Data Conflict",
  "message": "A record with this information already exists",
  "path": "uri=/api/users",
  "timestamp": "2025-11-19T10:30:00",
  "validationErrors": null
}
```

---

## Enumerations

### Role
User roles in the system (highest to lowest privilege):

```typescript
enum Role {
  ADMIN = "ADMIN",           // Full system access, manages property attributes
  BROKER = "BROKER",         // Manages agents, views all properties
  AGENT = "AGENT",           // Manages properties and customers
  ASSISTANT = "ASSISTANT"    // Read-only access
}
```

### UserStatus
```typescript
enum UserStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE"
}
```

### PropertyStatus
```typescript
enum PropertyStatus {
  ACTIVE = "ACTIVE",         // Available for sale
  PENDING = "PENDING",       // Under contract
  SOLD = "SOLD",             // Sale completed
  WITHDRAWN = "WITHDRAWN"    // Removed from market
}
```

### CustomerStatus
Customer lifecycle stages:

```typescript
enum CustomerStatus {
  LEAD = "LEAD",             // Initial contact
  ACTIVE = "ACTIVE",         // Actively working with agent
  CLOSED = "CLOSED",         // Deal completed
  INACTIVE = "INACTIVE"      // No longer active
}
```

### PropertyDataType
Types for dynamic property attributes:

```typescript
enum PropertyDataType {
  TEXT = "TEXT",                   // String value
  NUMBER = "NUMBER",               // Numeric value (BigDecimal)
  BOOLEAN = "BOOLEAN",             // True/false
  DATE = "DATE",                   // Date value
  SINGLE_SELECT = "SINGLE_SELECT", // Single choice from options
  MULTI_SELECT = "MULTI_SELECT"    // Multiple choices from options
}
```

### PropertyCategory
Categories for organizing property attributes:

```typescript
enum PropertyCategory {
  BASIC = "BASIC",           // Basic information (title, price, etc.)
  STRUCTURE = "STRUCTURE",   // Structural details (bedrooms, bathrooms, sq ft)
  FEATURES = "FEATURES",     // Amenities (pool, garage, etc.)
  LOCATION = "LOCATION",     // Location details (address, neighborhood)
  FINANCIAL = "FINANCIAL"    // Financial info (taxes, HOA, etc.)
}
```

### InteractionType
Types of customer interactions:

```typescript
enum InteractionType {
  PHONE_CALL = "PHONE_CALL",
  EMAIL = "EMAIL",
  MEETING = "MEETING",
  PROPERTY_VIEWING = "PROPERTY_VIEWING",
  SMS = "SMS",
  VIDEO_CALL = "VIDEO_CALL",
  OTHER = "OTHER"
}
```

---

## API Endpoints

---

## Authentication Endpoints

Base Path: `/api/auth`

### 1. Login

**Endpoint**: `POST /api/auth/login`  
**Authentication**: None (public)  
**Description**: Authenticate user and receive JWT tokens

**Request Body**:
```typescript
{
  username: string;    // Required, not blank
  password: string;    // Required, not blank
}
```

**Response** (200 OK):
```typescript
{
  token: string;           // JWT access token
  refreshToken: string;    // Refresh token for token renewal
  user: {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    roles: string[];       // e.g., ["ROLE_AGENT"]
    status: string;        // "ACTIVE" | "INACTIVE"
    createdDate: string;   // ISO 8601 format
    updatedDate: string;   // ISO 8601 format
  };
  expiresIn: number;       // Token expiration in seconds (e.g., 86400)
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "agent1", "password": "password123"}'
```

---

### 2. Refresh Token

**Endpoint**: `POST /api/auth/refresh`  
**Authentication**: None (uses refresh token)  
**Description**: Get new access token using refresh token

**Request Body**:
```typescript
{
  refreshToken: string;    // Required, not blank
}
```

**Response** (200 OK):
```typescript
{
  accessToken: string;     // New JWT access token
  refreshToken: string;    // New refresh token (rotated for security)
  expiresAt: string;       // ISO 8601 timestamp
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your-refresh-token"}'
```

---

### 3. Get Current User

**Endpoint**: `GET /api/auth/user`  
**Authentication**: Required  
**Roles**: Any authenticated user  
**Description**: Get current authenticated user's information

**Response** (200 OK):
```typescript
{
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  status: string;
  createdDate: string;
  updatedDate: string;
}
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/auth/user \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 4. Get User Permissions

**Endpoint**: `GET /api/auth/permissions`  
**Authentication**: Required  
**Roles**: Any authenticated user  
**Description**: Get current user's permissions

**Response** (200 OK):
```typescript
Array<{
  id: number;
  name: string;       // Permission name
  description: string;
}>
```

---

### 5. Get Subordinates

**Endpoint**: `GET /api/auth/subordinates`  
**Authentication**: Required  
**Roles**: Any authenticated user  
**Description**: Get list of direct subordinates in organizational hierarchy

**Response** (200 OK):
```typescript
Array<{
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  status: string;
  createdDate: string;
  updatedDate: string;
}>
```

---

### 6. Logout

**Endpoint**: `POST /api/auth/logout`  
**Authentication**: Required  
**Roles**: Any authenticated user  
**Description**: Logout and clear security context

**Response** (200 OK):
```typescript
{
  message: string;    // "Logout successful"
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer your-jwt-token"
```

---

## Property Endpoints

Base Path: `/api/properties`

### 1. Get All Properties (Paginated)

**Endpoint**: `GET /api/properties`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get paginated list of properties (agents see only their properties and subordinates', admins see all)

**Query Parameters**:
- `status` (optional): `PropertyStatus` enum value
- `minPrice` (optional): `BigDecimal` minimum price
- `maxPrice` (optional): `BigDecimal` maximum price
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort criteria (e.g., "createdDate,desc")

**Response** (200 OK):
```typescript
{
  content: Array<{
    id: number;
    title: string;
    description: string;
    price: number;
    agentId: number;
    agentName: string;
    status: PropertyStatus;
    createdDate: string;      // ISO 8601
    updatedDate: string;      // ISO 8601
    attributeValues?: null;   // Not included in list view
  }>;
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}
```

**Example**:
```bash
curl -X GET "http://localhost:8080/api/properties?page=0&size=20&status=ACTIVE" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 2. Get Property by ID

**Endpoint**: `GET /api/properties/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get property details without dynamic attributes (faster)

**Response** (200 OK):
```typescript
{
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;
  createdDate: string;
  updatedDate: string;
  attributeValues: null;    // Not included
}
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/properties/1 \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 3. Get Property with Attributes

**Endpoint**: `GET /api/properties/{id}/with-attributes`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get property with all dynamic attributes

**Response** (200 OK):
```typescript
{
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;
  createdDate: string;
  updatedDate: string;
  attributeValues: Array<{
    id: number;
    propertyId: number;
    attributeId: number;
    attributeName: string;
    dataType: string;        // PropertyDataType
    value: any;              // Type depends on dataType
  }>;
}
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/properties/1/with-attributes \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 4. Create Property

**Endpoint**: `POST /api/properties`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Create a new property (automatically assigned to current user)

**Request Body**:
```typescript
{
  title: string;           // Required, not blank
  description?: string;    // Optional
  price: number;           // Required, must be > 0
}
```

**Response** (201 Created):
```typescript
{
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;  // Will be "ACTIVE"
  createdDate: string;
  updatedDate: string;
  attributeValues: null;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/properties \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Beautiful Family Home",
    "description": "3BR/2BA in great neighborhood",
    "price": 450000
  }'
```

---

### 5. Update Property

**Endpoint**: `PUT /api/properties/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Update property details

**Request Body**:
```typescript
{
  title: string;           // Required, not blank
  description?: string;    // Optional
  price: number;           // Required, must be > 0
  status: PropertyStatus;  // Required
}
```

**Response** (200 OK):
```typescript
{
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;
  createdDate: string;
  updatedDate: string;
  attributeValues: null;
}
```

**Example**:
```bash
curl -X PUT http://localhost:8080/api/properties/1 \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Beautiful Family Home - UPDATED",
    "description": "3BR/2BA in great neighborhood",
    "price": 425000,
    "status": "PENDING"
  }'
```

---

### 6. Delete Property

**Endpoint**: `DELETE /api/properties/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Delete a property

**Response** (200 OK):
```typescript
{
  message: string;    // "Property deleted successfully"
}
```

**Example**:
```bash
curl -X DELETE http://localhost:8080/api/properties/1 \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 7. Set Attribute Value

**Endpoint**: `POST /api/properties/{id}/values`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Set or update a dynamic attribute value for a property

**Request Body**:
```typescript
{
  attributeId: number;     // Required
  value: any;              // Type depends on attribute's dataType
}
```

**Response** (200 OK):
```typescript
{
  id: number;
  propertyId: number;
  attributeId: number;
  attributeName: string;
  dataType: string;
  value: any;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/properties/1/values \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "attributeId": 5,
    "value": 3
  }'
```

---

### 8. Get Attribute Values

**Endpoint**: `GET /api/properties/{id}/values`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get all attribute values for a property

**Response** (200 OK):
```typescript
Array<{
  id: number;
  propertyId: number;
  attributeId: number;
  attributeName: string;
  dataType: string;
  value: any;
}>
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/properties/1/values \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 9. Delete Attribute Value

**Endpoint**: `DELETE /api/properties/{id}/values/{attributeId}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Remove an attribute value from a property

**Response** (200 OK):
```typescript
{
  message: string;    // "Attribute value deleted successfully"
}
```

**Example**:
```bash
curl -X DELETE http://localhost:8080/api/properties/1/values/5 \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 10. Share Property

**Endpoint**: `POST /api/properties/{id}/share`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Share a property with another user

**Request Body**:
```typescript
{
  sharedWithUserId: number;    // Required
}
```

**Response** (200 OK):
```typescript
{
  message: string;    // "Property shared successfully"
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/properties/1/share \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"sharedWithUserId": 5}'
```

---

### 11. Unshare Property

**Endpoint**: `DELETE /api/properties/{id}/share/{userId}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Remove property sharing with a user

**Response** (200 OK):
```typescript
{
  message: string;    // "Property unshared successfully"
}
```

**Example**:
```bash
curl -X DELETE http://localhost:8080/api/properties/1/share/5 \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 12. Get Property Sharing

**Endpoint**: `GET /api/properties/{id}/sharing`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get list of users a property is shared with

**Response** (200 OK):
```typescript
Array<{
  id: number;
  propertyId: number;
  sharedWithUserId: number;
  sharedWithUserName: string;
  sharedByUserId: number;
  sharedByUserName: string;
  createdDate: string;
}>
```

**Example**:
```bash
curl -X GET http://localhost:8080/api/properties/1/sharing \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 13. Search Properties (Simple)

**Endpoint**: `GET /api/properties/search`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Simple property search by price and status

**Query Parameters**:
- `minPrice` (optional): Minimum price
- `maxPrice` (optional): Maximum price
- `status` (optional): PropertyStatus enum value

**Response** (200 OK):
```typescript
Array<{
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;
  createdDate: string;
  updatedDate: string;
  attributeValues: null;
}>
```

**Example**:
```bash
curl -X GET "http://localhost:8080/api/properties/search?minPrice=300000&maxPrice=500000&status=ACTIVE" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 14. Search Properties by Criteria (Advanced)

**Endpoint**: `POST /api/properties/search/by-criteria`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Advanced property search with dynamic attribute filters

**Request Body**:
```typescript
{
  filters: Array<{
    attributeId: number;           // Required
    dataType: PropertyDataType;    // Required
    
    // For NUMBER type:
    minValue?: number;
    maxValue?: number;
    
    // For DATE type:
    minDate?: string;              // ISO 8601 date
    maxDate?: string;              // ISO 8601 date
    
    // For SINGLE_SELECT/MULTI_SELECT:
    selectedValues?: string[];
    
    // For TEXT type:
    textValue?: string;
    
    // For BOOLEAN type:
    booleanValue?: boolean;
  }>;
  page?: number;                   // Default: 0, min: 0
  size?: number;                   // Default: 20, min: 1, max: 100
  sort?: string;                   // Default: "createdDate,desc"
}
```

**Response** (200 OK): Paginated PropertyResponse (same as Get All Properties)

**Example**:
```bash
curl -X POST http://localhost:8080/api/properties/search/by-criteria \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "filters": [
      {
        "attributeId": 1,
        "dataType": "NUMBER",
        "minValue": 3,
        "maxValue": 5
      },
      {
        "attributeId": 2,
        "dataType": "SINGLE_SELECT",
        "selectedValues": ["Garage", "Carport"]
      }
    ],
    "page": 0,
    "size": 20,
    "sort": "price,asc"
  }'
```

---

## Customer Endpoints

Base Path: `/api/customers`

### 1. Get All Customers (Paginated)

**Endpoint**: `GET /api/customers`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get paginated list of customers (agents see only their customers)

**Query Parameters**:
- `status` (optional): `CustomerStatus` enum value
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort criteria

**Response** (200 OK): Paginated CustomerResponse

```typescript
{
  content: Array<{
    id: number;
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    budgetMin: number | null;
    budgetMax: number | null;
    notes: string | null;
    leadSource: string | null;
    status: CustomerStatus;
    agentId: number;
    agentName: string;
    createdDate: string;
    updatedDate: string;
  }>;
  // ... pagination metadata
}
```

**Example**:
```bash
curl -X GET "http://localhost:8080/api/customers?page=0&size=20&status=LEAD" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 2. Get Customer by ID

**Endpoint**: `GET /api/customers/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK):
```typescript
{
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  budgetMin: number | null;
  budgetMax: number | null;
  notes: string | null;
  leadSource: string | null;
  status: CustomerStatus;
  agentId: number;
  agentName: string;
  createdDate: string;
  updatedDate: string;
}
```

---

### 3. Create Customer

**Endpoint**: `POST /api/customers`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Create a new customer (automatically assigned to current user)

**Request Body**:
```typescript
{
  firstName: string;        // Required, not blank
  lastName: string;         // Required, not blank
  phone: string;            // Required, not blank
  email?: string;           // Optional, must be valid email
  budgetMin?: number;       // Optional
  budgetMax?: number;       // Optional
  notes?: string;           // Optional
  leadSource?: string;      // Optional
}
```

**Response** (201 Created): CustomerResponse

**Example**:
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "phone": "555-1234",
    "email": "john.doe@example.com",
    "budgetMin": 300000,
    "budgetMax": 500000,
    "leadSource": "Website"
  }'
```

---

### 4. Update Customer

**Endpoint**: `PUT /api/customers/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Request Body**:
```typescript
{
  firstName: string;        // Required, not blank
  lastName: string;         // Required, not blank
  phone: string;            // Required, not blank
  email?: string;           // Optional, must be valid email
  budgetMin?: number;       // Optional
  budgetMax?: number;       // Optional
  notes?: string;           // Optional
  leadSource?: string;      // Optional
  status: CustomerStatus;   // Required
}
```

**Response** (200 OK): CustomerResponse

---

### 5. Delete Customer

**Endpoint**: `DELETE /api/customers/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK):
```typescript
{
  message: string;    // "Customer deleted successfully"
}
```

---

### 6. Search Customers

**Endpoint**: `GET /api/customers/search`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Query Parameters**:
- `name` (optional): Search by first or last name
- `status` (optional): CustomerStatus
- `phone` (optional): Phone number
- `email` (optional): Email address

**Response** (200 OK): Array of CustomerResponse

**Example**:
```bash
curl -X GET "http://localhost:8080/api/customers/search?name=John&status=LEAD" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 7. Search by Budget Range

**Endpoint**: `GET /api/customers/budget-range`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Query Parameters**:
- `minBudget` (required): Minimum budget
- `maxBudget` (required): Maximum budget

**Response** (200 OK): Array of CustomerResponse

**Example**:
```bash
curl -X GET "http://localhost:8080/api/customers/budget-range?minBudget=300000&maxBudget=500000" \
  -H "Authorization: Bearer your-jwt-token"
```

---

### Customer Notes Endpoints

### 8. Add Customer Note

**Endpoint**: `POST /api/customers/{id}/notes`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Request Body**:
```typescript
{
  content: string;    // Required, not blank
}
```

**Response** (201 Created):
```typescript
{
  id: number;
  customerId: number;
  customerName: string;
  createdByUserId: number;
  createdByUserName: string;
  content: string;
  createdDate: string;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/customers/1/notes \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"content": "Customer is interested in 3BR homes"}'
```

---

### 9. Get Customer Notes

**Endpoint**: `GET /api/customers/{id}/notes`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK): Array of CustomerNoteResponse

---

### 10. Delete Customer Note

**Endpoint**: `DELETE /api/customers/{id}/notes/{noteId}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK):
```typescript
{
  message: string;    // "Customer note deleted successfully"
}
```

---

### Customer Interactions Endpoints

### 11. Create Customer Interaction

**Endpoint**: `POST /api/customers/{id}/interactions`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Request Body**:
```typescript
{
  type: InteractionType;           // Required
  subject: string;                 // Required, not blank
  notes?: string;                  // Optional
  interactionDate: string;         // Required, ISO 8601 datetime
  durationMinutes?: number;        // Optional
  relatedPropertyId?: number;      // Optional
}
```

**Response** (201 Created):
```typescript
{
  id: number;
  customerId: number;
  customerName: string;
  userId: number;
  userName: string;
  type: InteractionType;
  subject: string;
  notes: string | null;
  interactionDate: string;
  durationMinutes: number | null;
  relatedPropertyId: number | null;
  relatedPropertyTitle: string | null;
  createdDate: string;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/customers/1/interactions \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "PHONE_CALL",
    "subject": "Initial consultation call",
    "notes": "Discussed budget and preferences",
    "interactionDate": "2025-11-19T10:30:00",
    "durationMinutes": 30,
    "relatedPropertyId": 5
  }'
```

---

### 12. Get Customer Interactions

**Endpoint**: `GET /api/customers/{id}/interactions`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK): Array of CustomerInteractionResponse

---

### 13. Delete Customer Interaction

**Endpoint**: `DELETE /api/customers/{id}/interactions/{interactionId}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK):
```typescript
{
  message: string;    // "Customer interaction deleted successfully"
}
```

---

## User Endpoints

Base Path: `/api/users`

### 1. Get All Users (Paginated)

**Endpoint**: `GET /api/users`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`  
**Description**: Get users (brokers see subordinates, admins see all)

**Query Parameters**:
- `page` (optional): Page number
- `size` (optional): Page size
- `sort` (optional): Sort criteria

**Response** (200 OK): Paginated UserResponse

```typescript
{
  content: Array<{
    id: number;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    role: Role;
    status: UserStatus;
    createdDate: string;
    updatedDate: string;
  }>;
  // ... pagination metadata
}
```

---

### 2. Get User by ID

**Endpoint**: `GET /api/users/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, or own user

**Response** (200 OK): UserResponse

---

### 3. Create User

**Endpoint**: `POST /api/users`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Request Body**:
```typescript
{
  username: string;       // Required, 3-20 characters
  password: string;       // Required, min 8 characters, validated
  email: string;          // Required, valid email
  firstName?: string;     // Optional
  lastName?: string;      // Optional
  role: Role;             // Required
}
```

**Response** (201 Created): UserResponse

**Example**:
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newagent",
    "password": "SecurePass123!",
    "email": "newagent@company.com",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "AGENT"
  }'
```

---

### 4. Update User

**Endpoint**: `PUT /api/users/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN` or `BROKER` (if managing subordinate)

**Request Body**:
```typescript
{
  firstName?: string;
  lastName?: string;
  email: string;          // Required, valid email
  role: Role;             // Required
  status: UserStatus;     // Required
}
```

**Response** (200 OK): UserResponse

---

### 5. Delete User

**Endpoint**: `DELETE /api/users/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Response** (200 OK):
```typescript
{
  message: string;    // "User deleted successfully"
}
```

---

### User Hierarchy Endpoints

### 6. Add Supervisor Relationship

**Endpoint**: `POST /api/users/{id}/hierarchy`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`

**Request Body**:
```typescript
{
  supervisorId: number;    // Required
}
```

**Response** (200 OK):
```typescript
{
  message: string;    // "Supervisor relationship added successfully"
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/users/5/hierarchy \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"supervisorId": 2}'
```

---

### 7. Remove Supervisor Relationship

**Endpoint**: `DELETE /api/users/{id}/hierarchy/{supervisorId}`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`

**Response** (200 OK):
```typescript
{
  message: string;    // "Supervisor relationship removed successfully"
}
```

---

### 8. Get Subordinates

**Endpoint**: `GET /api/users/{id}/subordinates`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, or own user

**Response** (200 OK): Array of UserResponse

---

### 9. Get Users by Role

**Endpoint**: `GET /api/users/roles/{role}`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`

**Response** (200 OK): Array of UserResponse

**Example**:
```bash
curl -X GET http://localhost:8080/api/users/roles/AGENT \
  -H "Authorization: Bearer your-jwt-token"
```

---

## Property Attribute Endpoints

Base Path: `/api/property-attributes`

These endpoints manage the dynamic property attribute system (admin-only for modifications).

### 1. Get All Attributes

**Endpoint**: `GET /api/property-attributes`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, `AGENT`, `ASSISTANT`

**Response** (200 OK):
```typescript
Array<{
  id: number;
  name: string;
  dataType: string;                // PropertyDataType
  isRequired: boolean;
  isSearchable: boolean;
  category: string;                // PropertyCategory
  displayOrder: number;
  createdDate: string;
  updatedDate: string;
  options: Array<{
    id: number;
    attributeId: number;
    optionValue: string;
    displayOrder: number;
  }>;
}>
```

---

### 2. Get Searchable Attributes

**Endpoint**: `GET /api/property-attributes/searchable`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, `AGENT`, `ASSISTANT`

**Response** (200 OK): Array of PropertyAttributeResponse (only searchable attributes)

---

### 3. Get Attributes by Category

**Endpoint**: `GET /api/property-attributes/category/{category}`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, `AGENT`, `ASSISTANT`

**Response** (200 OK): Array of PropertyAttributeResponse

**Example**:
```bash
curl -X GET http://localhost:8080/api/property-attributes/category/STRUCTURE \
  -H "Authorization: Bearer your-jwt-token"
```

---

### 4. Get Attribute by ID

**Endpoint**: `GET /api/property-attributes/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, `AGENT`, `ASSISTANT`

**Response** (200 OK): PropertyAttributeResponse

---

### 5. Create Attribute

**Endpoint**: `POST /api/property-attributes`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Request Body**:
```typescript
{
  name: string;                  // Required, not blank
  dataType: PropertyDataType;    // Required
  isRequired: boolean;           // Required, default: false
  isSearchable: boolean;         // Required, default: true
  category: PropertyCategory;    // Required
  displayOrder?: number;         // Optional
}
```

**Response** (201 Created): PropertyAttributeResponse

**Example**:
```bash
curl -X POST http://localhost:8080/api/property-attributes \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Number of Bedrooms",
    "dataType": "NUMBER",
    "isRequired": true,
    "isSearchable": true,
    "category": "STRUCTURE",
    "displayOrder": 1
  }'
```

---

### 6. Update Attribute

**Endpoint**: `PUT /api/property-attributes/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Request Body**: Same as Create Attribute

**Response** (200 OK): PropertyAttributeResponse

---

### 7. Delete Attribute

**Endpoint**: `DELETE /api/property-attributes/{id}`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Response** (200 OK):
```typescript
{
  message: string;    // "Property attribute deleted successfully"
}
```

---

### Attribute Options Endpoints

### 8. Add Attribute Option

**Endpoint**: `POST /api/property-attributes/{id}/options`  
**Authentication**: Required  
**Roles**: `ADMIN` only  
**Description**: Add an option for SINGLE_SELECT or MULTI_SELECT attributes

**Request Body**:
```typescript
{
  optionValue: string;      // Required, not blank
  displayOrder?: number;    // Optional
}
```

**Response** (201 Created):
```typescript
{
  id: number;
  attributeId: number;
  optionValue: string;
  displayOrder: number;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/property-attributes/5/options \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "optionValue": "Hardwood",
    "displayOrder": 1
  }'
```

---

### 9. Get Attribute Options

**Endpoint**: `GET /api/property-attributes/{id}/options`  
**Authentication**: Required  
**Roles**: `ADMIN`, `BROKER`, `AGENT`, `ASSISTANT`

**Response** (200 OK): Array of AttributeOptionResponse

---

### 10. Delete Attribute Option

**Endpoint**: `DELETE /api/property-attributes/options/{optionId}`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Response** (200 OK):
```typescript
{
  message: string;    // "Attribute option deleted successfully"
}
```

---

### 11. Reorder Attributes

**Endpoint**: `PUT /api/property-attributes/category/{category}/reorder`  
**Authentication**: Required  
**Roles**: `ADMIN` only

**Request Body**:
```typescript
{
  attributeIds: number[];    // Required, ordered list of attribute IDs
}
```

**Response** (200 OK):
```typescript
{
  message: string;    // "Attributes reordered successfully"
}
```

**Example**:
```bash
curl -X PUT http://localhost:8080/api/property-attributes/category/STRUCTURE/reorder \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{"attributeIds": [5, 3, 1, 2, 4]}'
```

---

## Saved Search Endpoints

Base Paths: `/api/saved-searches` and `/api/customers/{customerId}/saved-searches`

### 1. Get All Saved Searches for Agent

**Endpoint**: `GET /api/saved-searches`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get all saved searches for the current agent's customers

**Response** (200 OK):
```typescript
Array<{
  id: number;
  customerId: number;
  customerName: string;
  agentId: number;
  agentName: string;
  name: string;
  description: string | null;
  filters: Array<SearchFilterDTO>;
  createdDate: string;
  updatedDate: string;
}>
```

---

### 2. Get Saved Searches for Customer

**Endpoint**: `GET /api/customers/{customerId}/saved-searches`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Get all saved searches for a specific customer

**Response** (200 OK): Array of SavedSearchResponse

---

### 3. Get Saved Search by ID

**Endpoint**: `GET /api/saved-searches/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK): SavedSearchResponse

---

### 4. Create Saved Search

**Endpoint**: `POST /api/customers/{customerId}/saved-searches`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Request Body**:
```typescript
{
  name: string;              // Required, max 100 characters
  description?: string;      // Optional, max 500 characters
  filters: Array<{           // Required, at least one filter
    attributeId: number;           // Required
    dataType: PropertyDataType;    // Required
    
    // For NUMBER type:
    minValue?: number;
    maxValue?: number;
    
    // For DATE type:
    minDate?: string;              // ISO 8601 date
    maxDate?: string;              // ISO 8601 date
    
    // For SINGLE_SELECT/MULTI_SELECT:
    selectedValues?: string[];
    
    // For TEXT type:
    textValue?: string;
    
    // For BOOLEAN type:
    booleanValue?: boolean;
  }>;
}
```

**Response** (201 Created):
```typescript
{
  message: string;
  data: SavedSearchResponse;
}
```

**Example**:
```bash
curl -X POST http://localhost:8080/api/customers/1/saved-searches \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "3BR Homes with Pool",
    "description": "Customer preference for family home",
    "filters": [
      {
        "attributeId": 1,
        "dataType": "NUMBER",
        "minValue": 3,
        "maxValue": 3
      },
      {
        "attributeId": 5,
        "dataType": "BOOLEAN",
        "booleanValue": true
      }
    ]
  }'
```

---

### 5. Update Saved Search

**Endpoint**: `PUT /api/saved-searches/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Request Body**: Same as Create Saved Search

**Response** (200 OK):
```typescript
{
  message: string;
  data: SavedSearchResponse;
}
```

---

### 6. Delete Saved Search

**Endpoint**: `DELETE /api/saved-searches/{id}`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`

**Response** (200 OK):
```typescript
{
  message: string;    // "Saved search deleted successfully"
}
```

---

### 7. Execute Saved Search

**Endpoint**: `GET /api/saved-searches/{id}/execute`  
**Authentication**: Required  
**Roles**: `AGENT`, `BROKER`, `ADMIN`  
**Description**: Execute a saved search and get matching properties

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort criteria (default: "createdDate,desc")

**Response** (200 OK): Paginated PropertyResponse

**Example**:
```bash
curl -X GET "http://localhost:8080/api/saved-searches/5/execute?page=0&size=20" \
  -H "Authorization: Bearer your-jwt-token"
```

---

## TypeScript Type Definitions

Here are complete TypeScript type definitions for all DTOs:

```typescript
// ============================================
// ENUMS
// ============================================

enum Role {
  ADMIN = "ADMIN",
  BROKER = "BROKER",
  AGENT = "AGENT",
  ASSISTANT = "ASSISTANT"
}

enum UserStatus {
  ACTIVE = "ACTIVE",
  INACTIVE = "INACTIVE"
}

enum PropertyStatus {
  ACTIVE = "ACTIVE",
  PENDING = "PENDING",
  SOLD = "SOLD",
  WITHDRAWN = "WITHDRAWN"
}

enum CustomerStatus {
  LEAD = "LEAD",
  ACTIVE = "ACTIVE",
  CLOSED = "CLOSED",
  INACTIVE = "INACTIVE"
}

enum PropertyDataType {
  TEXT = "TEXT",
  NUMBER = "NUMBER",
  BOOLEAN = "BOOLEAN",
  DATE = "DATE",
  SINGLE_SELECT = "SINGLE_SELECT",
  MULTI_SELECT = "MULTI_SELECT"
}

enum PropertyCategory {
  BASIC = "BASIC",
  STRUCTURE = "STRUCTURE",
  FEATURES = "FEATURES",
  LOCATION = "LOCATION",
  FINANCIAL = "FINANCIAL"
}

enum InteractionType {
  PHONE_CALL = "PHONE_CALL",
  EMAIL = "EMAIL",
  MEETING = "MEETING",
  PROPERTY_VIEWING = "PROPERTY_VIEWING",
  SMS = "SMS",
  VIDEO_CALL = "VIDEO_CALL",
  OTHER = "OTHER"
}

// ============================================
// AUTH DTOs
// ============================================

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  refreshToken: string;
  user: UserInfo;
  expiresIn: number;
}

interface RefreshTokenRequest {
  refreshToken: string;
}

interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
}

interface UserInfo {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  status: string;
  createdDate: string;
  updatedDate: string;
}

// ============================================
// PROPERTY DTOs
// ============================================

interface CreatePropertyRequest {
  title: string;
  description?: string;
  price: number;
}

interface UpdatePropertyRequest {
  title: string;
  description?: string;
  price: number;
  status: PropertyStatus;
}

interface PropertyResponse {
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: PropertyStatus;
  createdDate: string;
  updatedDate: string;
  attributeValues?: AttributeValueResponse[] | null;
}

interface SetAttributeValueRequest {
  attributeId: number;
  value: any;
}

interface AttributeValueResponse {
  id: number;
  propertyId: number;
  attributeId: number;
  attributeName: string;
  dataType: string;
  value: any;
}

interface SharePropertyRequest {
  sharedWithUserId: number;
}

interface PropertySharingResponse {
  id: number;
  propertyId: number;
  sharedWithUserId: number;
  sharedWithUserName: string;
  sharedByUserId: number;
  sharedByUserName: string;
  createdDate: string;
}

// ============================================
// CUSTOMER DTOs
// ============================================

interface CreateCustomerRequest {
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  budgetMin?: number;
  budgetMax?: number;
  notes?: string;
  leadSource?: string;
}

interface UpdateCustomerRequest {
  firstName: string;
  lastName: string;
  phone: string;
  email?: string;
  budgetMin?: number;
  budgetMax?: number;
  notes?: string;
  leadSource?: string;
  status: CustomerStatus;
}

interface CustomerResponse {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  budgetMin: number | null;
  budgetMax: number | null;
  notes: string | null;
  leadSource: string | null;
  status: CustomerStatus;
  agentId: number;
  agentName: string;
  createdDate: string;
  updatedDate: string;
}

interface CreateCustomerNoteRequest {
  content: string;
}

interface CustomerNoteResponse {
  id: number;
  customerId: number;
  customerName: string;
  createdByUserId: number;
  createdByUserName: string;
  content: string;
  createdDate: string;
}

interface CreateCustomerInteractionRequest {
  type: InteractionType;
  subject: string;
  notes?: string;
  interactionDate: string;
  durationMinutes?: number;
  relatedPropertyId?: number;
}

interface CustomerInteractionResponse {
  id: number;
  customerId: number;
  customerName: string;
  userId: number;
  userName: string;
  type: InteractionType;
  subject: string;
  notes: string | null;
  interactionDate: string;
  durationMinutes: number | null;
  relatedPropertyId: number | null;
  relatedPropertyTitle: string | null;
  createdDate: string;
}

// ============================================
// USER DTOs
// ============================================

interface CreateUserRequest {
  username: string;
  password: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: Role;
}

interface UpdateUserRequest {
  firstName?: string;
  lastName?: string;
  email: string;
  role: Role;
  status: UserStatus;
}

interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: Role;
  status: UserStatus;
  createdDate: string;
  updatedDate: string;
}

interface HierarchyRequest {
  supervisorId: number;
}

// ============================================
// PROPERTY ATTRIBUTE DTOs
// ============================================

interface CreateAttributeRequest {
  name: string;
  dataType: PropertyDataType;
  isRequired: boolean;
  isSearchable: boolean;
  category: PropertyCategory;
  displayOrder?: number;
}

interface PropertyAttributeResponse {
  id: number;
  name: string;
  dataType: string;
  isRequired: boolean;
  isSearchable: boolean;
  category: string;
  displayOrder: number;
  createdDate: string;
  updatedDate: string;
  options: AttributeOptionResponse[];
}

interface CreateAttributeOptionRequest {
  optionValue: string;
  displayOrder?: number;
}

interface AttributeOptionResponse {
  id: number;
  attributeId: number;
  optionValue: string;
  displayOrder: number;
}

interface ReorderAttributesRequest {
  attributeIds: number[];
}

// ============================================
// SAVED SEARCH DTOs
// ============================================

interface SavedSearchRequest {
  name: string;
  description?: string;
  filters: SearchFilterDTO[];
}

interface SavedSearchResponse {
  id: number;
  customerId: number;
  customerName: string;
  agentId: number;
  agentName: string;
  name: string;
  description: string;
  filters: SearchFilterDTO[];
  createdDate: string;
  updatedDate: string;
}

interface PropertySearchCriteriaRequest {
  filters: SearchFilterDTO[];
  page?: number;
  size?: number;
  sort?: string;
}

interface SearchFilterDTO {
  attributeId: number;
  dataType: PropertyDataType;
  
  // For NUMBER type
  minValue?: number;
  maxValue?: number;
  
  // For DATE type
  minDate?: string;
  maxDate?: string;
  
  // For SINGLE_SELECT/MULTI_SELECT
  selectedValues?: string[];
  
  // For TEXT type
  textValue?: string;
  
  // For BOOLEAN type
  booleanValue?: boolean;
}

// ============================================
// COMMON DTOs
// ============================================

interface MessageResponse {
  message: string;
}

interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
  validationErrors?: Record<string, string>;
}

// ============================================
// PAGINATION
// ============================================

interface Page<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
}
```

---

## Quick Start Example

Here's a complete example of a typical frontend integration workflow:

```typescript
class RealEstateCRMClient {
  private baseURL = 'http://localhost:8080/api';
  private accessToken: string | null = null;
  private refreshToken: string | null = null;

  // Login
  async login(username: string, password: string): Promise<LoginResponse> {
    const response = await fetch(`${this.baseURL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    
    if (!response.ok) {
      throw await this.handleError(response);
    }
    
    const data: LoginResponse = await response.json();
    this.accessToken = data.token;
    this.refreshToken = data.refreshToken;
    
    return data;
  }

  // Refresh access token
  async refreshAccessToken(): Promise<void> {
    if (!this.refreshToken) {
      throw new Error('No refresh token available');
    }

    const response = await fetch(`${this.baseURL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: this.refreshToken })
    });
    
    if (!response.ok) {
      throw await this.handleError(response);
    }
    
    const data: RefreshTokenResponse = await response.json();
    this.accessToken = data.accessToken;
    this.refreshToken = data.refreshToken;
  }

  // Get properties with automatic token refresh
  async getProperties(page = 0, size = 20): Promise<Page<PropertyResponse>> {
    return this.authenticatedRequest(`/properties?page=${page}&size=${size}`);
  }

  // Create property
  async createProperty(request: CreatePropertyRequest): Promise<PropertyResponse> {
    return this.authenticatedRequest('/properties', {
      method: 'POST',
      body: JSON.stringify(request)
    });
  }

  // Generic authenticated request with auto-refresh
  private async authenticatedRequest(
    path: string,
    options: RequestInit = {}
  ): Promise<any> {
    const response = await fetch(`${this.baseURL}${path}`, {
      ...options,
      headers: {
        'Authorization': `Bearer ${this.accessToken}`,
        'Content-Type': 'application/json',
        ...options.headers
      }
    });

    // If token expired, refresh and retry
    if (response.status === 401) {
      await this.refreshAccessToken();
      return this.authenticatedRequest(path, options);
    }

    if (!response.ok) {
      throw await this.handleError(response);
    }

    return response.json();
  }

  // Error handling
  private async handleError(response: Response): Promise<Error> {
    const errorData: ErrorResponse = await response.json();
    return new Error(errorData.message);
  }
}

// Usage
const client = new RealEstateCRMClient();

// Login
const loginResponse = await client.login('agent1', 'password123');
console.log('Logged in as:', loginResponse.user.username);

// Get properties
const properties = await client.getProperties(0, 20);
console.log('Total properties:', properties.totalElements);

// Create property
const newProperty = await client.createProperty({
  title: 'Beautiful Home',
  description: '3BR/2BA',
  price: 450000
});
console.log('Created property:', newProperty.id);
```

---

## Testing with Swagger UI

The API includes built-in Swagger UI documentation for interactive testing:

**Swagger UI URL**: `http://localhost:8080/swagger-ui/index.html`

This interface allows you to:
- Browse all endpoints
- Test endpoints directly from your browser
- View request/response schemas
- Authenticate and test protected endpoints

---

## Additional Notes

### Date/Time Formats
All date/time fields use **ISO 8601 format**:
- DateTime: `2025-11-19T10:30:00`
- Date: `2025-11-19`

### Decimal Numbers
Price and budget values are represented as `BigDecimal` in the backend. In JSON:
- Use numbers without quotes: `"price": 450000`
- Supports decimals: `"price": 450000.50`

### Pagination
Default pagination:
- Default page size: 20
- Max page size: 100
- Page numbers start at 0

### Role Hierarchy
From highest to lowest privilege:
1. **ADMIN** - Full access
2. **BROKER** - Manage agents and their resources
3. **AGENT** - Manage own properties and customers
4. **ASSISTANT** - Read-only access

---

## Support

For questions or issues:
- Check the Swagger UI at `http://localhost:8080/swagger-ui/index.html`
- Review the error response for detailed validation messages
- Ensure JWT token is not expired (24-hour expiration)

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-19  
**API Version**: Spring Boot 3.2.5
