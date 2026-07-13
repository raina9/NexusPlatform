package com.raina.nexus.external.weather.service;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * ==========================================================
 * Weather API Integration - RestTemplate
 * ==========================================================
 *
 * Purpose:
 * Fetches current weather information from WeatherAPI.
 *
 * Authentication:
 * API Key based authentication.
 *
 * Endpoint:
 * GET /current.json
 *
 * Example:
 * https://api.weatherapi.com/v1/current.json
 *      ?key=API_KEY
 *      &q=London
 *
 * Used For:
 * Demonstrating external API integration using RestTemplate.
 *
 * ==========================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherRestTemplateService {

    private final RestTemplate restTemplate;

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
                "Fetching weather using RestTemplate for city={}",
                city
        );

        String url =
                baseUrl +
                "/current.json?key=" +
                apiKey +
                "&q=" +
                city;

        log.debug(
                "Calling Weather API URL={}",
                url.replace(apiKey, "********")
        );

        WeatherResponse response =
                restTemplate.getForObject(
                        url,
                        WeatherResponse.class
                );

        log.info(
                "Weather fetched successfully for city={}",
                city
        );

        return response;
    }
}