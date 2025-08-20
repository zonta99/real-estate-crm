org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.realestatecrm.entity.PropertyAttribute.options: could not initialize proxy - no Session# Property Attributes — Frontend Integration Guide

This guide explains how dynamic Property Attributes work in the Real Estate CRM backend so you can build efficient, type‑safe UI forms, lists, and filters on the frontend.

Last updated: 2025-08-21

---

## 1) What are Property Attributes?

Property Attributes are dynamic fields attached to a Property. Admins can define attributes (e.g., "Bedrooms", "Furnished", "Heating Type") with a specific data type and optional select‑options. Agents then set per‑property values for those attributes.

Benefits:
- Schema‑less extensibility: add fields without redeploying.
- Typed values for validation and consistent rendering.
- Order and category for predictable UI grouping and layout.
- "Searchable" flag for building dynamic filters.

---

## 2) Core Domain Model

- PropertyAttribute
  - id: number
  - name: string (display name, unique)
  - dataType: enum PropertyDataType = TEXT | NUMBER | BOOLEAN | DATE | SINGLE_SELECT | MULTI_SELECT
  - isRequired: boolean (if true, value must be present on properties)
  - isSearchable: boolean (if true, useful to expose in filter UI)
  - category: enum PropertyCategory (grouping bucket; use to section your UI)
  - displayOrder: number (rendering order within a category)
  - options: list of PropertyAttributeOption (only for SINGLE_SELECT and MULTI_SELECT)

- PropertyAttributeOption
  - id: number
  - attributeId: number
  - optionValue: string (what backend stores/returns)
  - displayOrder: number (for option lists)

- AttributeValue (per property)
  - id, propertyId, attributeId
  - value stored in one of the typed columns, exposed to API as a single value field
  - For MULTI_SELECT, the backend stores a JSON string; it is exposed as the raw string value. See section 5 for payload shape.

---

## 3) Attribute Catalog APIs (Admin and readonly for agents)

Base path: /api/property-attributes

- GET /api/property-attributes
  - Returns all attributes ordered by category then displayOrder.
  - Response item (PropertyAttributeResponse):
    {
      id, name, dataType, isRequired, isSearchable, category, displayOrder,
      createdDate, updatedDate,
      options: [{ id, attributeId, optionValue, displayOrder }] | null
    }

- GET /api/property-attributes/searchable
  - Returns only attributes where isSearchable = true (use for building filter UI).

- GET /api/property-attributes/category/{category}
  - Returns attributes for a specific category.

- GET /api/property-attributes/{id}
  - Returns a single attribute definition.

- POST /api/property-attributes (ADMIN)
  - Body (CreateAttributeRequest):
    {
      name: string,
      dataType: "TEXT" | "NUMBER" | "BOOLEAN" | "DATE" | "SINGLE_SELECT" | "MULTI_SELECT",
      isRequired: boolean,
      isSearchable: boolean,
      category: string,
      displayOrder?: number
    }

- POST /api/property-attributes/{id}/options (ADMIN)
  - For SINGLE_SELECT / MULTI_SELECT only.
  - Body (CreateAttributeOptionRequest):
    { optionValue: string, displayOrder?: number }

Notes
- The service enforces that options can only be created for select types.
- Data type changes on existing attributes are allowed but discouraged (existing values may become incompatible).

---

## 4) Setting and Reading Property Attribute Values

Base path: /api/properties

- Set/Update a value
  - POST /api/properties/{propertyId}/values
  - Body (SetAttributeValueRequest):
    { attributeId: number, value: any }
  - Type expectations:
    - TEXT: value is a string
    - NUMBER: value is a number sent as JSON number, deserialized to BigDecimal
    - BOOLEAN: value is a boolean
    - DATE: value (not currently set by service switch) is reserved; use TEXT ISO format if needed and align with backend before use
    - SINGLE_SELECT: value is a string; should match one of the defined optionValue items
    - MULTI_SELECT: value is a JSON string representing string[] (e.g., "[\"Gas\",\"Electric\"]")

- Get all values for a property
  - GET /api/properties/{propertyId}/values
  - Returns AttributeValueResponse[] where each item includes:
    {
      id, propertyId, attributeId, attributeName, dataType: string, value: any
    }

- Get property with embedded values
  - GET /api/properties/{id}
  - PropertyResponse includes attributeValues: AttributeValueResponse[]

- Delete a value
  - DELETE /api/properties/{propertyId}/values/{attributeId}

Validation
- If an attribute isRequired = true and you send null, backend rejects.
- Type mismatch (e.g., NUMBER but value is not a number) raises 400 with validation error.

---

## 5) Payload Examples

1) SINGLE_SELECT (e.g., Heating Type)
- Catalog: GET /api/property-attributes => find attribute { id: 12, dataType: "SINGLE_SELECT", options: [ { optionValue: "Gas" }, { optionValue: "Electric" } ] }
- Set value:
  POST /api/properties/101/values
  {
    "attributeId": 12,
    "value": "Gas"
  }
- Read value:
  GET /api/properties/101/values =>
  [
    {
      "attributeId": 12,
      "attributeName": "Heating Type",
      "dataType": "SINGLE_SELECT",
      "value": "Gas"
    }
  ]

2) MULTI_SELECT (e.g., Amenities)
- Note: Send value as a JSON string of string array.
  POST /api/properties/101/values
  {
    "attributeId": 20,
    "value": "[\"Pool\",\"Gym\",\"Parking\"]"
  }
- On read, you will get the same JSON string back in value. Parse it on the client to string[].

3) NUMBER (e.g., Bedrooms)
  POST /api/properties/101/values
  {
    "attributeId": 7,
    "value": 3
  }

4) BOOLEAN (e.g., Furnished)
  POST /api/properties/101/values
  {
    "attributeId": 9,
    "value": true
  }

5) TEXT (e.g., View)
  POST /api/properties/101/values
  {
    "attributeId": 15,
    "value": "Sea View"
  }

---

## 6) Frontend Rendering & UX Recommendations

- Fetch catalog once and cache
  - On app start or when entering property screens, call GET /api/property-attributes (or by category) and cache the list.
  - Use displayOrder to render fields consistently.

- Group by category
  - The category field is suitable for UI sections (e.g., Basics, Interior, Exterior, Neighborhood).

- Render by dataType
  - TEXT: text input
  - NUMBER: numeric input
  - BOOLEAN: switch/checkbox
  - SINGLE_SELECT: dropdown/select using options
  - MULTI_SELECT: chips or multi-select; remember backend expects a JSON string; convert string[] <-> JSON string on submit/read
  - DATE: confirm the shape with backend before enabling (currently stored as dateValue in entity, but service setter switch does not handle it yet)

- Respect isRequired and isSearchable
  - Mark required fields in forms and validate before submit.
  - Build dynamic filter UI from GET /api/property-attributes/searchable; render filter widgets by dataType.

- Option labels and i18n
  - optionValue is both the stored value and what you get back. If you need localized labels, map them client‑side (value -> label) or request backend extension for labels.

- Editing vs viewing
  - For viewing a property, you can either:
    - GET /api/properties/{id} to get property plus attributeValues array; or
    - GET /api/properties/{id}/values just for values and join with cached catalog.

- Error handling
  - Backend returns clear errors for type mismatches and required fields. Surface messages near field.

---

## 7) Known Constraints and Edge Cases

- MULTI_SELECT is returned as a JSON string, not parsed array. Frontend must parse into string[].
- SINGLE_SELECT/MULTI_SELECT do not currently enforce that provided values match defined options; validate client‑side to improve UX.
- Changing an attribute’s data type after values exist can make existing values incompatible.
- Deleting an attribute that’s in use is blocked.

---

## 8) Quick Checklist for Frontend

- [ ] Load catalog once: GET /api/property-attributes
- [ ] Group by category and sort by displayOrder
- [ ] Render fields by dataType
- [ ] Enforce isRequired in forms
- [ ] Convert multi-select arrays to JSON string on submit; parse on read
- [ ] Use /searchable to build filters
- [ ] Cache options for select fields
- [ ] Handle 400s from validation gracefully

---

If you need any API shape adjustments (e.g., return MULTI_SELECT as array), open an issue or PR. This guide reflects the current backend behavior found in the source (PropertyAttribute, AttributeValue, PropertyService, PropertyAttributeController).
