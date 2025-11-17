# Security Documentation

## Environment Variables

This application requires the following environment variables to be set for security:

### Required Environment Variables

1. **JWT_SECRET** (REQUIRED)
   - **Description**: Secret key for signing JWT tokens
   - **Generate with**: `openssl rand -base64 64`
   - **Minimum length**: 256 bits (32 bytes)
   - **Example**: `JWT_SECRET=your-super-secret-jwt-key-here-minimum-256-bits`

2. **ADMIN_PASSWORD** (REQUIRED)
   - **Description**: Initial admin user password
   - **Minimum length**: 12 characters (recommended: 16+)
   - **Requirements**: Use a strong, unique password
   - **Example**: `ADMIN_PASSWORD=ChangeThisPasswordImmediately!`

3. **CORS_ALLOWED_ORIGINS** (RECOMMENDED)
   - **Description**: Comma-separated list of allowed frontend origins
   - **Default**: `http://localhost:3000,http://localhost:4200`
   - **Production**: Set to your actual frontend domains
   - **Example**: `CORS_ALLOWED_ORIGINS=https://app.example.com,https://admin.example.com`

### Optional Environment Variables

- **JWT_EXPIRATION**: Token expiration time in milliseconds (default: 86400000 = 24 hours)
- **ADMIN_USERNAME**: Admin username (default: admin)

## Setting Environment Variables

### Development (Linux/Mac)

Create a `.env` file in the project root (copy from `.env.example`):

```bash
cp .env.example .env
# Edit .env with your values
```

Then export before running:

```bash
export $(cat .env | xargs)
./mvnw spring-boot:run
```

### Development (Windows)

```cmd
set JWT_SECRET=your-secret-here
set ADMIN_PASSWORD=your-password-here
set CORS_ALLOWED_ORIGINS=http://localhost:3000
mvnw.cmd spring-boot:run
```

### Production

Use your deployment platform's environment variable management:
- **Docker**: Use `-e` flag or `env_file` in docker-compose
- **Kubernetes**: Use ConfigMaps and Secrets
- **Cloud platforms**: Use their secret management services (AWS Secrets Manager, Azure Key Vault, etc.)

## Security Features

### 1. JWT Authentication

- Tokens are signed using HMAC-SHA with 256-bit secret
- Tokens expire after 24 hours (configurable)
- Tokens include user ID, username, and email claims
- Stateless authentication (no server-side sessions)

### 2. CORS Protection

- CORS is configured to allow specific origins only
- Wildcard origins are NOT allowed in production
- Credentials are properly handled
- Preflight requests are supported

### 3. CSRF Protection Strategy

**Current Implementation**: CSRF protection is DISABLED

**Why?** This application uses JWT tokens in the `Authorization` header (not cookies). CSRF attacks rely on browsers automatically sending cookies. With header-based tokens, attackers cannot access or send tokens from their malicious sites due to Same-Origin Policy.

**This is safe IF AND ONLY IF**:
1. JWT tokens are sent ONLY in the `Authorization` header (not cookies)
2. Tokens are stored securely in the frontend (memory or sessionStorage)
3. The frontend properly implements CORS
4. XSS vulnerabilities are mitigated

**WARNING**: If you switch to cookie-based authentication (e.g., httpOnly cookies), you MUST enable CSRF protection.

### 4. Password Security

- Passwords are hashed using BCrypt with cost factor 10
- Minimum password length: 6 characters (increase in production)
- Passwords are never logged or exposed in responses
- Admin password must be set via environment variable

### 5. H2 Console Security

- H2 console is enabled ONLY in development profile
- Remote access is disabled (`web-allow-others: false`)
- Console should be completely disabled in production
- Access restricted to localhost only

### 6. Security Headers

Development profile includes:
- Frame options disabled (for H2 console)
- Relaxed Content Security Policy

Production profile includes:
- Strict Content Security Policy
- HTTP Strict Transport Security (HSTS)
- X-Frame-Options: DENY
- Referrer Policy: strict-origin-when-cross-origin

## Security Checklist for Production

- [ ] Set strong JWT_SECRET (minimum 256 bits, generated randomly)
- [ ] Set strong ADMIN_PASSWORD (minimum 16 characters, complex)
- [ ] Configure CORS_ALLOWED_ORIGINS with actual frontend domains
- [ ] Disable H2 console or use production database (PostgreSQL)
- [ ] Enable HTTPS/TLS for all communications
- [ ] Set `spring.profiles.active=prod` environment variable
- [ ] Review and restrict API access with proper authorization
- [ ] Enable rate limiting on authentication endpoints
- [ ] Set up monitoring and logging for security events
- [ ] Regular security audits and dependency updates
- [ ] Implement backup and disaster recovery
- [ ] Use proper secret management (AWS Secrets Manager, Vault, etc.)

## Known Security Improvements Needed

See the main security analysis for a comprehensive list of improvements:

### Critical
1. Implement rate limiting on authentication endpoints
2. Add account lockout after failed login attempts
3. Implement proper refresh token mechanism with rotation

### High Priority
1. Add comprehensive test coverage (currently ~3%)
2. Implement access control validation (prevent IDOR)
3. Add request/response auditing
4. Implement soft delete instead of hard delete
5. Add database indexes for performance and security

### Medium Priority
1. Implement caching strategy
2. Add OpenAPI/Swagger documentation
3. Create custom exception hierarchy
4. Implement database migrations (Flyway/Liquibase)
5. Add health checks with custom indicators

## Reporting Security Issues

If you discover a security vulnerability, please email security@example.com (DO NOT create a public issue).

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if any)

We will respond within 48 hours and provide a fix timeline.
