package com.raina.nexus.employee.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Country is required")
        String country

) {
}
