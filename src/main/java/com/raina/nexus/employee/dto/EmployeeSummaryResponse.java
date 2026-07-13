package com.raina.nexus.employee.dto;

public record EmployeeSummaryResponse(
        Long id,
        String firstName,
        Double salary
) {
}