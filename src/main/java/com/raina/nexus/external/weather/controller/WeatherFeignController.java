package com.raina.nexus.external.weather.controller;

import com.raina.nexus.external.weather.dto.WeatherResponse;

import com.raina.nexus.external.weather.service.WeatherFeignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==========================================================
 * Weather API Controller - OpenFeign
 * ==========================================================
 *
 * Purpose:
 * Exposes REST endpoint to fetch current weather
 * information using OpenFeign.
 *
 * Endpoint:
 * GET /api/weather/feign/{city}
 *
 * Example:
 * GET /api/weather/feign/London
 *
 * ==========================================================
 */
@RestController
@RequestMapping("/api/weather/feign")
@RequiredArgsConstructor
public class WeatherFeignController {

    private final WeatherFeignService weatherService;

    /**
     * Fetch Current Weather By City
     *
     * Example:
     * GET /api/weather/feign/London
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