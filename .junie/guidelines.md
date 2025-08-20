Real Estate CRM — Development Guidelines

Scope: This document captures project-specific guidance for building, testing, and extending the Real Estate CRM backend. It targets experienced Java/Spring developers and focuses on details that are unique or easy to miss in this codebase.

1) Build and Configuration

- Toolchain
  - Java: 21 (enforced via pom.xml property <java.version>21</java.version>)
  - Build: Maven, Spring Boot 3.5.3 (parent BOM)
  - Starters: Web, Security, Data JPA, Validation, Actuator; Devtools (runtime, optional)
  - Databases: H2 (runtime) and PostgreSQL (runtime)
  - JWT: io.jsonwebtoken:jjwt-* 0.12.6

- Quick build
  - Windows PowerShell in project root:
    - mvnw.cmd -v  (verify Maven wrapper runs)
    - mvnw.cmd -q -DskipTests package  (fast packaging)
    - mvnw.cmd test  (run tests)

- Running the application
  - Default profile is none; security is profile-aware (see Security section). For local dev, run with the dev profile:
    - mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  - Server defaults: 0.0.0.0:8080 (see src\main\resources\application.properties)
  - Swagger/OpenAPI: /swagger-ui/index.html and /v3/api-docs
  - Actuator health: /actuator/health
  - H2 console (dev only, if H2 is active): /h2-console

- Critical configuration: JWT properties
  - JwtUtils requires two properties to be present at runtime:
    - jwt.secret: HMAC signing key. Must be sufficiently long for Keys.hmacShaKeyFor (>= 32 ASCII chars for HS256). Example:
      - jwt.secret=dev-secret-key-0123456789-0123456789
    - jwt.expiration: token lifetime in milliseconds. Example:
      - jwt.expiration=86400000  (24h)
  - Where to set:
    - For local dev: put into application.properties or a profile-specific file (e.g., application-dev.properties), or provide as environment variables (SPRING_APPLICATION_JSON or java -Djwt.secret=... -Djwt.expiration=...).
  - Note: AuthController has a default for jwt.expiration, but JwtUtils does not — ensure both values resolve in the Spring Environment to avoid startup failures.

- Database notes
  - H2 is on the classpath with scope runtime. If you intend to leverage in-memory DB locally, ensure the datasource is configured, e.g. in application-dev.properties:
    - spring.datasource.url=jdbc:h2:mem:crmdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    - spring.datasource.driverClassName=org.h2.Driver
    - spring.datasource.username=sa
    - spring.datasource.password=
    - spring.jpa.hibernate.ddl-auto=update  (or create-drop for test-only runs)
  - PostgreSQL is also available at runtime. Provide standard Spring Boot datasource properties for prod.

2) Testing

- Stack & conventions
  - JUnit 5 via spring-boot-starter-test; Spring Security test support available.
  - Typical integration test pattern: @SpringBootTest (see src\test\java\com\realestatecrm\RealEstateCrmApplicationTests.java).
  - Package base: com.realestatecrm; place tests under src\test\java\com\realestatecrm to inherit component scanning and simplify @SpringBootTest.

- Running tests
  - Run the entire test suite:
    - mvnw.cmd test
  - Run a single test class by FQN:
    - mvnw.cmd -Dtest=com.realestatecrm.RealEstateCrmApplicationTests test
  - Run a single test method:
    - mvnw.cmd -Dtest=com.realestatecrm.RealEstateCrmApplicationTests#contextLoads test

- Environment required for tests that bootstrap Spring
  - If a test loads the full application context (@SpringBootTest), ensure jwt.secret and jwt.expiration are resolvable. Recommended to create src\test\resources\application-test.properties with safe defaults and activate the test profile, e.g.:
    - File: src\test\resources\application-test.properties
      - jwt.secret=test-secret-key-0123456789-0123456789
      - jwt.expiration=3600000
    - Activate by annotating tests with @ActiveProfiles("test") or by Maven Surefire argLine/systemPropertyVariables if you prefer not to annotate.
  - Without providing these, context bootstrap may fail once JwtUtils is initialized.

- Adding a new test (example)
  - Create a simple JUnit 5 test under com.realestatecrm package:

    package com.realestatecrm;

    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.assertTrue;

    class SampleSmokeTest {
        @Test
        void sanity() {
            assertTrue(1 + 1 == 2);
        }
    }

  - Run just this test:
    - mvnw.cmd -Dtest=com.realestatecrm.SampleSmokeTest test

- Verified demonstration
  - During guideline preparation, we created a minimal JUnit test (SampleSmokeTest) under src\test\java\com\realestatecrm, executed it successfully, and then removed it to keep the repository clean, as required by the task. The existing @SpringBootTest (RealEstateCrmApplicationTests) also runs green in this environment.

3) Additional Development Information

- Security configuration is profile-specific (src\main\java\com\realestatecrm\config\SecurityConfig.java):
  - dev profile: Stateless sessions; supports both JWT and HTTP Basic; H2 console allowed; relaxed CSP; JWT filter registered.
  - prod profile: JWT only; stricter security headers (HSTS, CSP, referrer policy); H2 console disallowed.
  - Public endpoints include /api/auth/login, /api/auth/refresh, /api/auth/logout, /actuator/health, Swagger UI/docs, and (dev) /h2-console.

- DTOs and layering
  - DTOs live under com.realestatecrm.dto.* structured by domain (auth, user, property, propertyattribute, customer). Entities, repositories, and services follow a standard Spring layering under com.realestatecrm.entity, .repository, .service, respectively.
  - Validation is available via spring-boot-starter-validation; annotate request DTOs with javax/jakarta validation constraints where applicable.

- Logging
  - Default log levels are DEBUG for com.realestatecrm and org.springframework.security (see application.properties). Adjust when running locally to reduce noise.

- OpenAPI
  - springdoc-openapi-starter-webmvc-ui is configured; review controllers and DTOs to ensure accurate schema exposure. Swagger UI available at /swagger-ui/.

- Code style & language level
  - Java 21 features are allowed. Prefer records for immutable DTOs where appropriate, otherwise use simple POJOs. No Lombok is present; follow explicit getters/constructors as seen in dto classes.

- Common pitfalls
  - Missing JWT properties: If you see Property 'jwt.secret' could not be resolved during context startup, provide the properties as noted above or activate a profile with those values.
  - H2 vs Postgres differences: If leveraging H2, consider MODE=PostgreSQL to reduce dialect differences for local development.
  - Security tests: When testing secured endpoints, use spring-security-test utilities (e.g., @WithMockUser) and consider disabling JWT filter via test config if you are focusing on controller logic only.

- Profiles quick reference
  - Dev: mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
  - Prod: supply prod-ready properties and run the packaged jar or via Spring Boot Maven plugin without dev profile.

Revision date: 2025-08-20
