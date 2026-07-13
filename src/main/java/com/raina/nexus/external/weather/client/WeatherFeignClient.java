package com.raina.nexus.external.weather.client;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ==========================================================
 * Weather API Feign Client
 * ==========================================================
 *
 * Purpose:
 * Communicates with Weather API using OpenFeign.
 *
 * Endpoint:
 * GET /current.json
 *
 * Example:
 * https://api.weatherapi.com/v1/current.json
 *      ?key=API_KEY
 *      &q=London
 *
 * ==========================================================
 */
@FeignClient(
        name = "weather-client",
        url = "${external.weather.base-url}"
)
public interface WeatherFeignClient {

    /**
     * Fetch Current Weather
     *
     * @param apiKey Weather API Key
     * @param city City Name
     * @return Weather Response
     */
    @GetMapping("/current.json")
    WeatherResponse getWeather(

            @RequestParam("key")
            String apiKey,

            @RequestParam("q")
            String city
    );
}