package com.raina.nexus.client;

import com.raina.nexus.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * External Employee API Client
 */
@Component
@RequiredArgsConstructor
public class EmployeeClient {

    private final RestTemplate restTemplate;

    /**
     * Calls Employee API
     */
    public EmployeeResponse getEmployee(Long id) {

        String url =
                "http://localhost:9191/api/employees/" + id;

        return restTemplate.getForObject(
                url,
                EmployeeResponse.class
        );
    }
}