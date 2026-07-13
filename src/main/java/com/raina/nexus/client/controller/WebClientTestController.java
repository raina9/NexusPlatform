package com.raina.nexus.client.controller;

import com.raina.nexus.client.webclient.EmployeeWebClient;
import com.raina.nexus.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webclient")
@RequiredArgsConstructor
public class WebClientTestController {

    private final EmployeeWebClient employeeWebClient;

    @GetMapping("/employees/{id}")
    public Object getEmployee(
            @PathVariable Long id) {

        return employeeWebClient.getEmployee(id);
    }
}