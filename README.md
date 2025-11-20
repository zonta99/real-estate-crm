# Real Estate CRM

This repository contains a Spring Boot backend for a Real Estate CRM.

## Quick Start

### Prerequisites
- Java 21
- Maven 3.6+

### Setup

1. **Set required environment variables** (see `.env.example`):
   ```bash
   export JWT_SECRET=$(openssl rand -base64 64)
   export ADMIN_PASSWORD="YourSecurePassword123!"
   export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:4200"
   ```

2. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the API**:
   - API docs: http://localhost:8080/swagger-ui/index.html
   - Health: http://localhost:8080/actuator/health
   - H2 Console (dev only): http://localhost:8080/h2-console

## Security

**IMPORTANT**: This application requires environment variables to be set for security.
See [SECURITY.md](SECURITY.md) for detailed security documentation.

## Documentation

### For Frontend Developers:
- [Frontend Integration Guide](docs/Frontend-Integration-Guide.md) - **START HERE** - Complete API guide with TypeScript types
- [API Testing Guide](docs/API-Testing-Guide.md) - Quick and easy endpoint testing

### For Backend Developers:
- [Security Documentation](SECURITY.md) - **READ THIS FIRST**
- [Lazy Initialization Prevention](docs/Lazy-Initialization-Prevention.md) - Critical JPA guide
- [AI Assistant Guide](CLAUDE.md) - Comprehensive development guide

## Development

See the docs files for how to manage properties and render, validate, and submit dynamic property attributes efficiently.
