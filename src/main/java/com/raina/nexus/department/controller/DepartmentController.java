package com.raina.nexus.department.controller;

import com.raina.nexus.common.response.ApiResponse;
import com.raina.nexus.department.dto.DepartmentRequest;
import com.raina.nexus.department.dto.DepartmentResponse;
import com.raina.nexus.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentResponse response =
                departmentService.createDepartment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(
                        true,
                        "Department created successfully",
                        response
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartment(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Department fetched successfully",
                        departmentService.getDepartmentById(id)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Departments fetched successfully",
                        departmentService.getAllDepartments()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @PathVariable Long id) {

        departmentService.deleteDepartment(id);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Department deleted successfully",
                        null
                )
        );
    }

    @GetMapping("/webclient/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentViaWebClient(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Department fetched successfully",
                        departmentService.getDepartmentViaWebClient(id)
                )
        );
    }
}