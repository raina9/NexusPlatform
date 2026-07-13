package com.raina.nexus.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(

        // Department Name
        @NotBlank(message = "Department name is required")
        @Size(
                min = 2,
                max = 100,
                message = "Department name must be between 2 and 100 characters"
        )
        String departmentName

) {
}