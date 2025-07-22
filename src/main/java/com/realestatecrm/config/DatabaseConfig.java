package com.realestatecrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // Keep this for created/modified date functionality
public class DatabaseConfig {
    // No need for @EnableJpaRepositories here.
    // Spring Boot will handle it automatically.
}