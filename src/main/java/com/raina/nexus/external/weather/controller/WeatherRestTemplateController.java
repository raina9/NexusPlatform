package com.raina.nexus.external.weather.controller;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import com.raina.nexus.external.weather.service.WeatherRestTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * Weather API Controller - RestTemplate
 * ==========================================================
 *
 * Purpose:
 * Exposes REST endpoint to fetch current weather
 * information using RestTemplate.
 *
 * Endpoint:
 * GET /api/weather/rest/{city}
 *
 * Example:
 * GET /api/weather/rest/London
 *
 * ==========================================================
 */
@RestController
@RequestMapping("/api/weather/rest")
@RequiredArgsConstructor
public class WeatherRestTemplateController {

    private final WeatherRestTemplateService weatherService;

    /**
     * Fetch Current Weather By City
     *
     * Example:
     * GET /api/weather/rest/London
     *
     * @param city City Name
     * @return Weather Information
     */
    @GetMapping("/{city}")
    public WeatherResponse getWeather(
            @PathVariable String city
    ) {

        return weatherService.getWeather(city);
    }
}