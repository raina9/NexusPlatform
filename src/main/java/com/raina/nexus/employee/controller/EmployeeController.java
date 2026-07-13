package com.raina.nexus.employee.controller;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.common.response.CursorPageResponse;
import com.raina.nexus.employee.dto.EmployeeRequest;
import com.raina.nexus.employee.dto.EmployeeResponse;
import com.raina.nexus.employee.dto.EmployeeSummaryResponse;
import com.raina.nexus.employee.projection.EmployeeProjection;
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

    @GetMapping("/native")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getEmployeesNative(
            @RequestParam Double salary) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService
                                .getEmployeesWithSalaryGreaterThanNative(salary)
                )
        );
    }

    @GetMapping("/cursor")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeResponse>>> getEmployeesCursor(
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeesCursor(cursor, size)
                )
        );
    }

    @GetMapping("/keyset")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeResponse>>> getEmployeesKeyset(
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeesKeyset(afterId, size)
                )
        );
    }

    @GetMapping("/projection/interface")
    public ResponseEntity<ApiResponse<Page<EmployeeProjection>>> getEmployeeProjectionsPage(
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeProjections(pageable)
                )
        );
    }

    @GetMapping("/projection/interface/cursor")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeProjection>>> getEmployeeProjectionsCursor(
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeProjectionsCursor(cursor, size)
                )
        );
    }

    @GetMapping("/projection/interface/keyset")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeProjection>>> getEmployeeProjectionsKeyset(
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeProjectionsKeyset(afterId, size)
                )
        );
    }

    @GetMapping("/projection/dto")
    public ResponseEntity<ApiResponse<Page<EmployeeSummaryResponse>>> getEmployeeSummariesPage(
            Pageable pageable) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeSummaries(pageable)
                )
        );
    }

    @GetMapping("/projection/dto/cursor")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeSummaryResponse>>> getEmployeeSummariesCursor(
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeSummariesCursor(cursor, size)
                )
        );
    }

    @GetMapping("/projection/dto/keyset")
    public ResponseEntity<ApiResponse<CursorPageResponse<EmployeeSummaryResponse>>> getEmployeeSummariesKeyset(
            @RequestParam(defaultValue = "0") Long afterId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Employees fetched successfully",
                        employeeService.getEmployeeSummariesKeyset(afterId, size)
                )
        );
    }
}