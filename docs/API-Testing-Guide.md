# API Testing Guide

> **Quick and easy way to test all Real Estate CRM endpoints with mock data**

## Table of Contents

1. [Quick Start](#quick-start)
2. [Testing Tools](#testing-tools)
3. [Authentication Flow](#authentication-flow)
4. [Testing Workflow](#testing-workflow)
5. [Available Test Users](#available-test-users)
6. [Common Test Scenarios](#common-test-scenarios)
7. [Tips & Tricks](#tips--tricks)

---

## Quick Start

### Prerequisites

1. **Start the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Set environment variables** (if not already set):
   ```bash
   export ADMIN_PASSWORD="YourSecurePassword123!"
   ```

3. **Install a REST client**:
   - **VS Code**: Install "REST Client" extension by Huachao Mao
   - **IntelliJ IDEA**: Built-in HTTP Client (no installation needed)
   - **Alternative**: Use Postman, Insomnia, or curl

### First Test

1. Open `api-tests.http` in your IDE
2. Scroll to "Login as Agent (Alice)"
3. Click "Send Request" (VS Code) or the green arrow (IntelliJ)
4. You'll get a JWT token in the response
5. Now you can test other endpoints - the token auto-populates!

---

## Testing Tools

### Option 1: VS Code REST Client (Recommended)

**Installation**:
1. Open VS Code
2. Go to Extensions (Ctrl+Shift+X)
3. Search for "REST Client"
4. Install by Huachao Mao

**Usage**:
- Open `api-tests.http`
- Click "Send Request" above any request
- Responses appear in a new tab
- Variables like `{{token}}` auto-populate from previous responses

**Keyboard Shortcuts**:
- `Ctrl+Alt+R` / `Cmd+Alt+R`: Send request
- `Ctrl+Alt+E` / `Cmd+Alt+E`: Switch environment
- `Ctrl+Alt+H` / `Cmd+Alt+H`: View request history

### Option 2: IntelliJ IDEA HTTP Client

**Usage** (Built-in, no installation):
- Open `api-tests.http`
- Click the green arrow (▶) beside each request
- Responses appear in "Run" panel
- Variables auto-populate from `@name` responses

### Option 3: Postman

**Import Steps**:
1. Open Postman
2. Import → Raw text
3. Copy/paste sections from `api-tests.http`
4. Manually set {{variables}} or use Postman environments

---

## Authentication Flow

### Step 1: Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "password"
}
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 2,
    "username": "alice",
    "email": "alice@realestatecrm.com",
    "fullName": "Alice Anderson",
    "role": "AGENT"
  }
}
```

### Step 2: Use Token

Copy the token and use it in the `Authorization` header:

```http
GET http://localhost:8080/api/properties
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Step 3: Auto-Population (REST Client Only)

With REST Client, use named requests:

```http
# @name agentLogin
POST http://localhost:8080/api/auth/login
...

### Later requests automatically use the token
GET http://localhost:8080/api/properties
Authorization: Bearer {{agentLogin.response.body.token}}
```

---

## Available Test Users

The mock data includes these pre-configured users:

| Username | Password | Role | Use For Testing |
|----------|----------|------|-----------------|
| `admin` | *env var* | ADMIN | User management, property attributes |
| `alice` | `password` | AGENT | Property CRUD, customer management |
| `bob` | `password` | BROKER | Viewing all properties, managing agents |
| `carol` | `password` | AGENT | Property operations, property sharing |
| `david` | `password` | AGENT | Customer interactions, saved searches |
| `emma` | `password` | ASSISTANT | Read-only access (test permissions) |

**Security Note**: These passwords are for development only. Production passwords must be strong and unique.

---

## Testing Workflow

### Full Test Sequence

Follow this order to test the entire application:

#### 1. Authentication (5 tests)
```
✓ Login as Admin
✓ Login as Agent
✓ Get Current User Info
✓ Get Permissions
✓ Refresh Token
```

#### 2. Properties (15 tests)
```
✓ Get All Properties
✓ Get Property by ID
✓ Search Properties by Price
✓ Create Property
✓ Update Property
✓ Set Attribute Values
✓ Share Property
✓ Delete Property
```

#### 3. Customers (10 tests)
```
✓ Get All Customers
✓ Search Customers
✓ Create Customer
✓ Update Customer
✓ Add Interaction
✓ Add Note
```

#### 4. Saved Searches (5 tests)
```
✓ Create Saved Search
✓ Get Saved Searches
✓ Execute Search
✓ Update Search
✓ Delete Search
```

#### 5. Property Attributes - Admin (8 tests)
```
✓ Get All Attributes
✓ Create Text Attribute
✓ Create Number Attribute
✓ Create Select Attribute
✓ Update Attribute
✓ Reorder Attributes
```

#### 6. User Management - Admin (8 tests)
```
✓ Get All Users
✓ Search Users
✓ Create User
✓ Update User
✓ Change Status
✓ Delete User
```

---

## Common Test Scenarios

### Scenario 1: Agent Creates and Lists Property

```http
### 1. Login as Alice
# @name aliceLogin
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "alice",
  "password": "password"
}

### 2. Create Property
POST {{baseUrl}}/api/properties
Content-Type: application/json
Authorization: Bearer {{aliceLogin.response.body.token}}

{
  "title": "Sunset Villa",
  "description": "Beautiful villa with ocean views",
  "price": 850000,
  "status": "ACTIVE"
}

### 3. View All My Properties
GET {{baseUrl}}/api/properties
Authorization: Bearer {{aliceLogin.response.body.token}}
```

### Scenario 2: Customer Lead → Qualified → Sold

```http
### 1. Create Lead
POST {{baseUrl}}/api/customers
Authorization: Bearer {{aliceLogin.response.body.token}}
Content-Type: application/json

{
  "firstName": "Tom",
  "lastName": "Harris",
  "email": "tom.harris@example.com",
  "phone": "555-0200",
  "budgetMin": 300000,
  "budgetMax": 450000,
  "status": "LEAD",
  "leadSource": "Website"
}

### 2. Add Initial Contact Note
POST {{baseUrl}}/api/customers/1/interactions
Authorization: Bearer {{aliceLogin.response.body.token}}
Content-Type: application/json

{
  "interactionType": "CALL",
  "notes": "Initial contact. Very interested in family homes.",
  "interactionDate": "2025-11-19T09:00:00"
}

### 3. Qualify Lead
PATCH {{baseUrl}}/api/customers/1/status?status=QUALIFIED
Authorization: Bearer {{aliceLogin.response.body.token}}

### 4. Create Saved Search for Customer
POST {{baseUrl}}/api/customers/1/saved-searches
Authorization: Bearer {{aliceLogin.response.body.token}}
Content-Type: application/json

{
  "name": "Tom's Dream Home",
  "description": "3-4 bedroom family homes with garage",
  "filters": [
    {
      "attributeId": 8,
      "dataType": "NUMBER",
      "minValue": 3,
      "maxValue": 4
    }
  ]
}

### 5. Execute Search
GET {{baseUrl}}/api/saved-searches/1/execute
Authorization: Bearer {{aliceLogin.response.body.token}}

### 6. After showing properties...
PATCH {{baseUrl}}/api/customers/1/status?status=NEGOTIATING
Authorization: Bearer {{aliceLogin.response.body.token}}

### 7. Close the deal!
PATCH {{baseUrl}}/api/customers/1/status?status=SOLD
Authorization: Bearer {{aliceLogin.response.body.token}}
```

### Scenario 3: Admin Configures New Attribute

```http
### 1. Login as Admin
# @name adminLogin
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "{{$dotenv ADMIN_PASSWORD}}"
}

### 2. Create Boolean Attribute
POST {{baseUrl}}/api/property-attributes
Authorization: Bearer {{adminLogin.response.body.token}}
Content-Type: application/json

{
  "name": "Smart Home Ready",
  "dataType": "BOOLEAN",
  "category": "FEATURES",
  "isRequired": false,
  "isSearchable": true,
  "displayOrder": 60
}

### 3. Create Select Attribute with Options
POST {{baseUrl}}/api/property-attributes
Authorization: Bearer {{adminLogin.response.body.token}}
Content-Type: application/json

{
  "name": "View Quality",
  "dataType": "SINGLE_SELECT",
  "category": "LOCATION",
  "isRequired": false,
  "isSearchable": true,
  "displayOrder": 10,
  "options": [
    {"optionValue": "Ocean", "displayOrder": 1},
    {"optionValue": "Mountain", "displayOrder": 2},
    {"optionValue": "City", "displayOrder": 3},
    {"optionValue": "Garden", "displayOrder": 4},
    {"optionValue": "None", "displayOrder": 5}
  ]
}

### 4. Verify It Appears
GET {{baseUrl}}/api/property-attributes/search?name=smart
Authorization: Bearer {{adminLogin.response.body.token}}
```

### Scenario 4: Property Search with Filters

```http
### Complex property search
POST {{baseUrl}}/api/properties/search/by-criteria
Authorization: Bearer {{aliceLogin.response.body.token}}
Content-Type: application/json

{
  "minPrice": 400000,
  "maxPrice": 800000,
  "filters": [
    {
      "attributeId": 8,
      "dataType": "NUMBER",
      "minValue": 3,
      "maxValue": 5
    },
    {
      "attributeId": 18,
      "dataType": "BOOLEAN",
      "booleanValue": true
    },
    {
      "attributeId": 19,
      "dataType": "BOOLEAN",
      "booleanValue": true
    },
    {
      "attributeId": 2,
      "dataType": "SINGLE_SELECT",
      "selectedValues": ["Single Family Home"]
    }
  ]
}
```

---

## Tips & Tricks

### 1. Environment Variables

Create `.env` file for sensitive data:

```env
ADMIN_PASSWORD=YourSecurePassword123!
JWT_SECRET=your-secret-key
```

In `api-tests.http`, reference with:
```http
{
  "password": "{{$dotenv ADMIN_PASSWORD}}"
}
```

### 2. Variables and Reuse

Define reusable variables at the top:

```http
@baseUrl = http://localhost:8080
@agentId = 2
@propertyId = 1

### Use in requests
GET {{baseUrl}}/api/properties/{{propertyId}}
```

### 3. Quick Loops (VS Code REST Client)

Test with different data quickly:

```http
### Test with multiple prices
POST {{baseUrl}}/api/properties
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "title": "Test Property",
  "price": 100000
}

###
{
  "title": "Test Property 2",
  "price": 200000
}

###
{
  "title": "Test Property 3",
  "price": 300000
}
```

### 4. Response Inspection

**VS Code REST Client**:
- Click on response to see full JSON
- Copy response body with Ctrl+C
- Save response to file

**IntelliJ**:
- Response appears in "Run" panel
- Right-click → "Copy Response"
- View as JSON/XML/HTML

### 5. Debugging Failed Requests

Check these common issues:

| Error | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Missing/expired token | Re-login to get new token |
| 403 Forbidden | Insufficient permissions | Use correct role (e.g., admin for attributes) |
| 404 Not Found | Resource doesn't exist | Verify ID exists in database |
| 400 Bad Request | Validation error | Check request body format and required fields |
| 500 Internal Error | Server error | Check server logs |

### 6. Request History

**VS Code**:
- `Ctrl+Alt+H`: View request history
- Re-run previous requests quickly

**IntelliJ**:
- "Run" panel → "History" tab
- Rerun any previous request

### 7. Bulk Testing

Test all endpoints quickly:

```bash
# Using curl with the requests
for user in alice bob carol david; do
  echo "Testing as $user..."
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$user\",\"password\":\"password\"}"
done
```

### 8. Pagination Testing

Test different page sizes:

```http
### Page 1
GET {{baseUrl}}/api/properties?page=0&size=5

### Page 2
GET {{baseUrl}}/api/properties?page=1&size=5

### Large page
GET {{baseUrl}}/api/properties?page=0&size=50

### With sorting
GET {{baseUrl}}/api/properties?page=0&size=10&sort=price,desc
```

### 9. Performance Testing

Add timing information:

```http
### Time this request
# @name timedRequest
GET {{baseUrl}}/api/properties/search?minPrice=100000&maxPrice=1000000
Authorization: Bearer {{token}}

### The response shows timing in milliseconds
```

### 10. Error Scenario Testing

Deliberately test error handling:

```http
### Test 404
GET {{baseUrl}}/api/properties/99999

### Test 400 - Missing required field
POST {{baseUrl}}/api/properties
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "description": "Missing title and price"
}

### Test 403 - Wrong role
POST {{baseUrl}}/api/property-attributes
Authorization: Bearer {{agentToken}}
Content-Type: application/json

{
  "name": "Test",
  "dataType": "TEXT"
}
```

---

## Automated Testing Script

For CI/CD or batch testing, use this bash script:

```bash
#!/bin/bash
# test-endpoints.sh

BASE_URL="http://localhost:8080"

# Login and get token
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password"}' \
  | jq -r '.token')

echo "Token: $TOKEN"

# Test endpoints
echo "Testing Properties..."
curl -s -X GET "$BASE_URL/api/properties" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'

echo "Testing Customers..."
curl -s -X GET "$BASE_URL/api/customers" \
  -H "Authorization: Bearer $TOKEN" | jq '.content | length'

echo "Tests complete!"
```

**Usage**:
```bash
chmod +x test-endpoints.sh
./test-endpoints.sh
```

---

## Next Steps

### Production Testing

When testing against production:

1. **Update base URL**:
   ```http
   @baseUrl = https://api.yourdomain.com
   ```

2. **Use strong passwords**:
   ```http
   {
     "password": "{{$dotenv PROD_PASSWORD}}"
   }
   ```

3. **Enable TLS verification**

4. **Use production test accounts** (not demo data)

### Integration with CI/CD

1. Export tests to Newman (Postman CLI)
2. Run tests in GitHub Actions
3. Generate test reports
4. Monitor API health

### API Documentation

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## Troubleshooting

### Token Expired

**Symptom**: 401 Unauthorized after some time

**Solution**: Re-login or use refresh token:

```http
POST {{baseUrl}}/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{agentLogin.response.body.refreshToken}}"
}
```

### H2 Console Access (Dev Only)

View database contents:

1. Open: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:testdb`
3. Username: `sa`
4. Password: *(leave empty)*

### Server Not Running

```bash
# Start server
./mvnw spring-boot:run

# Check health
curl http://localhost:8080/actuator/health
```

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

---

## Summary

- **Fastest way**: Open `api-tests.http` in VS Code with REST Client extension
- **Complete coverage**: 100+ requests covering all endpoints
- **Mock data included**: 8 properties, 8 customers, 15 saved searches, 5 users
- **Auto-authentication**: Tokens auto-populate from login requests
- **Error testing**: Includes invalid requests to test error handling
- **Production-ready**: Easily adapt for production testing

**Start testing in 30 seconds**:
1. `./mvnw spring-boot:run`
2. Open `api-tests.http`
3. Click "Send Request" on any login
4. Test away! 🚀

---

**Related Documentation**:
- [API Documentation](API-Documentation.md)
- [Property CRUD Guide](property-crud.md)
- [Security Documentation](../SECURITY.md)
- [Frontend Integration Guide](Frontend-Integration.md)

**Last Updated**: 2025-11-19
