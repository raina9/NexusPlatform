package com.raina.nexus.external.weather.controller;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import com.raina.nexus.external.weather.service.WeatherWebClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * Weather API Controller - WebClient
 * ==========================================================
 *
 * Purpose:
 * Exposes REST endpoint to fetch current weather
 * information using WebClient.
 *
 * Endpoint:
 * GET /api/weather/webclient/{city}
 *
 * Example:
 * GET /api/weather/webclient/London
 *
 * ==========================================================
 */
@RestController
@RequestMapping("/api/weather/webclient")
@RequiredArgsConstructor
public class WeatherWebClientController {

    private final WeatherWebClientService weatherService;

    /**
     * Fetch Current Weather By City
     *
     * Example:
     * GET /api/weather/webclient/London
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