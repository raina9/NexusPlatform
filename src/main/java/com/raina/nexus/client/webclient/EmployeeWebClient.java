package com.raina.nexus.client.webclient;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Employee WebClient
 */
@Component
@RequiredArgsConstructor
public class EmployeeWebClient {

    private final WebClient webClient;

    public ApiResponse<EmployeeResponse> getEmployee(Long id) {

        return webClient
                .get()
                .uri("/api/employees/{id}", id)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<
                                ApiResponse<EmployeeResponse>>() {
                        }
                )
                .block();
    }
}