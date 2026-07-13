package com.raina.nexus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ==========================================================
 * RestTemplate Configuration
 * ==========================================================
 *
 * Purpose:
 * Creates a singleton RestTemplate bean managed by Spring.
 *
 * Why Required?
 * RestTemplate is not automatically registered as a bean.
 * We must explicitly create it so it can be injected
 * into service classes.
 *
 * Used By:
 * - Internal API Communication
 * - External API Communication
 * - Third Party Integrations
 *
 * Examples:
 * - Employee Service Calls
 * - Weather API
 * - Payment Gateway
 * - Verification Providers
 *
 * ==========================================================
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates RestTemplate Bean
     */
    @Bean
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }
}