Property CRUD and Attributes — API Usage Guide

This document provides a practical, end‑to‑end guide to create, read, update, delete (CRUD) properties and to add/edit their dynamic attributes in the Real Estate CRM backend.

Last updated: 2025-08-21

---

1) Authentication and Roles

- All endpoints below require authentication with roles: AGENT, BROKER, or ADMIN, unless otherwise noted.
- ADMIN is additionally required to define the attribute catalog (see Attribute Catalog section below).
- Security is JWT-based in production; in dev profile, HTTP Basic may also be enabled. See README and Swagger UI for details.

---

2) Property CRUD Endpoints

Base path: /api/properties

- List properties (paged)
  - GET /api/properties
  - Query params (optional):
    - status: PropertyStatus (ACTIVE, etc.)
    - minPrice: number
    - maxPrice: number
    - Pageable params (page, size, sort) are supported by Spring
  - Behavior:
    - ADMIN: sees all properties
    - Non-admin: sees properties owned by them and their subordinates
  - Response: Page<PropertyResponse>

- Get one property (with attributes)
  - GET /api/properties/{id}
  - Response: PropertyResponse, including attributeValues array

- Create property
  - POST /api/properties
  - Body (CreatePropertyRequest):
    {
      "title": "string",          // required
      "description": "string?",
      "price": 123456.78           // required, positive number
    }
  - Behavior: sets status=ACTIVE and assigns current user as agent
  - Response: 201 Created with PropertyResponse

- Update property
  - PUT /api/properties/{id}
  - Body (UpdatePropertyRequest):
    {
      "title": "string",          // required
      "description": "string?",
      "price": 123456.78,          // required, positive
      "status": "ACTIVE"          // required (enum PropertyStatus)
    }
  - Response: 200 OK with PropertyResponse

- Delete property
  - DELETE /api/properties/{id}
  - Response: { message: "Property deleted successfully" }

- Search (simple example)
  - GET /api/properties/search?minPrice=100000&maxPrice=300000&status=ACTIVE
  - Response: PropertyResponse[]

Validation notes
- Price must be positive. Backend enforces this in PropertyService.validateProperty.
- On update, status must be a valid PropertyStatus.

---

3) Attribute Catalog (Admin)

Base path: /api/property-attributes

- List attributes
  - GET /api/property-attributes
  - GET /api/property-attributes/searchable (subset used to build filter UI)
  - GET /api/property-attributes/category/{category}
  - GET /api/property-attributes/{id}

- Create/update attribute (ADMIN)
  - POST /api/property-attributes
    {
      "name": "string",
      "dataType": "TEXT" | "NUMBER" | "BOOLEAN" | "DATE" | "SINGLE_SELECT" | "MULTI_SELECT",
      "isRequired": true|false,
      "isSearchable": true|false,
      "category": "BASICS" | ...,   // see PropertyCategory enum
      "displayOrder": 10?
    }
  - PUT /api/property-attributes/{id} — same payload as create
  - DELETE /api/property-attributes/{id}

- Manage options for select types (ADMIN)
  - POST /api/property-attributes/{id}/options
    { "optionValue": "Gas", "displayOrder": 1 }
  - GET /api/property-attributes/{id}/options
  - DELETE /api/property-attributes/options/{optionId}

See docs/property-attributes.md for deeper frontend guidance.

---

4) Set, Edit, List, and Delete Attribute Values on a Property

Base path: /api/properties/{propertyId}/values

- Add or update a value
  - POST /api/properties/{propertyId}/values
  - Body (SetAttributeValueRequest):
    {
      "attributeId": 12,           // required
      "value": any                 // type depends on attribute dataType
    }
  - Behavior:
    - If value for (propertyId, attributeId) exists, it is updated; otherwise created.
    - Typed persistence:
      - TEXT or SINGLE_SELECT: value is a string
      - NUMBER: value is a JSON number (will be BigDecimal on server)
      - BOOLEAN: value is a boolean
      - MULTI_SELECT: value is a JSON string representing string[], e.g. "[\"Pool\",\"Gym\"]"
      - DATE: currently not set through the setter switch; coordinate before using

- Read all values for a property
  - GET /api/properties/{propertyId}/values
  - Returns AttributeValueResponse[] with fields: id, propertyId, attributeId, attributeName, dataType, value

- Delete a value
  - DELETE /api/properties/{propertyId}/values/{attributeId}

Validation and errors
- If attribute.isRequired = true and you send null, request is rejected.
- Type mismatches lead to 400-level errors (e.g., NUMBER expects numeric JSON value).
- For SINGLE_SELECT/MULTI_SELECT, backend does not enforce membership in options list; validate on client for best UX.

---

5) Example Flows

A) Create a property and add attributes
1. Create property
   POST /api/properties
   {
     "title": "Modern Loft Downtown",
     "description": "Top-floor loft with skyline views",
     "price": 350000
   }
   => 201 with { id: 101, ... }

2. Set number of bedrooms (NUMBER)
   POST /api/properties/101/values
   { "attributeId": 7, "value": 2 }

3. Set heating type (SINGLE_SELECT)
   POST /api/properties/101/values
   { "attributeId": 12, "value": "Gas" }

4. Set amenities (MULTI_SELECT)
   POST /api/properties/101/values
   { "attributeId": 20, "value": "[\"Pool\",\"Gym\",\"Parking\"]" }

B) Edit attribute values later
- To change bedrooms from 2 to 3:
  POST /api/properties/101/values
  { "attributeId": 7, "value": 3 }

- To remove an attribute value entirely:
  DELETE /api/properties/101/values/7

C) Update core property fields
- PUT /api/properties/101
  {
    "title": "Modern Loft Downtown (renovated)",
    "description": "Updated kitchen and bath",
    "price": 365000,
    "status": "ACTIVE"
  }

---

6) Response Shapes

- PropertyResponse
  {
    id, title, description, price,
    agentId, agentName,
    status,
    createdDate, updatedDate,
    attributeValues?: [
      { id, propertyId, attributeId, attributeName, dataType, value }
    ]
  }

- AttributeValueResponse
  { id, propertyId, attributeId, attributeName, dataType, value }

---

7) Tips and Gotchas

- Always fetch attribute catalog (and options) to render proper UI and validation.
- MULTI_SELECT values are stored and returned as JSON strings; convert to/from string[] on the client.
- DATE type exists in the model, but PropertyService.setAttributeValue currently doesn’t set dateValue; avoid using until aligned.
- Non-admin users cannot create attributes; they can only set values on properties they own or have access to.
- Use GET /api/properties/{id} when you want property details plus embedded attribute values in one round trip.

If you need the backend to return MULTI_SELECT as arrays instead of JSON strings, or to accept ISO-8601 dates for DATE type, open an issue to discuss API changes.
