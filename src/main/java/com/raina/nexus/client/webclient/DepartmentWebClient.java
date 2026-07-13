package com.raina.nexus.client.webclient;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.department.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Department WebClient
 */
@Component
@RequiredArgsConstructor
public class DepartmentWebClient {

    private final WebClient webClient;

    public ApiResponse<DepartmentResponse> getDepartment(Long id) {

        return webClient
                .get()
                .uri("/api/departments/{id}", id)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<
                                ApiResponse<DepartmentResponse>>() {
                        }
                )
                .block();
    }
}