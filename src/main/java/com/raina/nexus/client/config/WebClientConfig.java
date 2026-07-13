package com.raina.nexus.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================================
 * WebClient Configuration
 * ==========================================================
 *
 * Purpose:
 * Creates reusable WebClient bean.
 *
 * Used By:
 * - Internal API Communication
 * - External API Integration
 * - Future Third Party Providers
 *
 * ==========================================================
 */
@Configuration
public class WebClientConfig {

    /**
     * Shared WebClient Bean
     */
    @Bean
    public WebClient webClient() {

        return WebClient.builder().build();
    }
}