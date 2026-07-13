package com.raina.nexus.employee.controller;

import com.raina.nexus.client.EmployeeClient;
import com.raina.nexus.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final EmployeeClient employeeClient;

    @GetMapping("/employee/{id}")
    public EmployeeResponse getEmployee(
            @PathVariable Long id) {

        return employeeClient.getEmployee(id);
    }
}