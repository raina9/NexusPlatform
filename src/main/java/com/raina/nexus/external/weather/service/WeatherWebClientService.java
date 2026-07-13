package com.raina.nexus.external.weather.service;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * ==========================================================
 * Weather API Integration - WebClient
 * ==========================================================
 *
 * Purpose:
 * Fetches current weather information using
 * Spring WebClient.
 *
 * Authentication:
 * API Key based authentication.
 *
 * Used For:
 * Demonstrating modern HTTP client integration.
 *
 * ==========================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherWebClientService {

    private final WebClient webClient;

    /**
     * Weather API Base URL
     */
    @Value("${external.weather.base-url}")
    private String baseUrl;

    /**
     * Weather API Key
     */
    @Value("${external.weather.api-key}")
    private String apiKey;

    /**
     * Fetch Current Weather By City
     *
     * @param city City Name
     * @return Weather Response
     */
    public WeatherResponse getWeather(String city) {

        log.info(
                "Fetching weather using WebClient for city={}",
                city
        );

        return webClient
                .get()
                .uri(
                        baseUrl +
                        "/current.json?key=" +
                        apiKey +
                        "&q=" +
                        city
                )
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .block();
    }
}