package com.raina.nexus.employee.dto;

public record AddressResponse(
        Long id,
        String city,
        String state,
        String country
) {
}
