package com.raina.nexus.employee.controller;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse response =
                employeeService.createEmployee(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Employee created successfully",
                        response
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employee fetched successfully",
                        employeeService.getEmployeeById(id)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getAllEmployees()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @PathVariable Long id) {

        employeeService.deleteEmployee(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employee deleted successfully",
                        null
                )
        );
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getEmployees(
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployees(pageable)
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> searchEmployees(
            @RequestParam String firstName) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.searchEmployees(firstName)
                )
        );
    }

    @GetMapping("/specification")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> specificationSearch(
            @RequestParam String firstName) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.searchWithSpecification(firstName)
                )
        );
    }

    @GetMapping("/salary")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesBySalary(
            @RequestParam Double salary) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService
                                .getEmployeesWithSalaryGreaterThan(salary)
                )
        );
    }
}