package com.raina.nexus.external.weather.controller;

import com.raina.nexus.external.weather.dto.WeatherResponse;
import com.raina.nexus.external.weather.service.WeatherFeignService;
import com.raina.nexus.external.weather.service.WeatherRestTemplateService;
import com.raina.nexus.external.weather.service.WeatherWebClientService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        WeatherRestTemplateController.class,
        WeatherWebClientController.class,
        WeatherFeignController.class
})
class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WeatherRestTemplateService weatherRestTemplateService;

    @MockitoBean
    private WeatherWebClientService weatherWebClientService;

    @MockitoBean
    private WeatherFeignService weatherFeignService;

    private WeatherResponse sampleWeatherResponse() {

        return new WeatherResponse(
                new WeatherResponse.Location(
                        "London",
                        "City of London, Greater London",
                        "United Kingdom"
                ),
                new WeatherResponse.Current(
                        18.0,
                        72,
                        14.4,
                        new WeatherResponse.Condition("Partly cloudy")
                )
        );
    }

    @Test
    void shouldReturnWeatherViaRestTemplate() throws Exception {

        when(weatherRestTemplateService.getWeather("London"))
                .thenReturn(sampleWeatherResponse());

        mockMvc.perform(get("/api/weather/rest/London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.name").value("London"))
                .andExpect(jsonPath("$.current.temp_c").value(18.0));
    }

    @Test
    void shouldReturnWeatherViaWebClient() throws Exception {

        when(weatherWebClientService.getWeather("London"))
                .thenReturn(sampleWeatherResponse());

        mockMvc.perform(get("/api/weather/webclient/London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.name").value("London"))
                .andExpect(jsonPath("$.current.condition.text").value("Partly cloudy"));
    }

    @Test
    void shouldReturnWeatherViaFeign() throws Exception {

        when(weatherFeignService.getWeather("London"))
                .thenReturn(sampleWeatherResponse());

        mockMvc.perform(get("/api/weather/feign/London"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.name").value("London"))
                .andExpect(jsonPath("$.current.humidity").value(72));
    }
}
