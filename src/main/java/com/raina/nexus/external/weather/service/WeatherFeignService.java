package com.raina.nexus.external.weather.service;

import com.raina.nexus.external.weather.client.WeatherFeignClient;
import com.raina.nexus.external.weather.dto.WeatherResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * ==========================================================
 * Weather API Integration - OpenFeign
 * ==========================================================
 *
 * Purpose:
 * Fetches current weather information using
 * OpenFeign Client.
 *
 * Authentication:
 * API Key based authentication.
 *
 * ==========================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherFeignService {

    private final WeatherFeignClient weatherFeignClient;

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
                "Fetching weather using Feign for city={}",
                city
        );

        return weatherFeignClient.getWeather(
                apiKey,
                city
        );
    }
}