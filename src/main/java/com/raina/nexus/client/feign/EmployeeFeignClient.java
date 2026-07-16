package com.raina.nexus.client.feign;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.employee.dto.EmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP Client
 */
@FeignClient(
        name = "employee-client",
        url = "http://localhost:9191"
)
public interface EmployeeFeignClient {

    @GetMapping("/api/employees/{id}")
    ApiResponse<EmployeeResponse> getEmployee(
            @PathVariable Long id
    );
}