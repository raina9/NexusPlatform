package com.raina.nexus.employee.controller;

import com.raina.nexus.client.feign.EmployeeFeignClient;
import com.raina.nexus.employee.dto.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feign")
@RequiredArgsConstructor
public class FeignTestController {

    private final EmployeeFeignClient employeeFeignClient;

    @GetMapping("/employee/{id}")
    public EmployeeResponse getEmployee(
            @PathVariable Long id) {

        return employeeFeignClient.getEmployee(id).data();
    }
}