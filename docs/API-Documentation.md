# Real Estate CRM - API Documentation for Frontend

**Base URL**: `http://localhost:8080`
**Version**: 1.0
**Authentication**: JWT Bearer Token

---

## Table of Contents

1. [Authentication](#authentication)
2. [Users](#users)
3. [Properties](#properties)
4. [Customers](#customers)
5. [Property Attributes](#property-attributes)
6. [Error Handling](#error-handling)
7. [Common Patterns](#common-patterns)

---

## Authentication

### Login

Authenticate a user and receive access and refresh tokens.

**Endpoint**: `POST /api/auth/login`
**Authentication**: None (public)

**Request Body**:
```json
{
  "username": "admin",
  "password": "YourPassword123!"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlkIjoxLCJlbWFpbCI6ImFkbWluQHJlYWxlc3RhdGVjcm0uY29tIiwiaWF0IjoxNzA1NTAwMDAwLCJleHAiOjE3MDU1ODY0MDB9.xyz",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "user": {
    "id": "1",
    "username": "admin",
    "email": "admin@realestatecrm.com",
    "firstName": "System",
    "lastName": "Administrator",
    "roles": ["ROLE_ADMIN"],
    "status": "ACTIVE",
    "createdDate": "2025-01-15T10:30:00",
    "updatedDate": "2025-01-15T10:30:00"
  },
  "expiresIn": 86400
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"YourPassword123!"}'
```

**TypeScript Example**:
```typescript
interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    roles: string[];
    status: string;
    createdDate: string;
    updatedDate: string;
  };
  expiresIn: number;
}

async function login(credentials: LoginRequest): Promise<LoginResponse> {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(credentials)
  });

  if (!response.ok) {
    throw new Error('Login failed');
  }

  const data: LoginResponse = await response.json();

  // Store tokens
  sessionStorage.setItem('accessToken', data.token);
  sessionStorage.setItem('refreshToken', data.refreshToken);
  sessionStorage.setItem('user', JSON.stringify(data.user));

  return data;
}
```

---

### Refresh Token

Get a new access token using a refresh token.

**Endpoint**: `POST /api/auth/refresh`
**Authentication**: None (uses refresh token)

**Request Body**:
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
  "expiresAt": "2025-01-16T10:30:00"
}
```

**TypeScript Example**:
```typescript
async function refreshAccessToken(): Promise<string> {
  const refreshToken = sessionStorage.getItem('refreshToken');

  const response = await fetch('http://localhost:8080/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  });

  const data = await response.json();

  // Update tokens
  sessionStorage.setItem('accessToken', data.token);
  sessionStorage.setItem('refreshToken', data.refreshToken);

  return data.token;
}
```

---

### Get Current User

Get information about the currently authenticated user.

**Endpoint**: `GET /api/auth/user`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "id": "1",
  "username": "admin",
  "email": "admin@realestatecrm.com",
  "firstName": "System",
  "lastName": "Administrator",
  "roles": ["ROLE_ADMIN"],
  "status": "ACTIVE",
  "createdDate": "2025-01-15T10:30:00",
  "updatedDate": "2025-01-15T10:30:00"
}
```

---

### Get User Permissions

Get permissions for the current user.

**Endpoint**: `GET /api/auth/permissions`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "permissions": [
    "VIEW_PROPERTIES",
    "CREATE_PROPERTIES",
    "EDIT_PROPERTIES",
    "DELETE_PROPERTIES",
    "VIEW_CUSTOMERS",
    "MANAGE_USERS"
  ]
}
```

---

## Users

### List All Users

Get a paginated list of users.

**Endpoint**: `GET /api/users`
**Authentication**: Required (ADMIN, BROKER)

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (e.g., `username,asc`)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "username": "admin",
      "email": "admin@realestatecrm.com",
      "firstName": "System",
      "lastName": "Administrator",
      "role": "ADMIN",
      "status": "ACTIVE",
      "createdDate": "2025-01-15T10:30:00",
      "updatedDate": "2025-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

**cURL Example**:
```bash
curl -X GET "http://localhost:8080/api/users?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### Get User by ID

Get details of a specific user.

**Endpoint**: `GET /api/users/{id}`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "id": 1,
  "username": "alice",
  "email": "alice@realestatecrm.com",
  "firstName": "Alice",
  "lastName": "Agent",
  "role": "AGENT",
  "status": "ACTIVE",
  "createdDate": "2025-01-15T10:30:00",
  "updatedDate": "2025-01-15T10:30:00"
}
```

---

### Create User

Create a new user.

**Endpoint**: `POST /api/users`
**Authentication**: Required (ADMIN)

**Request Body**:
```json
{
  "username": "john.agent",
  "password": "SecurePass123!@#",
  "email": "john@realestatecrm.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "AGENT"
}
```

**Validation Rules**:
- `username`: 3-20 characters, required
- `password`: Min 8 characters, must contain uppercase, lowercase, digit, and special character
- `email`: Valid email format, required
- `role`: One of: ADMIN, BROKER, AGENT, ASSISTANT

**Response** (201 Created):
```json
{
  "id": 5,
  "username": "john.agent",
  "email": "john@realestatecrm.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "AGENT",
  "status": "ACTIVE",
  "createdDate": "2025-01-15T12:00:00",
  "updatedDate": "2025-01-15T12:00:00"
}
```

**TypeScript Example**:
```typescript
interface CreateUserRequest {
  username: string;
  password: string;
  email: string;
  firstName?: string;
  lastName?: string;
  role: 'ADMIN' | 'BROKER' | 'AGENT' | 'ASSISTANT';
}

async function createUser(user: CreateUserRequest) {
  const token = sessionStorage.getItem('accessToken');

  const response = await fetch('http://localhost:8080/api/users', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(user)
  });

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }

  return await response.json();
}
```

---

### Update User

Update an existing user.

**Endpoint**: `PUT /api/users/{id}`
**Authentication**: Required (ADMIN or self)

**Request Body**:
```json
{
  "email": "john.updated@realestatecrm.com",
  "firstName": "John",
  "lastName": "Smith",
  "status": "ACTIVE"
}
```

**Response** (200 OK):
```json
{
  "id": 5,
  "username": "john.agent",
  "email": "john.updated@realestatecrm.com",
  "firstName": "John",
  "lastName": "Smith",
  "role": "AGENT",
  "status": "ACTIVE",
  "createdDate": "2025-01-15T12:00:00",
  "updatedDate": "2025-01-15T14:30:00"
}
```

---

### Delete User

Delete a user.

**Endpoint**: `DELETE /api/users/{id}`
**Authentication**: Required (ADMIN)

**Response** (200 OK):
```json
{
  "message": "User deleted successfully"
}
```

---

## Properties

### List All Properties

Get a paginated list of properties.

**Endpoint**: `GET /api/properties`
**Authentication**: Required (AGENT, BROKER, ADMIN)

**Query Parameters**:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `status` (optional): Filter by status (ACTIVE, PENDING, SOLD, etc.)
- `minPrice` (optional): Minimum price filter
- `maxPrice` (optional): Maximum price filter

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "title": "Cozy Family Home",
      "description": "3-bed, 2-bath cozy home near parks and schools.",
      "price": 350000.00,
      "agentId": 2,
      "agentName": "Alice Agent",
      "status": "ACTIVE",
      "createdDate": "2025-01-10T09:00:00",
      "updatedDate": "2025-01-15T10:30:00"
    },
    {
      "id": 2,
      "title": "Modern Downtown Condo",
      "description": "Stylish 2-bed condo with city views and amenities.",
      "price": 525000.00,
      "agentId": 3,
      "agentName": "Bob Broker",
      "status": "ACTIVE",
      "createdDate": "2025-01-12T11:00:00",
      "updatedDate": "2025-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true
}
```

**TypeScript Example**:
```typescript
interface Property {
  id: number;
  title: string;
  description: string;
  price: number;
  agentId: number;
  agentName: string;
  status: string;
  createdDate: string;
  updatedDate: string;
  attributeValues?: AttributeValue[];
}

interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
}

async function getProperties(
  page = 0,
  size = 20,
  filters?: { status?: string; minPrice?: number; maxPrice?: number }
): Promise<PageResponse<Property>> {
  const token = sessionStorage.getItem('accessToken');

  const params = new URLSearchParams({
    page: page.toString(),
    size: size.toString()
  });

  if (filters?.status) params.append('status', filters.status);
  if (filters?.minPrice) params.append('minPrice', filters.minPrice.toString());
  if (filters?.maxPrice) params.append('maxPrice', filters.maxPrice.toString());

  const response = await fetch(`http://localhost:8080/api/properties?${params}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  return await response.json();
}
```

---

### Get Property by ID

Get details of a specific property with all attributes.

**Endpoint**: `GET /api/properties/{id}`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "id": 1,
  "title": "Cozy Family Home",
  "description": "3-bed, 2-bath cozy home near parks and schools.",
  "price": 350000.00,
  "agentId": 2,
  "agentName": "Alice Agent",
  "status": "ACTIVE",
  "createdDate": "2025-01-10T09:00:00",
  "updatedDate": "2025-01-15T10:30:00",
  "attributeValues": [
    {
      "id": 1,
      "propertyId": 1,
      "attributeId": 3,
      "attributeName": "City",
      "dataType": "TEXT",
      "value": "Springfield"
    },
    {
      "id": 2,
      "propertyId": 1,
      "attributeId": 15,
      "attributeName": "Bedrooms",
      "dataType": "NUMBER",
      "value": 3
    },
    {
      "id": 3,
      "propertyId": 1,
      "attributeId": 45,
      "attributeName": "Has Garage",
      "dataType": "BOOLEAN",
      "value": true
    }
  ]
}
```

---

### Create Property

Create a new property listing.

**Endpoint**: `POST /api/properties`
**Authentication**: Required (AGENT, BROKER, ADMIN)

**Request Body**:
```json
{
  "title": "Beautiful Lake House",
  "description": "Stunning 4-bedroom lake house with private dock",
  "price": 750000.00,
  "agentId": 2,
  "status": "ACTIVE"
}
```

**Validation Rules**:
- `title`: Required, not blank
- `price`: Required, must be positive
- `agentId`: Required, must exist
- `status`: Optional, defaults to ACTIVE

**Response** (201 Created):
```json
{
  "id": 10,
  "title": "Beautiful Lake House",
  "description": "Stunning 4-bedroom lake house with private dock",
  "price": 750000.00,
  "agentId": 2,
  "agentName": "Alice Agent",
  "status": "ACTIVE",
  "createdDate": "2025-01-15T15:00:00",
  "updatedDate": "2025-01-15T15:00:00"
}
```

---

### Update Property

Update an existing property.

**Endpoint**: `PUT /api/properties/{id}`
**Authentication**: Required (Owner or ADMIN)

**Request Body**:
```json
{
  "title": "Beautiful Lake House - PRICE REDUCED",
  "description": "Stunning 4-bedroom lake house with private dock",
  "price": 695000.00,
  "status": "ACTIVE"
}
```

**Response** (200 OK):
```json
{
  "id": 10,
  "title": "Beautiful Lake House - PRICE REDUCED",
  "description": "Stunning 4-bedroom lake house with private dock",
  "price": 695000.00,
  "agentId": 2,
  "agentName": "Alice Agent",
  "status": "ACTIVE",
  "createdDate": "2025-01-15T15:00:00",
  "updatedDate": "2025-01-15T16:00:00"
}
```

---

### Delete Property

Delete a property listing.

**Endpoint**: `DELETE /api/properties/{id}`
**Authentication**: Required (Owner or ADMIN)

**Response** (200 OK):
```json
{
  "message": "Property deleted successfully"
}
```

---

### Set Property Attribute Value

Set or update a custom attribute value for a property.

**Endpoint**: `POST /api/properties/{id}/attributes`
**Authentication**: Required

**Request Body**:
```json
{
  "attributeId": 15,
  "value": 4
}
```

**Examples by Data Type**:
```json
// TEXT or SINGLE_SELECT
{
  "attributeId": 3,
  "value": "Springfield"
}

// NUMBER
{
  "attributeId": 15,
  "value": 4
}

// BOOLEAN
{
  "attributeId": 45,
  "value": true
}

// MULTI_SELECT (JSON string)
{
  "attributeId": 50,
  "value": "[\"Granite Counters\",\"Stainless Appliances\",\"Island\"]"
}
```

**Response** (200 OK):
```json
{
  "id": 15,
  "propertyId": 1,
  "attributeId": 15,
  "attributeName": "Bedrooms",
  "dataType": "NUMBER",
  "value": 4
}
```

---

### Search Properties

Search properties with advanced filters.

**Endpoint**: `POST /api/properties/search`
**Authentication**: Required

**Request Body**:
```json
{
  "minPrice": 300000,
  "maxPrice": 800000,
  "status": "ACTIVE",
  "city": "Springfield",
  "bedrooms": 3,
  "hasGarage": true
}
```

**Response** (200 OK):
```json
{
  "results": [
    {
      "id": 1,
      "title": "Cozy Family Home",
      "price": 350000.00,
      "city": "Springfield",
      "bedrooms": 3,
      "hasGarage": true
    }
  ],
  "total": 1
}
```

---

### Share Property

Share a property with another user.

**Endpoint**: `POST /api/properties/{id}/share`
**Authentication**: Required (Property owner)

**Request Body**:
```json
{
  "sharedWithUserId": 5
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "propertyId": 1,
  "sharedWithUserId": 5,
  "sharedWithUserName": "John Doe",
  "sharedByUserId": 2,
  "sharedByUserName": "Alice Agent",
  "sharedDate": "2025-01-15T17:00:00"
}
```

---

### Unshare Property

Remove property sharing.

**Endpoint**: `DELETE /api/properties/{propertyId}/share/{userId}`
**Authentication**: Required (Property owner)

**Response** (200 OK):
```json
{
  "message": "Property unshared successfully"
}
```

---

## Customers

### List All Customers

Get a paginated list of customers.

**Endpoint**: `GET /api/customers`
**Authentication**: Required

**Query Parameters**:
- `page` (optional): Page number
- `size` (optional): Page size
- `status` (optional): Filter by status (ACTIVE, INACTIVE, LEAD, etc.)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "firstName": "Jane",
      "lastName": "Smith",
      "email": "jane.smith@example.com",
      "phone": "+1-555-0123",
      "status": "LEAD",
      "assignedAgentId": 2,
      "assignedAgentName": "Alice Agent",
      "createdDate": "2025-01-10T09:00:00",
      "updatedDate": "2025-01-15T10:30:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### Create Customer

Create a new customer record.

**Endpoint**: `POST /api/customers`
**Authentication**: Required

**Request Body**:
```json
{
  "firstName": "Michael",
  "lastName": "Johnson",
  "email": "michael.j@example.com",
  "phone": "+1-555-9876",
  "status": "LEAD",
  "assignedAgentId": 2,
  "notes": "Looking for 3-bedroom in Springfield area"
}
```

**Response** (201 Created):
```json
{
  "id": 5,
  "firstName": "Michael",
  "lastName": "Johnson",
  "email": "michael.j@example.com",
  "phone": "+1-555-9876",
  "status": "LEAD",
  "assignedAgentId": 2,
  "assignedAgentName": "Alice Agent",
  "notes": "Looking for 3-bedroom in Springfield area",
  "createdDate": "2025-01-15T18:00:00",
  "updatedDate": "2025-01-15T18:00:00"
}
```

---

### Set Customer Search Criteria

Set search criteria for a customer.

**Endpoint**: `POST /api/customers/{id}/search-criteria`
**Authentication**: Required

**Request Body**:
```json
{
  "minPrice": 300000,
  "maxPrice": 500000,
  "minBedrooms": 3,
  "maxBedrooms": 4,
  "city": "Springfield",
  "propertyType": "Single Family Home",
  "mustHaveGarage": true
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "customerId": 5,
  "minPrice": 300000,
  "maxPrice": 500000,
  "minBedrooms": 3,
  "maxBedrooms": 4,
  "city": "Springfield",
  "propertyType": "Single Family Home",
  "mustHaveGarage": true
}
```

---

### Match Properties to Customer

Find properties matching customer's search criteria.

**Endpoint**: `GET /api/customers/{id}/matches`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "customerId": 5,
  "customerName": "Michael Johnson",
  "matches": [
    {
      "propertyId": 1,
      "title": "Cozy Family Home",
      "price": 350000,
      "bedrooms": 3,
      "city": "Springfield",
      "matchScore": 95
    }
  ],
  "totalMatches": 1
}
```

---

## Property Attributes

### List All Attributes

Get all available property attributes.

**Endpoint**: `GET /api/property-attributes`
**Authentication**: Required

**Query Parameters**:
- `category` (optional): Filter by category (BASIC, STRUCTURE, FEATURES, etc.)
- `searchable` (optional): Filter by searchable flag (true/false)

**Response** (200 OK):
```json
{
  "attributes": [
    {
      "id": 1,
      "name": "Address",
      "dataType": "TEXT",
      "category": "BASIC",
      "displayOrder": 1,
      "isRequired": true,
      "isSearchable": true,
      "options": []
    },
    {
      "id": 2,
      "name": "Property Type",
      "dataType": "SINGLE_SELECT",
      "category": "BASIC",
      "displayOrder": 2,
      "isRequired": true,
      "isSearchable": true,
      "options": [
        {
          "id": 1,
          "value": "Single Family Home",
          "displayOrder": 1
        },
        {
          "id": 2,
          "value": "Condo",
          "displayOrder": 2
        },
        {
          "id": 3,
          "value": "Townhouse",
          "displayOrder": 3
        }
      ]
    },
    {
      "id": 15,
      "name": "Bedrooms",
      "dataType": "NUMBER",
      "category": "STRUCTURE",
      "displayOrder": 1,
      "isRequired": false,
      "isSearchable": true,
      "options": []
    },
    {
      "id": 45,
      "name": "Has Garage",
      "dataType": "BOOLEAN",
      "category": "FEATURES",
      "displayOrder": 1,
      "isRequired": false,
      "isSearchable": true,
      "options": []
    }
  ]
}
```

**Data Types**:
- `TEXT`: Free text input
- `NUMBER`: Numeric value
- `BOOLEAN`: true/false
- `SINGLE_SELECT`: Single choice from options
- `MULTI_SELECT`: Multiple choices from options (stored as JSON array string)
- `DATE`: Date value (ISO 8601 format)

**Categories**:
- `BASIC`: Basic property information
- `STRUCTURE`: Structural details
- `FEATURES`: Property features
- `LOCATION`: Location details
- `FINANCIAL`: Financial information

---

### Get Attributes by Category

Get attributes filtered by category.

**Endpoint**: `GET /api/property-attributes/category/{category}`
**Authentication**: Required

**Response** (200 OK):
```json
{
  "category": "STRUCTURE",
  "attributes": [
    {
      "id": 15,
      "name": "Bedrooms",
      "dataType": "NUMBER",
      "displayOrder": 1
    },
    {
      "id": 16,
      "name": "Bathrooms",
      "dataType": "NUMBER",
      "displayOrder": 2
    }
  ]
}
```

---

## Error Handling

### Standard Error Response

All errors follow this format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users",
  "timestamp": "2025-01-15T20:00:00",
  "validationErrors": {
    "password": "Password must contain uppercase, lowercase, digit, and special character",
    "email": "Email must be valid"
  }
}
```

### HTTP Status Codes

- `200 OK`: Request succeeded
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: Authentication required or token invalid
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource already exists
- `500 Internal Server Error`: Server error

### Common Error Scenarios

#### Invalid Credentials
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password"
}
```

#### Token Expired
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired. Please sign in again."
}
```

#### Insufficient Permissions
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions to access this resource."
}
```

#### Validation Error
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "validationErrors": {
    "username": "Username must be between 3 and 20 characters",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## Common Patterns

### Authentication Flow

```typescript
// 1. Login
const loginResponse = await login({ username, password });

// 2. Store tokens
sessionStorage.setItem('accessToken', loginResponse.token);
sessionStorage.setItem('refreshToken', loginResponse.refreshToken);

// 3. Make authenticated requests
async function fetchWithAuth(url: string, options: RequestInit = {}) {
  const token = sessionStorage.getItem('accessToken');

  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${token}`
    }
  });

  // Handle token expiration
  if (response.status === 401) {
    const newToken = await refreshAccessToken();
    // Retry request with new token
    return fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${newToken}`
      }
    });
  }

  return response;
}
```

---

### Pagination Helper

```typescript
interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
}

function buildPaginationParams(params: PaginationParams): URLSearchParams {
  const searchParams = new URLSearchParams();

  if (params.page !== undefined) {
    searchParams.append('page', params.page.toString());
  }
  if (params.size !== undefined) {
    searchParams.append('size', params.size.toString());
  }
  if (params.sort) {
    searchParams.append('sort', params.sort);
  }

  return searchParams;
}

// Usage
const params = buildPaginationParams({ page: 0, size: 20, sort: 'title,asc' });
const url = `http://localhost:8080/api/properties?${params}`;
```

---

### Error Handling Helper

```typescript
interface ApiError {
  status: number;
  error: string;
  message: string;
  validationErrors?: Record<string, string>;
}

async function handleApiResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error: ApiError = await response.json();

    if (error.validationErrors) {
      // Handle validation errors
      const messages = Object.entries(error.validationErrors)
        .map(([field, message]) => `${field}: ${message}`)
        .join('\n');
      throw new Error(messages);
    }

    throw new Error(error.message || 'An error occurred');
  }

  return await response.json();
}

// Usage
try {
  const property = await handleApiResponse<Property>(response);
} catch (error) {
  console.error('Failed to fetch property:', error);
}
```

---

### Form Data for Property Attributes

```typescript
// Helper to handle different attribute data types
function formatAttributeValue(dataType: string, value: any): any {
  switch (dataType) {
    case 'TEXT':
    case 'SINGLE_SELECT':
      return String(value);

    case 'NUMBER':
      return Number(value);

    case 'BOOLEAN':
      return Boolean(value);

    case 'MULTI_SELECT':
      // Convert array to JSON string
      return JSON.stringify(Array.isArray(value) ? value : [value]);

    case 'DATE':
      // Convert to ISO 8601
      return value instanceof Date ? value.toISOString() : value;

    default:
      return value;
  }
}

// Set multiple attributes
async function setPropertyAttributes(
  propertyId: number,
  attributes: Array<{ attributeId: number; value: any; dataType: string }>
) {
  const token = sessionStorage.getItem('accessToken');

  for (const attr of attributes) {
    const formattedValue = formatAttributeValue(attr.dataType, attr.value);

    await fetch(`http://localhost:8080/api/properties/${propertyId}/attributes`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        attributeId: attr.attributeId,
        value: formattedValue
      })
    });
  }
}
```

---

## Complete React Example

```tsx
import React, { useState, useEffect } from 'react';

interface Property {
  id: number;
  title: string;
  description: string;
  price: number;
  agentName: string;
  status: string;
}

const PropertyList: React.FC = () => {
  const [properties, setProperties] = useState<Property[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProperties();
  }, []);

  const fetchProperties = async () => {
    try {
      const token = sessionStorage.getItem('accessToken');

      const response = await fetch('http://localhost:8080/api/properties', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to fetch properties');
      }

      const data = await response.json();
      setProperties(data.content);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h1>Properties</h1>
      {properties.map(property => (
        <div key={property.id} className="property-card">
          <h2>{property.title}</h2>
          <p>{property.description}</p>
          <p>Price: ${property.price.toLocaleString()}</p>
          <p>Agent: {property.agentName}</p>
          <p>Status: {property.status}</p>
        </div>
      ))}
    </div>
  );
};

export default PropertyList;
```

---

## Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html (interactive API docs)
- **Health Check**: http://localhost:8080/actuator/health
- **H2 Console** (dev only): http://localhost:8080/h2-console

For more details, see:
- [Property CRUD Guide](./property-crud.md)
- [Property Attributes Guide](./property-attributes.md)
- [LazyInitializationException Prevention](./LazyInitializationException-Prevention.md)
