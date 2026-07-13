package com.raina.nexus.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmployeeRequest(

        // Employee First Name
        @NotBlank(message = "First name is required")
        String firstName,

        // Employee Last Name
        @NotBlank(message = "Last name is required")
        String lastName,

        // Employee Email
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        // Employee Salary
        @NotNull(message = "Salary is required")
        @Positive(message = "Salary must be greater than zero")
        Double salary

) {
}